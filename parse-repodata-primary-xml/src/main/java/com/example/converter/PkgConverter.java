package com.example.converter;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.example.entity.po.RPMPackage;
import com.example.service.ParseRepoType;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.power.common.util.StringUtil;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class PkgConverter {
    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    ParseRepoType parseRepoType;

    public RPMPackage assembleInputObject(Map<String ,String> underLineMap, List<String> srcFiles) {
        Map<String, String> camelMap = new HashMap<>();
        for (String underLineKey: underLineMap.keySet()) {
            String camelKey = StringUtil.underlineToCamel(underLineKey);
            camelMap.put(camelKey, underLineMap.get(underLineKey));
        }

        RPMPackage pkg = null;
        try {
            String json = objectMapper.writeValueAsString(camelMap);
            
            pkg = objectMapper.readValue(json, RPMPackage.class);
        } catch (Exception e) {
        }
        
        pkg.setBinDownloadUrl(camelMap.get("baseUrl") + camelMap.get("locationHref"));
        pkg.setChangeLog("");

        pkg.setMaintanierId("");
        pkg.setMaintianerEmail("");
        pkg.setMaintainerGiteeId("");
        pkg.setMaintainerUpdateAt("");
        pkg.setMaintainerStatus("");

        pkg.setOs(camelMap.get("osName") + "-" + camelMap.get("osVer"));

        // 版本支持情况
        pkg.setOsSupport(camelMap.get("osName") + "-" + camelMap.get("osVer"));

        // pkg.setRepo("openEuler官方仓库");
        pkg.setRepo("");
        try {
            pkg.setRepo(objectMapper.writeValueAsString(Map.ofEntries(
                Map.entry("type", "openEuler官方仓库"),
                Map.entry("url", "https://gitee.com/src-openeuler/")
            )));
        } catch (JsonProcessingException e) {
            log.error("", e);
        }

        // pkg.setRepoType(camelMap.get("osType"));
        pkg.setRepoType("");
        String desiredRepo = "";
        List<String> repos = parseRepoType.getRepos();
        for (String repo : repos) {
            String cutRepo = repo.replace("src-openeuler/", "");
            if (pkg.getName().contains(cutRepo)) {
                desiredRepo = repo;
                break;
            }
        }
        try {
            pkg.setRepoType(objectMapper.writeValueAsString(Map.ofEntries(
                Map.entry("type", camelMap.get("osType")),
                Map.entry("url", "https://gitee.com/" + desiredRepo)
            )));
        } catch (JsonProcessingException e) {
            log.error("", e);
        }
        

        pkg.setRpmCategory("Unspecified");

        Long cTime = Long.parseLong(camelMap.get("timeFile"));
        String fTime = new SimpleDateFormat("yyyy-MM-dd").format(new Date(cTime * 1000));
        pkg.setRpmUpdateAt(fTime);
        
        double sSize = (double) Integer.parseInt(camelMap.get("sizePackage")) / 1024 / 1024;
        String fSize = String.format("%.2fMB", sSize);
        pkg.setRpmSize(fSize);

        pkg.setSecurity("");
        pkg.setSimilarPkgs("");
     
        String desired = "";
        for (String srcUrl : srcFiles) {
            String[] splits = srcUrl.split("/");
            String name = splits[splits.length - 1];
            if (name.equals(camelMap.get("rpmSourcerpm"))) {
                desired = srcUrl;
                break;
            }
        }
        pkg.setSrcDownloadUrl(desired);

        pkg.setSrcRepo(camelMap.get("url"));

        String formatS = String.format("1. 添加源\n`dnf config-manager --add-repo %s `\n2. 更新源索引\n" +
                "`dnf clean all && dnf makecache`\n3. 安装 %s 软件包\n`dnf install %s`", camelMap.get("baseUrl"),
                pkg.getName(), pkg.getName());
        
        pkg.setInstallation(formatS);
        pkg.setUpStream("");
        pkg.setVersion(camelMap.get("versionVer") + "-" + camelMap.get("versionRel"));

        return pkg;
    }
}
