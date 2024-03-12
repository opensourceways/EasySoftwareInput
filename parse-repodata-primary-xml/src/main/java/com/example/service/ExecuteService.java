package com.example.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.example.entity.po.RPMPackage;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ExecuteService {
    @Autowired
    ObjectMapper objectMapper;

    @Value("${post.url}")
    private String postUrl;

    
    public void insertRPMPackage(RPMPackage pkg) {
        String jsonPkg = "";
        try {
            jsonPkg= objectMapper.writeValueAsString(pkg);
        } catch (JsonProcessingException e) {
        }

        log.info(jsonPkg);

        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> request = new HttpEntity<>(jsonPkg, headers);

        try {
            restTemplate.postForObject(postUrl, request, String.class);
        } catch (Exception e) {
            
            log.error(pkg.getName());
        }
    }
}
