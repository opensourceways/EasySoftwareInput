package com.easysoftwareinput.application.rpmpackage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.easysoftwareinput.domain.rpmpackage.model.GitRepoConfig;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

@Component
public class GitRepoService {
    /**
     * logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(GitRepoService.class);

    /**
     * config.
     */
    @Autowired
    private GitRepoConfig config;

    /**
     * get org repos.
     * @param org org.
     * @return repos.
     */
    public Set<String> getOrgRepos(String org) {
        int perPage = 100;
        int page = 1;
        Set<String> res = new HashSet<>();
        while (true) {
            int startPage = page;
            int endPage = page + config.getPageInterval();
            Set<String> names = getOrgPageRepos(org, startPage, endPage);
            if (names == null || names.isEmpty()) {
                break;
            }
            res.addAll(names);
            page += config.getPageInterval();
        }
        return res;
    }

    /**
     * get repos within pages.
     * @param org org.
     * @param startPage startPage.
     * @param endPage endPage.
     * @return repos.
     */
    public Set<String> getOrgPageRepos(String org, int startPage, int endPage) {
        if (endPage < startPage) {
            return Collections.emptySet();
        }
        ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(10);
        List<CompletableFuture<List<String>>> taskList = new ArrayList<>();
        for (int curPage = startPage; curPage < endPage; curPage++) {
            String url = String.format(config.getOrgTemplate(), org, config.getToken(), curPage, config.getPerPage());
            CompletableFuture<List<String>> task = CompletableFuture.supplyAsync(
                    () -> getNameList(url), executor);
            taskList.add(task);
        }

        CompletableFuture.allOf(taskList.toArray(new CompletableFuture[0])).join();
        Set<String> nameList = new HashSet<>();
        for (CompletableFuture<List<String>> task : taskList) {
            try {
                List<String> curList = task.get();
                nameList.addAll(curList);
            } catch (Exception e) {
                LOGGER.error("fail to get page, cause: {}", e.getMessage());
            }
        }
        return nameList;
    }

    /**
     * get names from url.
     * @param url url.
     * @return names.
     */
    public List<String> getNameList(String url) {
        RestTemplate rest = new RestTemplate();
        List<String> nameList = new ArrayList<>();
        ResponseEntity<JsonNode> res = rest.getForEntity(url, JsonNode.class);
        ArrayNode array = (ArrayNode) res.getBody();
        if (array == null || array.size() == 0) {
            return Collections.emptyList();
        }
        for (JsonNode repo : array) {
            String name = repo.get("name").asText();
            nameList.add(name);
        }
        return nameList;
    }
}
