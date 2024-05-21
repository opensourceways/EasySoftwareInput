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
import org.bouncycastle.jcajce.provider.digest.GOST3411.HashMac;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import com.easysoftwareinput.common.utils.HttpClientUtil;
import com.easysoftwareinput.common.utils.ObjectMapperUtil;
import com.easysoftwareinput.domain.apppackage.model.AppPackage;
import com.easysoftwareinput.domain.appver.AppVersion;
import com.fasterxml.jackson.databind.JsonNode;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class AppVerService {
    @Autowired
    Environment env;

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

    private void setStatus(AppVersion appVer) {
        if (StringUtils.isBlank(appVer.getOpeneulerVersion())) {
            appVer.setStatus("MISSING");
        } else if (appVer.getOpeneulerVersion().equals(appVer.getUpstreamVersion())) {
            appVer.setStatus("OK");
        } else {
            appVer.setStatus("OUTDATED");
        }
    }

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
            os = "openEuler-22.03-LTS-SP1";
        } else if ("oe2203lts".equals(rawOs)) {
            os = "openEuler-22.03-LTS";
        } else {
            log.info("unrecognized os: " + rawOs);
        }
        return Map.ofEntries(
            Map.entry("osVer", os),
            Map.entry("pkgVer", version)
        );
    }

    private void setByEuler(AppVersion appVer, Map<String, String> osAndVer, JsonNode euler) {
        appVer.setEulerOsVersion(osAndVer.get("osVer"));
        appVer.setOpeneulerVersion(osAndVer.get("pkgVer"));
        appVer.setEulerHomepage(euler.get("homepage").asText());
    }

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

    private void fillWithCi(List<AppVersion> appList, JsonNode ci) {
        if (ci == null) {
            return;
        }

        String ciVer = ci.get("version").asText();
        for (AppVersion app : appList) {
            app.setCiVersion(ciVer);
        }
    }

    private void fillUp(List<AppVersion> appList, Map<String, JsonNode> items, String name) {
        fillWithUp(appList, items.get("app_up"));
        fillWithCi(appList, items.get("app_ci_openeuler"));

        for(AppVersion app : appList) {
            setStatus(app);
            app.setName(name);
        }
    }

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

    private void post(List<AppVersion> verList, String postUrl) {
        for (int idx = 0; idx < verList.size(); idx ++) {
            AppVersion ver = verList.get(idx);
            String body = ObjectMapperUtil.writeValueAsString(ver);
            String re = HttpClientUtil.postHttpClient(postUrl, body);
            log.info("body: {}, res: {}", body, re);
        }
    }

    public void run() {
        String appUrl = env.getProperty("appver.appurl");
        String monUrl = env.getProperty("appver.monurl");
        String postUrl = env.getProperty("appver.posturl");

        Set<String> names = getAppList(appUrl);
        List<AppVersion> verList = generateAppVerList(names, monUrl);
        post(verList, postUrl);
    }
}
