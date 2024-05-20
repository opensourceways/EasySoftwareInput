package com.easysoftwareinput.application.epkgpackage;

import java.util.Map;

import org.dom4j.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.easysoftwareinput.application.rpmpackage.HttpService;
import com.easysoftwareinput.application.rpmpackage.PkgService;
import com.easysoftwareinput.domain.epkgpackage.ability.EPKGPackageConverter;
import com.easysoftwareinput.domain.epkgpackage.model.EPKGPackage;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class EPKGAsyncService {
    private static final Logger logger = LoggerFactory.getLogger(EPKGAsyncService.class);

    @Autowired
    PkgService pkgService;

   
    @Autowired
    HttpService httpService;

    @Autowired
    EPKGPackageConverter epkgPackageConverter;

    @Autowired
    ObjectMapper objectMapper;

    @Async("epkgasyncServiceExecutor")
    public void executeAsync(Element e, Map<String, String> osMes, int i, String postUrl, Map<String, String> srcMap) {
        logger.info("thread name: {},  index: {}", Thread.currentThread().getName(), i);
        Map<String, String> res = pkgService.parsePkg(e, osMes);

        EPKGPackage ePkg = epkgPackageConverter.toEntity(res, srcMap);

        // 不添加源码包
        if ("src".equals(ePkg.getArch())) {
            return;
        }

        String body = "";
        ObjectMapper ma = new ObjectMapper();
        try {
            body = ma.writeValueAsString(ePkg);
        } catch (Exception ex) {
            log.info("can not tojson, pkg: {}", ePkg);
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> request = new HttpEntity<>(body, headers);
        RestTemplate restTemplate = new RestTemplate();
        String rr = restTemplate.postForObject(postUrl, request, String.class);
        // System.out.println(body);System.out.println(postUrl);System.exit(0);
        // // httpService.postPkg(body, postUrl);
        // // System.exit(0);
    }
}
