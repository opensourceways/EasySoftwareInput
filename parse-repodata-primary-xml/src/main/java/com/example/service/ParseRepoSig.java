package com.example.service;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.example.entity.po.MessageCode;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.power.common.util.StringUtil;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ParseRepoSig {
    @Value("${repofilename}")
    String repoFilename;

    @Value("${repo.sig.url}")
    String sigUrl;

    @Value("${repo.sig.filepath}")
    String sigFilepath;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    RestTemplate restTemplate;

    @Autowired
    ParseRepoType parseRepoType;

    static Map<String, String> cateMap = Map.ofEntries(
        Map.entry("sig-CloudNative", "云服务"),
        Map.entry("AI", "AI"),
        Map.entry("storage", "分布式存储"),
        Map.entry("sig-HPC", "HPC"),
        Map.entry("DB", "数据库"),
        Map.entry("bigdata", "大数据"),
        Map.entry("other", "其他")
    );

    public void run() {
        List<Map<String, String>> res = new ArrayList<>();
        List<String> repos = parseRepoType.getRepos();
        for (String repo : repos) {
            repo = repo.replace("src-openeuler/", "");
            String sig = fetchSig(repo);

            String cate = specifyCategory(sig);

            res.add(Map.ofEntries(
                Map.entry("repoName", repo),
                Map.entry("sigName", sig),
                Map.entry("category", cate)
            ));
        }

        writeToFile(res);
    }

    public void writeToFile(List<Map<String, String>> data) {
        String str = "";
        try {
            str = objectMapper.writeValueAsString(data);
        } catch (JsonProcessingException e) {
            log.error(MessageCode.EC00014.getMsgEn(), e);
        }

        try (Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(sigFilepath), "UTF-8"))) {
            writer.write(str);
        } catch (IOException e) {
            log.error(MessageCode.EC00014.getMsgEn(), e);
        }


    }


    public String specifyCategory(String sig) {
        if (cateMap.keySet().contains(sig)) {
            return cateMap.get(sig);
        } else {
            return cateMap.get("other");
        }
    }

    public String fetchSig(String repoName) {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            log.error(MessageCode.EC00015.getMsgEn(), e);
        }
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(sigUrl)
                .queryParam("repo", repoName)
                .queryParam("community", "openeuler");

        String url = builder.build().toString();

        String response = restTemplate.getForObject(url, String.class);
        
        String sig = "";
        try {
            Map<String, String> map = objectMapper.readValue(response, new TypeReference<Map<String, String>>(){});
            sig = map.get("data");
        } catch (Exception e) {
            log.error(MessageCode.EC00016.getMsgEn(), e);
        }
        return sig;
    }
}
