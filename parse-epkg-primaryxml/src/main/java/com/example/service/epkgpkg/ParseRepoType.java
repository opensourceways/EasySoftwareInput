package com.example.service.epkgpkg;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ParseRepoType {
    @Autowired
    ObjectMapper objectMapper;

    @Value("${token}")
    String token;

    @Value("${repofilename}")
    String repoFilename;

    public void run() {
        List<String> res = fetchRepoNames();
        // String s = toString(res);
        // writeToFile(s);
    }

    private List<String> repos = new ArrayList<>();

    public List<String> getRepos() {
        return repos;
    }

    @PostConstruct
    public void loadFile() {
        try (BufferedReader reader = new BufferedReader(new FileReader(repoFilename))) {
            String line;
            while ((line = reader.readLine()) != null) {
                repos.add(line);
            }
        } catch (IOException e) {
        }
    }

    public void writeToFile(String srcUrl) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(repoFilename, true))) {
            writer.write(srcUrl + "\n");
        } catch (IOException e) {
            System.err.println("An error occurred: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private String toString (List<String> list) {
        StringJoiner stringJoiner = new StringJoiner("\n");
        for (String str : list) {
            stringJoiner.add(str);
        }
        return stringJoiner.toString();
    }

    private List<String> fetchRepoNames() {
        RestTemplate restTemplate = new RestTemplate();
        int pageNum = 1;
        List<String> res = new ArrayList<>();

        while (true) {
            List<String> repos = new ArrayList<>();
            log.info("current pageNum: {}", pageNum);
            String url = String.format("https://gitee.com/api/v5/orgs/src-openeuler/repos?" + 
                    "access_token=%s&type=all&page=%d&per_page=100", token, pageNum);
            String result = restTemplate.getForObject(url, String.class);

            List<Object> list = new ArrayList<>();
            try {
                list = objectMapper.readValue(result, List.class);
            } catch (JsonProcessingException e) {
                log.error("", e);
            }
            for (Object obj: list) {
                Map<String, Object> repo = (Map<String, Object>) obj;
                String name = (String) repo.get("full_name");
                repos.add(name);
            }

            String s = toString(repos);
            writeToFile(s);

            if (list.size() < 100) {
                break;
            }
            pageNum++;
        }

        return res;
    }
}
