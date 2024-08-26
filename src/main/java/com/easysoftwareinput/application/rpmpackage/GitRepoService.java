/* Copyright (c) 2024 openEuler Community
 EasySoftwareInput is licensed under the Mulan PSL v2.
 You can use this software according to the terms and conditions of the Mulan PSL v2.
 You may obtain a copy of Mulan PSL v2 at:
     http://license.coscl.org.cn/MulanPSL2
 THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
 EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
 MERCHANTABILITY OR FIT FOR A PARTICULAR PURPOSE.
 See the Mulan PSL v2 for more details.
*/

package com.easysoftwareinput.application.rpmpackage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.ParseException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.easysoftwareinput.common.utils.ObjectMapperUtil;
import com.easysoftwareinput.domain.gitrepo.model.RepoFileVO;
import com.easysoftwareinput.domain.repopkgnamemapper.RepoPkgNamePkg;
import com.easysoftwareinput.domain.rpmpackage.model.GitRepoConfig;
import com.fasterxml.jackson.core.type.TypeReference;
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
     * client.
     */
    @Autowired
    private CloseableHttpClient client;


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
        ResponseEntity<JsonNode> res;
        try {
            res = rest.getForEntity(url, JsonNode.class);
        } catch (Exception e) {
            LOGGER.error("can not get name, url: {}, cause: {}", url, e.getMessage());
            return Collections.emptyList();
        }

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

    /**
     * get branches from repo.
     * @param orgName org.
     * @param repoName repo.
     * @return list of pkg.
     */
    public List<RepoPkgNamePkg> getBranchesFromRepo(String orgName, String repoName) {
        if (StringUtils.isBlank(orgName) || StringUtils.isBlank(repoName)) {
            return null;
        }

        int page = 1;
        List<RepoPkgNamePkg> pkgList = new ArrayList<>();
        while (true) {
            List<String> pageBranches = getBranchesFromRepo(orgName, repoName, page);
            if (pageBranches == null || pageBranches.isEmpty()) {
                break;
            }
            for (String branch : pageBranches) {
                pkgList.add(RepoPkgNamePkg.of(branch, orgName, repoName));
            }
            page++;
        }
        return pkgList;
    }

    /**
     * get branch from repo.
     * @param orgName org.
     * @param repoName repo.
     * @param page page.
     * @return branches.
     */
    public List<String> getBranchesFromRepo(String orgName, String repoName, int page) {
        String url = String.format(config.getRepoBranchTemplate(), orgName, repoName, config.getToken(), page,
        config.getPerPage());
        HttpGet httpGet = new HttpGet(url);
        String rawContext;
        try {
            CloseableHttpResponse response = client.execute(httpGet);
            rawContext = EntityUtils.toString(response.getEntity());
        } catch (ParseException | IOException e) {
            LOGGER.error("fail get branch, org: {}, repo: {}, page: {}, cause: {}",
                    orgName, repoName, page, e.getMessage());
            return null;
        }
        JsonNode info = ObjectMapperUtil.toJsonNode(rawContext);
        if (info == null || !(info instanceof ArrayNode)) {
            return null;
        }

        List<String> branchList = new ArrayList<>();
        for (JsonNode branch : info) {
            String name = branch.get("name").asText();
            branchList.add(name);
        }
        return branchList;
    }

    /**
     * get spec context from repo.
     * @param pkg pkg.
     * @return pkg.
     */
    public RepoPkgNamePkg getSpecContextFromRepo(RepoPkgNamePkg pkg) {
        List<RepoFileVO> fileList = getFileTreeFromRepo(pkg.getOrg(), pkg.getRepoName(), pkg.getBranch());
        if (fileList == null || fileList.isEmpty()) {
            return null;
        }

        List<RepoFileVO> specList = fileList.stream().filter(
            file -> !Objects.isNull(file.getPath()) && file.getPath().endsWith(".spec")
        ).collect(Collectors.toList());

        if (specList == null || specList.size() == 0) {
            pkg.setHasSpec(false);
            return pkg;
        }

        pkg.setHasSpec(true);
        List<String> blobList = new ArrayList<>();
        for (RepoFileVO spec : specList) {
            String blob = getBlobFile(pkg.getOrg(), pkg.getRepoName(), spec.getSha());
            blobList.add(blob);
        }
        pkg.setRawSpecContextList(blobList);
        return pkg;
    }

    /**
     * get tree from repo.
     * @param orgName org.
     * @param repoName repo.
     * @param branchName branch.
     * @return file tree.
     */
    public List<RepoFileVO> getFileTreeFromRepo(String orgName, String repoName, String branchName) {
        String url = String.format(config.getTreeTemplate(), orgName, repoName, branchName, config.getToken());
        HttpGet httpGet = new HttpGet(url);
        String rawContent;
        try {
            CloseableHttpResponse response = client.execute(httpGet);
            rawContent = EntityUtils.toString(response.getEntity());
        } catch (ParseException | IOException e) {
            LOGGER.error("fail get tree, org: {}, repo: {}, branch: {}, cause: {}",
                    orgName, repoName, branchName, e.getMessage());
            return Collections.emptyList();
        }
        JsonNode res = ObjectMapperUtil.toJsonNode(rawContent);
        if (res == null) {
            return Collections.emptyList();
        }
        JsonNode tree = res.get("tree");
        if (tree == null || !(tree instanceof ArrayNode)) {
            return Collections.emptyList();
        }

        return ObjectMapperUtil.convertValue(tree, new TypeReference<List<RepoFileVO>>() { });
    }

    /**
     * get blob file.
     * @param orgName org.
     * @param repoName repo.
     * @param sha sha.
     * @return file.
     */
    public String getBlobFile(String orgName, String repoName, String sha) {
        String url = String.format(config.getBlobTextTemplate(), orgName, repoName, sha, config.getToken());
        HttpGet httpGet = new HttpGet(url);
        String rawContent;
        try {
            CloseableHttpResponse response = client.execute(httpGet);
            rawContent = EntityUtils.toString(response.getEntity());
        } catch (ParseException | IOException e) {
            LOGGER.error("fail get tree, org: {}, repo: {}, sha: {}, cause: {}",
                    orgName, repoName, sha, e.getMessage());
            return null;
        }
        JsonNode res = ObjectMapperUtil.toJsonNode(rawContent);
        if (res == null) {
            return null;
        }
        JsonNode content = res.get("content");
        return content == null ? null : content.asText();
    }
}
