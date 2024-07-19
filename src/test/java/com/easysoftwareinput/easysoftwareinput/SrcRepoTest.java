package com.easysoftwareinput.easysoftwareinput;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import com.easysoftwareinput.infrastructure.rpmpkg.RpmGatewayImpl;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

@SpringBootTest(classes  = {EasysoftwareinputApplication.class})
public class SrcRepoTest {
    @Autowired
    private RpmGatewayImpl rpmGateway;

    /**
     * 测试rpm数据表中srcdownloadUrl字段的软件包包名是否存在于`https://gitee.com/src-openeuler`
     * @throws IOException
     */
    @Test
    public void test_repo_of_rpm() throws IOException {
        Set<String> urls = rpmGateway.getSrcDownloadUrls();

        Set<String> nameSet = getRemoteRepos();
        Set<String> unIncludedName = new HashSet<>();
        for (String url : urls) {
            String name = getName(url);
            if (!nameSet.contains(name)) {
                unIncludedName.add(name);
            }
        }

        String fileName = "";
        writeText(fileName, unIncludedName);
        return;
    }

    public void writeText(String fileName, Set<String> set) {
        try (BufferedWriter bfw = new BufferedWriter(new FileWriter(fileName))) {
            for (String item : set) {
                bfw.write(item);
                bfw.newLine();
                bfw.flush();
            }
        } catch (IOException e) {

        }
    }

    public Set<String> getRemoteRepos() {
        String token = "";
        int perPage = 100;
        ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(30);
        List<CompletableFuture<List<String>>> taskList = new ArrayList<>();
        for (int page = 1; page <= 107; page ++) {
            String url = String.format("https://gitee.com/api/v5/orgs/src-openeuler/repos?access_token=%s&type=all&page=%s&per_page=%s", token, page, perPage);
            while (executor.getQueue().size() > 3) {
            }
            CompletableFuture<List<String>> task = CompletableFuture.supplyAsync(
                () -> getListFromRemote(url), executor);
            taskList.add(task);
        }

        CompletableFuture.allOf(taskList.toArray(new CompletableFuture[0])).join();
        Set<String> nameList = new HashSet<>();
        for (CompletableFuture<List<String>> task : taskList) {
            try {
                List<String> curList = task.get();
                nameList.addAll(curList);
            } catch (Exception e) {
                System.out.println();
            }
        }

        return nameList;
    }

    public List<String> getListFromRemote(String url) {
        RestTemplate rest = new RestTemplate();
        List<String> nameList = new ArrayList<>();
        ResponseEntity<JsonNode> res = rest.getForEntity(url, JsonNode.class);
        ArrayNode array = (ArrayNode) res.getBody();
        if (array.size() == 0) {
            return Collections.emptyList();
        }
        for (JsonNode repo : array) {
            String name = repo.get("name").asText();
            nameList.add(name);
        }
        return nameList;
    }

    public String getName(String url) {
        String[] splits = url.split("/");
        if (splits == null || splits.length == 0) {
            return null;
        }
        String pkgName = splits[splits.length - 1];
        if (StringUtils.isBlank(pkgName)) {
            return null;
        }
        splits = pkgName.split("-");
        if (splits == null || splits.length < 2) {
            return null;
        }
        String[] names = Arrays.copyOfRange(splits, 0, splits.length - 2);
        if (names == null || names.length == 0) {
            return null;
        }
        return StringUtils.join(names, "-");
    }
}
