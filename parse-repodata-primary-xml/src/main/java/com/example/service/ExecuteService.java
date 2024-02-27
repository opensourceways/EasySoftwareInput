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

@Service
public class ExecuteService {
    @Autowired
    ObjectMapper objectMapper;

    @Value("${post.url}")
    private String postUrl;

    
    public String insertRPMPackage(RPMPackage pkg) {
        String jsonPkg = "";
        try {
            jsonPkg= objectMapper.writeValueAsString(pkg);
        } catch (JsonProcessingException e) {
        }

        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> request = new HttpEntity<>(jsonPkg, headers);


        return restTemplate.postForObject(postUrl, request, String.class);
    }
}
