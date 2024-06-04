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

import com.easysoftwareinput.application.rpmpackage.PkgService;
import com.easysoftwareinput.domain.epkgpackage.ability.EPKGPackageConverter;
import com.easysoftwareinput.domain.epkgpackage.model.EPKGPackage;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class EPKGAsyncService {
    /**
     * logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(EPKGAsyncService.class);

    /**
     * pkgservice.
     */
    @Autowired
    private PkgService pkgService;

    /**
     * epkg pkg converter.
     */
    @Autowired
    private EPKGPackageConverter epkgPackageConverter;

    /**
     * execute parse epkg xml by multi thread.
     * @param e epkg.
     * @param osMes os.
     * @param i file index.
     * @param postUrl posturl.
     * @param srcMap src pkg name and url.
     */
    @Async("epkgasyncServiceExecutor")
    public void executeAsync(Element e, Map<String, String> osMes, int i, String postUrl, Map<String, String> srcMap) {
        LOGGER.info("thread name: {},  index: {}", Thread.currentThread().getName(), i);
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
            LOGGER.info("can not tojson, pkg: {}", ePkg);
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> request = new HttpEntity<>(body, headers);
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.postForObject(postUrl, request, String.class);
    }
}
