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
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import com.easysoftwareinput.common.utils.HttpClientUtil;
import com.easysoftwareinput.common.utils.ObjectMapperUtil;
import com.easysoftwareinput.domain.apppackage.model.OsTagMonitorAlias;
import com.easysoftwareinput.domain.apppackage.model.OsTagMonitorAliasConfig;
import com.easysoftwareinput.domain.appver.AppVersion;
import com.easysoftwareinput.infrastructure.appver.AppVerGatewayImpl;
import com.fasterxml.jackson.databind.JsonNode;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class AppVerService {
    /**
     * logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(AppVerService.class);

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
     * gateway.
     */
    @Autowired
    private OsTagMonitorAliasConfig osTagConfig;


    /**
     * alias map.
     */
    private Map<String, String> aliasMap = new HashMap<>();


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
    public Map<String, JsonNode> getItems(String name, String monUrl) {
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
    public void setStatus(AppVersion appVer) {
        if (appVer == null) {
            return;
        }

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

        if (aliasMap.isEmpty()) {
            initAliasMap();
        }

        String rawOs = parts[parts.length - 1];
        if ("lts".equals(rawOs)) {
            rawOs = parts[parts.length - 2] + "-" + rawOs;
        }

        String os = "";
        if (aliasMap.containsKey(rawOs)) {
            os = aliasMap.get(rawOs);
        }

        if (StringUtils.isBlank(os)) {
            log.info("unrecognized os: " + rawOs);
        }

        return Map.ofEntries(
            Map.entry("osVer", os),
            Map.entry("pkgVer", version)
        );
    }

    /**
     * init alias map.
     */
    public void initAliasMap() {
        List<OsTagMonitorAlias> aliasList = osTagConfig.getAppMonitorOsAliasList();
        for (OsTagMonitorAlias alias : aliasList) {
            String monitorOsTag = alias.getMonitorOsTag();
            String eulerTag = alias.getEulerTag();
            if (StringUtils.isBlank(monitorOsTag) || StringUtils.isBlank(eulerTag)) {
                continue;
            }
            aliasMap.put(monitorOsTag, eulerTag);
        }
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
     * fill app with op.
     * @param app apList to be updated.
     * @param up up.
     */
    public void fillWithUp(AppVersion app, JsonNode up) {
        if (up == null) {
            return;
        }

        String upHome = up.get("homepage").asText();
        app.setUpHomepage(upHome);
        setPkgUpStreamVersion(app, up.get("version"));
    }

    /**
     * set upstreamversion of pkg.
     * @param app pkg.
     * @param v jsonnode.
     */
    public void setPkgUpStreamVersion(AppVersion app, JsonNode v) {
        if (v == null) {
            return;
        }

        String upVer = v.asText();
        if (StringUtils.isBlank(upVer)) {
            return;
        }

        upVer = upVer.replaceAll("_", ".");
        String[] splits = upVer.split("-");
        if (splits == null || splits.length == 0) {
            return;
        } else if (splits.length == 1) {
            app.setUpstreamVersion(splits[0]);
        } else {
            app.setUpstreamVersion(pickPiece(splits));
        }
    }

    /**
     * pick the best string from string array.
     * @param splits string array.
     * @return the best string.
     */
    public String pickPiece(String[] splits) {
        int max = -1;
        String winner = "";
        for (String s : splits) {
            int count = calculateString(s);
            if (count > max) {
                max = count;
                winner = s;
            }
        }
        return winner;
    }

    /**
     * calculate the number of digit or `.` in the string.
     * @param s string.
     * @return the number.
     */
    public int calculateString(String s) {
        if (StringUtils.isBlank(s)) {
            return 0;
        }
        int count = 0;
        for (int i = 0; i < s.length(); i++) {
            if (Character.isDigit(s.charAt(i)) || '.' == s.charAt(i)) {
                count++;
            }
        }
        return count;
    }

    /**
     * fill appList with ci.
     * @param app applist to be updated.
     * @param ci ci.
     */
    public void fillWithCi(AppVersion app, JsonNode ci) {
        if (ci == null) {
            return;
        }

        String ciVer = ci.get("version").asText();
        app.setCiVersion(ciVer);
    }

    /**
     * fill aplist.
     * @param appList applist to be updated.
     * @param items itmes from monitor service.
     * @param name name of pkg.
     */
    private void fillUp(List<AppVersion> appList, Map<String, JsonNode> items, String name) {
        for (AppVersion a : appList) {
            fillWithUp(a, items.get("app_up"));
            fillWithCi(a, items.get("app_ci_openeuler"));
            setStatus(a);
            a.setName(name);
            a.setType("image");
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
     * whether the AppVersion is empty or not.
     * @param v AppVersion.
     * @return boolean.
     */
    public boolean validEmptyPkg(AppVersion v) {
        if (StringUtils.isBlank(v.getUpstreamVersion()) && StringUtils.isBlank(v.getUpHomepage())
                && StringUtils.isBlank(v.getCiVersion())
                && StringUtils.isBlank(v.getEulerOsVersion()) && StringUtils.isBlank(v.getOpeneulerVersion())
                && StringUtils.isBlank(v.getEulerHomepage())) {
            return true;
        }
        return false;
    }

    /**
     * filter the pkg.
     * @param originList origin pkg.
     * @return filtered pkg.
     */
    public List<AppVersion> filter(List<AppVersion> originList) {
        List<AppVersion> vList = new ArrayList<>();
        for (AppVersion v : originList) {
            if (validEmptyPkg(v)) {
                continue;
            }

            if (StringUtils.isBlank(v.getUpstreamVersion())) {
                continue;
            }
            vList.add(v);
        }
        return vList;
    }

    /**
     * run program.
     */
    public void run() {
        String appUrl = env.getProperty("appver.appurl");
        String monUrl = env.getProperty("appver.monurl");

        Set<String> names = getAppList(appUrl);
        List<AppVersion> verList = generateAppVerList(names, monUrl);
        filter(verList);
        gateway.saveAll(verList);
        LOGGER.info("finish-app-ver");
    }
}
