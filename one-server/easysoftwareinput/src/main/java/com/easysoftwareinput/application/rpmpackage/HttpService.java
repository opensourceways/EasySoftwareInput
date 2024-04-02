package com.easysoftwareinput.application.rpmpackage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.easysoftwareinput.common.entity.MessageCode;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class HttpService {
    @Autowired
    RestTemplate restTemplate;

    private HttpHeaders headers = null;

    @PostConstruct
    public void init() {
        headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
    }

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
    
    @Retryable(value = Exception.class,maxAttempts = 3,backoff = @Backoff(delay = 2000,multiplier = 1.5))
    public void postPkg(String jsonPkg, String postUrl) {
        HttpEntity<String> request = new HttpEntity<>(jsonPkg, headers);
        restTemplate.postForObject(postUrl, request, String.class);
        log.info("succeed post! thread: {}, pkg: {}",Thread.currentThread().getName(), jsonPkg);
    }

    @Recover
    public void recover(Exception e, String jsonPkg, String postUrl) {
        log.error(MessageCode.EC0001.getMsgEn(), e);
        log.error("Failed post! pkg: {}", jsonPkg);
    }
        
    
}
