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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import com.easysoftwareinput.common.utils.HttpClientUtil;
import com.easysoftwareinput.common.utils.ObjectMapperUtil;
import com.easysoftwareinput.domain.appver.AppVersion;
import com.easysoftwareinput.infrastructure.appver.AppVerGatewayImpl;
import com.fasterxml.jackson.databind.JsonNode;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class AppVerService {
    /**
     * env.
     */
    @Autowired
    private Environment env;

    /**
     * gateway.
     */
    @Autowired
    private AppVerGatewayImpl gateway;

    /**
     * get iamges.
     * @param appUrl appurl.
     * @return a list of images.
     */
    private Set<String> getAppList(String appUrl) {
        Set<String> names = new HashSet<>();
        JsonNode resp = HttpClientUtil.getApiResponseJson(appUrl);
        if (resp == null) {
            return names;
        }

        JsonNode appList = resp.get("list");
        for (JsonNode app : appList) {
            names.add(app.get("name").asText());
        }
        return names;
    }

    /**
     * get result of monitor service.
     * @param name pkg name.
     * @param monUrl monurl.
     * @return a map.
     */
    private Map<String, JsonNode> getItems(String name, String monUrl) {
        name = name.replaceAll(" ", "%20");
        String url = String.format(monUrl, name);
        String response = HttpClientUtil.getHttpClient(url, null, null, null);
        if (StringUtils.isBlank(response)) {
            return Collections.emptyMap();
        }
        JsonNode info = ObjectMapperUtil.toJsonNode(response);
        if (info == null) {
            return Collections.emptyMap();
        }

        JsonNode items = info.get("items");
        if (items == null) {
            return Collections.emptyMap();
        }

        Map<String, JsonNode> tagMap = new HashMap<>();
        for (JsonNode item : items) {
            tagMap.put(item.get("tag").asText(), item);
        }
        return tagMap;
    }

    /**
     * set status of AppVerison.
     * @param appVer appVer.
     */
    private void setStatus(AppVersion appVer) {
        if (StringUtils.isBlank(appVer.getOpeneulerVersion())) {
            appVer.setStatus("MISSING");
        } else if (appVer.getOpeneulerVersion().equals(appVer.getUpstreamVersion())) {
            appVer.setStatus("OK");
        } else {
            appVer.setStatus("OUTDATED");
        }
    }

    /**
     * get rawversions of jsonNode.
     * @param euler jsonNode object.
     * @return raw versions.
     */
    private List<String> getRawVersions(JsonNode euler) {
        if (euler == null) {
            return Collections.emptyList();
        }

        JsonNode rawVers = euler.get("raw_versions");
        List<String> vers = new ArrayList<>();
        for (JsonNode v : rawVers) {
            vers.add(v.asText());
        }
        return vers;
    }

    /**
     * split string to os and version.
     * @param versionAndOs origin string.
     * @return a map.
     */
    public Map<String, String> splitVer(String versionAndOs) {
        String[] parts = versionAndOs.split("-");
        String version = parts[0];

        String rawOs = StringUtils.join(Arrays.copyOfRange(parts, 1, parts.length), "-");

        String os = "";
        if ("oe2203sp3".equals(rawOs)) {
            os = "openEuler-22.03-LTS-SP3";
        } else if ("22.03-lts".equals(rawOs)) {
            os = "openEuler-22.03-LTS";
        } else if ("20.03-lts-sp1".equals(rawOs)) {
            os = "openEuler-20.03-LTS-SP1";
        } else if ("oe2203lts".equals(rawOs)) {
            os = "openEuler-22.03-LTS";
        } else if ("oe2003sp1".equals(rawOs)) {
            os = "openEuler-20.03-LTS-SP1";
        } else {
            log.info("unrecognized os: " + rawOs);
        }
        return Map.ofEntries(
            Map.entry("osVer", os),
            Map.entry("pkgVer", version)
        );
    }

    /**
     * set appVer by osAndVer and euler.
     * @param appVer the object to be updated.
     * @param osAndVer osAndVer.
     * @param euler euler.
     */
    private void setByEuler(AppVersion appVer, Map<String, String> osAndVer, JsonNode euler) {
        appVer.setEulerOsVersion(osAndVer.get("osVer"));
        appVer.setOpeneulerVersion(osAndVer.get("pkgVer"));
        appVer.setEulerHomepage(euler.get("homepage").asText());
    }

    /**
     * generate appvers by rawversions of euler.
     * @param euler euler.
     * @return a list of appver.
     */
    private List<AppVersion> generateEntityByEuler(JsonNode euler) {
        List<String> rawVers = getRawVersions(euler);
        if (rawVers.size() == 0) {
            return Collections.emptyList();
        }

        List<AppVersion> appList = new ArrayList<>();
        for (String rawVer : rawVers) {
            AppVersion appVer = new AppVersion();
            Map<String, String> osAndVer = splitVer(rawVer);
            setByEuler(appVer, osAndVer, euler);
            appList.add(appVer);
        }
        return appList;
    }

    /**
     * fill appList with op.
     * @param appList apList to be updated.
     * @param up up.
     */
    private void fillWithUp(List<AppVersion> appList, JsonNode up) {
        if (up == null) {
            return;
        }

        String upVer = up.get("version").asText();
        upVer = upVer.replaceAll("_", ".");
        String upHome = up.get("homepage").asText();

        for (AppVersion app : appList) {
            app.setUpstreamVersion(upVer);
            app.setUpHomepage(upHome);
        }
    }

    /**
     * fill appList with ci.
     * @param appList applist to be updated.
     * @param ci ci.
     */
    private void fillWithCi(List<AppVersion> appList, JsonNode ci) {
        if (ci == null) {
            return;
        }

        String ciVer = ci.get("version").asText();
        for (AppVersion app : appList) {
            app.setCiVersion(ciVer);
        }
    }

    /**
     * fill aplist.
     * @param appList applist to be updated.
     * @param items itmes from monitor service.
     * @param name name of pkg.
     */
    private void fillUp(List<AppVersion> appList, Map<String, JsonNode> items, String name) {
        fillWithUp(appList, items.get("app_up"));
        fillWithCi(appList, items.get("app_ci_openeuler"));

        for (AppVersion app : appList) {
            setStatus(app);
            app.setName(name);
        }
    }

    /**
     * generate appvers by names.
     * @param names names of pkgs.
     * @param monUrl monurl.
     * @return a list of appver.
     */
    private List<AppVersion> generateAppVerList(Set<String> names, String monUrl) {
        List<AppVersion> verList = new ArrayList<>();
        for (String name : names) {
            Map<String, JsonNode> items = getItems(name, monUrl);
            List<AppVersion> appList = generateEntityByEuler(items.get("app_openeuler"));
            fillUp(appList, items, name);
            verList.addAll(appList);
        }
        return verList;
    }

    /**
     * run program.
     */
    public void run() {
        String appUrl = env.getProperty("appver.appurl");
        String monUrl = env.getProperty("appver.monurl");

        Set<String> names = getAppList(appUrl);
        List<AppVersion> verList = generateAppVerList(names, monUrl);
        gateway.saveAll(verList);
    }
}
