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

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import com.easysoftwareinput.common.utils.HttpClientUtil;
import com.easysoftwareinput.common.utils.ObjectMapperUtil;
import com.easysoftwareinput.domain.appver.AppVersion;
import com.easysoftwareinput.infrastructure.appver.AppVerGatewayImpl;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

@Component
public class RpmVerService {
    /**
     * logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(RpmVerService.class);

    /**
     * env.
     */
    @Autowired
    private Environment env;

    /**
     * appverservice.
     */
    @Autowired
    private AppVerService appService;

    /**
     * gateway.
     */
    @Autowired
    private AppVerGatewayImpl gateway;

    /**
     * run the program.
     */
    public void run() {
        String rpmTxt = env.getProperty("appver.rpm-txt");
        String monUrl = env.getProperty("appver.monurl");
        String rpmEulerUrl = env.getProperty("appver.rpm-euler");
        if (rpmTxt == null || monUrl == null || rpmEulerUrl == null) {
            LOGGER.error("no env");
            return;
        }

        List<String> pkgList = readFileByLine(rpmTxt);
        Set<String> pkgs = validPkgs(pkgList);

        List<AppVersion> vList = new ArrayList<>();
        for (String name : pkgs) {
            AppVersion v = handleEachPkg(name, monUrl, rpmEulerUrl);
            vList.add(v);
        }

        gateway.saveAll(appService.filter(vList));
        LOGGER.info("finish-rpm-version");
    }

    /**
     * Generate AppVersion for each pkg.
     * @param name pkg name.
     * @param monUrl monnitor url.
     * @param rpmEulerUrl rpm euler url.
     * @return AppVersion.
     */
    private AppVersion handleEachPkg(String name, String monUrl, String rpmEulerUrl) {
        Map<String, JsonNode> items = appService.getItems(name, monUrl);
        Map<String, String> euler = getEulerVersion(name, rpmEulerUrl);

        AppVersion v = new AppVersion();
        v.setName(name);
        v.setType("rpm");
        fillWithEuler(v, euler);
        fillWithUpCi(v, items);
        return v;
    }

    /**
     * fill AppVersion with upstream.
     * @param v AppVersion pkg.
     * @param items upstream message.
     */
    public void fillWithUpCi(AppVersion v, Map<String, JsonNode> items) {
        appService.fillWithUp(v, items.get("app_up"));
        appService.fillWithCi(v, items.get("app_ci_openeuler"));
        appService.setStatus(v);
    }

    /**
     * fill AppVersion with euler.
     * @param v AppVersion pkg.
     * @param euler euler message.
     */
    public void fillWithEuler(AppVersion v, Map<String, String> euler) {
        v.setEulerOsVersion(euler.get("os"));
        v.setOpeneulerVersion(euler.get("ver"));
        setEulerVersion(v, euler.get("ver"));
        v.setEulerHomepage(""); // none
    }

    /**
     * set OpeneulerVersion of pkg.
     * @param v pkg.
     * @param ver openEulerverison.
     */
    public void setEulerVersion(AppVersion v, String ver) {
        if (StringUtils.isBlank(ver)) {
            return;
        }

        String[] splits = ver.split("-");
        if (splits == null || splits.length == 0) {
            return;
        }

        v.setOpeneulerVersion(splits[0]);
    }

    /**
     * get euler version of each pkg.
     * @param name pgk name.
     * @param monUrl monitor url.
     * @return euler version.
     */
    public Map<String, String> getEulerVersion(String name, String monUrl) {
        name = name.replaceAll(" ", "%20");
        String url = String.format(monUrl, name);
        try {
            String response = HttpClientUtil.getHttpClient(url, null, null, null);
            JsonNode info = ObjectMapperUtil.toJsonNode(response);
            JsonNode data = info.get("data");
            ArrayNode list = (ArrayNode) data.get("list");
            JsonNode l = list.get(0);
            String ver = l.get("newestVersion").asText();
            String os = l.get("os").asText();
            return Map.ofEntries(
                Map.entry("ver", StringUtils.trimToEmpty(ver)),
                Map.entry("os", StringUtils.trimToEmpty(os))
            );
        } catch (Exception e) {
            LOGGER.error("fail-to-get-euler-verison, url: {}", url);
            return Collections.emptyMap();
        }
    }

    /**
     * filter the pkgs which is empty.
     * @param pkgs origin pkgs.
     * @return filtered pkgs.
     */
    public Set<String> validPkgs(List<String> pkgs) {
        Set<String> res = new HashSet<>(pkgs);
        return res.stream().filter(p -> !StringUtils.isBlank(p)).collect(Collectors.toSet());
    }

    /**
     * read data from file by line.
     * @param fileName filename.
     * @return list of string.
     */
    public List<String> readFileByLine(String fileName) {
        List<String> res = new ArrayList<>();
        try (BufferedReader r = new BufferedReader(new FileReader(fileName, StandardCharsets.UTF_8))) {
            String line;
            while ((line = r.readLine()) != null) {
                res.add(StringUtils.trimToEmpty(line));
            }
        } catch (IOException e) {
            LOGGER.error("error-reading-file: {}", fileName);
        }
        return res;
    }
}
