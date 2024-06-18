/* Copyright (c) 2024 openEuler Community
 EasySoftwareInput is licensed under the Mulan PSL v2.
 You can use this software according to the terms and conditions of the Mulan PSL v2.
 You may obtain a copy of Mulan PSL v2 at:
     http://license.coscl.org.cn/MulanPSL2
 THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
 EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
 MERCHANTABILITY OR FIT FOR A PARTICULAR PURPOSE.
 See the Mulan PSL v2 for more details.
*/

package com.easysoftwareinput.application.rpmpackage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import com.easysoftwareinput.common.entity.MessageCode;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class HttpService {
    /**
     * resttemplate.
     */
    @Autowired
    private RestTemplate restTemplate;

    /**
     * http headers.
     */
    private HttpHeaders headers = null;

    /**
     * init the header.
     */
    @PostConstruct
    public void init() {
        headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
    }

    /**
     * whether the url is valid.
     * @param url url.
     * @return whether the url is valid.
     */
    public boolean validUrl(String url) {
        RestTemplate restTemplate = new RestTemplate();
        try {
            restTemplate.getForObject(url, String.class);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * post the pkg.
     * @param jsonPkg pkg.
     * @param postUrl url.
     */
    @Retryable(value = Exception.class, maxAttempts = 3, backoff = @Backoff(delay = 2000, multiplier = 1.5))
    public synchronized void postPkg(String jsonPkg, String postUrl) {
        // log.info("body: {}", jsonPkg);
        HttpEntity<String> request = new HttpEntity<>(jsonPkg, headers);
        String res = restTemplate.postForObject(postUrl, request, String.class);
        // restTemplate.put(postUrl, request);
        log.info("post-res: {}", res);
    }

    /**
     * log if the method "postPkg" failed.
     * @param e exception.
     * @param jsonPkg pkg.
     * @param postUrl url.
     */
    @Recover
    public void recover(Exception e, String jsonPkg, String postUrl) {
        log.error(MessageCode.EC0001.getMsgEn(), e);
        log.error("Failed post! pkg: {}", jsonPkg);
    }
}
