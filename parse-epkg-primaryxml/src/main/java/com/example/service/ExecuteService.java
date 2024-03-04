package com.example.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.example.entity.po.EPKGPackage;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ExecuteService {
    @Autowired
    ObjectMapper objectMapper;

    public void insertEPKGPackage(EPKGPackage pkg, String postUrl) {
        String jsonPkg = "";
        try {
            jsonPkg= objectMapper.writeValueAsString(pkg);
        } catch (JsonProcessingException e) {
        }

        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> request = new HttpEntity<>(jsonPkg, headers);

        try {
            restTemplate.postForObject(postUrl, request, String.class);
        } catch (Exception e) {
            log.error(pkg.getName(), e);
        }
    }
}
