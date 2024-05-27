package com.easysoftwareinput.application.externalos;

import java.io.UnsupportedEncodingException;
import java.net.http.HttpClient;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.easysoftwareinput.common.utils.HttpClientUtil;
import com.easysoftwareinput.common.utils.ObjectMapperUtil;
import com.easysoftwareinput.common.utils.YamlUtil;
import com.easysoftwareinput.domain.externalos.ability.ExternalOsConverter;
import com.easysoftwareinput.domain.externalos.model.ExternalOs;

@Service
public class ExternalOsService {
    private static final Logger logger = LoggerFactory.getLogger(ExternalOsService.class);

    @Value("${externalos.path}")
    private String path;

    @Value("${externalos.url}")
    private String postUrl;
    
    public void run() {
        Map<String, Object> map = YamlUtil.parseYaml(path);
        List<ExternalOs> exOsList = ExternalOsConverter.toEntityList(map);
        post(exOsList, postUrl);
        logger.info("finish-external-os");

    }

    private void post(List<ExternalOs> exOsList, String url) {
        for (ExternalOs exOs : exOsList) {
            String body = ObjectMapperUtil.writeValueAsString(exOs);
            HttpClientUtil.postApp(url, body);
        }
    }



}
