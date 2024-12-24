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

package com.easysoftwareinput.application.appver;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.easysoftwareinput.common.utils.ObjectMapperUtil;
import com.easysoftwareinput.domain.appver.AppVerConfig;
import com.easysoftwareinput.domain.appver.FedoraMonitorVO;
import com.fasterxml.jackson.databind.JsonNode;

@Component
public class FedoraMonitorService {
    /**
     * rpmver monitor alias service.
     */
    @Autowired
    private RpmVerMonitorAliasService rpmVerMonitorAliasService;

    /**
     * logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(FedoraMonitorService.class);

    /**
     * priority map.
     */
    private static Map<String, Integer> priorityMap = Map.of("github", 0, "gitlab", 1);

    /**
     * pkg name prefix.
     */
    private static List<String> pkgNamePrefixList = List.of("rubygem-", "qt6-", "qt5-", "qt-",
            "python-", "python3-");

    /**
     * appver config.
     */
    @Autowired
    private AppVerConfig config;

    /**
     * client.
     */
    @Autowired
    private CloseableHttpClient client;

    /**
     * get upstream.
     * @param pkgName pkg name.
     * @return FedoraMonitorVO.
     */
    public FedoraMonitorVO getUpstream(String pkgName) {
        List<String> possibleNames = new ArrayList<>();
        possibleNames.add(pkgName);

        for (String prefix : pkgNamePrefixList) {
            if (pkgName.startsWith(prefix)) {
                String pkgName2 = pkgName.replace(prefix, "");
                possibleNames.add(pkgName2);
            }
        }

        char lastChar = pkgName.charAt(pkgName.length() - 1);
        if (Character.isDigit(lastChar)) {
            String pkgName3 = pkgName.substring(0, pkgName.length() - 1);
            possibleNames.add(pkgName3);
        }

        for (String name : possibleNames) {
            String monitorName = rpmVerMonitorAliasService.getMonitorName(name);
            JsonNode items = getResponseFromApiByName(monitorName);
            List<FedoraMonitorVO> monitorList = extractItemsToFediraMonitorVO(items);
            FedoraMonitorVO monitor = pickOneMonitor(monitorList, pkgName);
            if (monitor != null) {
                return monitor;
            }
        }
        return null;
    }

    /**
     * get response from api by name.
     * @param pkgName pkg name.
     * @return JsonNode.
     */
    public JsonNode getResponseFromApiByName(String pkgName) {
        String url = String.format(config.getFedoraMonitorUrl(), pkgName);
        HttpGet httpGet = new HttpGet(url);
        httpGet.setHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) "
                + "AppleWebKit/537.36 (KHTML, like Gecko) Chrome/85.0.4183.121 Safari/537.36");
        String rawContext;
        try {
            CloseableHttpResponse response = client.execute(httpGet);
            rawContext = EntityUtils.toString(response.getEntity());
        } catch (IOException e) {
            LOGGER.error("fail get url: {}, cause: {}", url, e.getMessage());
            return null;
        }
        JsonNode info = ObjectMapperUtil.toJsonNode(rawContext);
        if (info == null) {
            return null;
        }
        return info.get("items");
    }

    /**
     * pick one monitor.
     * @param monitorList monitor list.
     * @param pkgName pkg name.
     * @return monitor.
     */
    public FedoraMonitorVO pickOneMonitor(List<FedoraMonitorVO> monitorList, String pkgName) {
        if (monitorList.size() == 0) {
            return null;
        } else if (monitorList.size() == 1) {
            return monitorList.get(0);
        }

        List<FedoraMonitorVO> sortedList = monitorList.stream().sorted(Comparator.comparingInt(
            monitor -> priorityMap.getOrDefault(StringUtils.lowerCase(monitor.getBackend()), Integer.MAX_VALUE)
        )).collect(Collectors.toList());
        return sortedList.get(0);
    }

    /**
     * extract items to FedoraMonitorVO.
     * @param items items.
     * @return FedoraMonitorVO.
     */
    public List<FedoraMonitorVO> extractItemsToFediraMonitorVO(JsonNode items) {
        if (items == null) {
            return Collections.emptyList();
        }

        List<FedoraMonitorVO> monitorList = new ArrayList<>();
        for (JsonNode item : items) {
            JsonNode backend = item.get("backend");
            JsonNode stableVersions = item.get("stable_versions");
            FedoraMonitorVO fedoraMonitorVO = new FedoraMonitorVO();
            if (backend != null && stableVersions != null && stableVersions.get(0) != null) {
                fedoraMonitorVO.setBackend(backend.asText());
                fedoraMonitorVO.setFirstStableVersion(stableVersions.get(0).asText());
                monitorList.add(fedoraMonitorVO);
            }
        }
        return monitorList;
    }
}
