package com.easysoftwareinput.application.rpmpackage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.easysoftwareinput.common.entity.MessageCode;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class HttpService {

    @Value("${rpm.post.url}")
    String postUrl;

    public boolean validUrl(String url) {
        RestTemplate restTemplate = new RestTemplate();
        while (true) {
            String result = "";
            try {
                result = restTemplate.getForObject(url, String.class);
                return true;
            } catch (Exception e) {
                return false;
            }
        }
    }
    
    public <T> void postPkg(T t) {
        RestTemplate restTemplate = new RestTemplate();
        ObjectMapper objectMapper = new ObjectMapper();
        String jsonPkg = "";
        try {
            jsonPkg= objectMapper.writeValueAsString(t);
        } catch (JsonProcessingException e) {
            log.error(MessageCode.EC00014.getMsgEn(), e);
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> request = new HttpEntity<>(jsonPkg, headers);

        try {
            restTemplate.postForObject(postUrl, request, String.class);
        } catch (Exception e) {
            log.error(MessageCode.EC0001.getMsgEn(), e);
            log.error(jsonPkg, e);
        }
    }
}
