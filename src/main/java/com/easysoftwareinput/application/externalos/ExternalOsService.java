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

package com.easysoftwareinput.application.externalos;

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
    /**
     * logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(ExternalOsService.class);

    /**
     * path of file.
     */
    @Value("${externalos.path}")
    private String path;

    /**
     * post url.
     */
    @Value("${externalos.url}")
    private String postUrl;

    /**
     * run the program.
     */
    public void run() {
        Map<String, Object> map = YamlUtil.parseYaml(path);
        List<ExternalOs> exOsList = ExternalOsConverter.toEntityList(map);
        post(exOsList, postUrl);
        LOGGER.info("finished external os");

    }

    /**
     * post to url.
     * @param exOsList list to be posted.
     * @param url url.
     */
    private void post(List<ExternalOs> exOsList, String url) {
        for (ExternalOs exOs : exOsList) {
            String body = ObjectMapperUtil.writeValueAsString(exOs);
            HttpClientUtil.postApp(url, body);
        }
    }



}
