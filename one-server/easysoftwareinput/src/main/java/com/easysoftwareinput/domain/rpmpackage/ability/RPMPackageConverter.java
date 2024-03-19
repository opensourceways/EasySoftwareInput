package com.easysoftwareinput.domain.rpmpackage.ability;

import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import com.easysoftwareinput.application.rpmpackage.HttpService;
import com.easysoftwareinput.common.components.UpstreamService;
import com.easysoftwareinput.common.entity.MessageCode;
import com.easysoftwareinput.domain.rpmpackage.model.RPMPackage;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.power.common.util.StringUtil;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class RPMPackageConverter {
    @Autowired
    Environment env;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    HttpService httpService;

    @Autowired
    UpstreamService<RPMPackage> upstreamService;

    private String getRepoType(String name) {
        String url = env.getProperty("rpm.remote-repo") + name;
        boolean existed = httpService.validUrl(url);

        String desiredRepo = "";
        if (existed) {
            desiredRepo = url;
        } else {
            desiredRepo = env.getProperty("rpm.remote-repo");
        }

        return desiredRepo;
    }

    public RPMPackage toEntity(Map<String, String> underLineMap) {

        Map<String, String> camelMap = new HashMap<>();
        for (String underLineKey: underLineMap.keySet()) {
            String camelKey = StringUtil.underlineToCamel(underLineKey);
            camelMap.put(camelKey, underLineMap.get(underLineKey));
        }

        RPMPackage pkg = new RPMPackage();
        pkg.setArch(camelMap.get("arch"));
        pkg.setConflicts(camelMap.get("conflicts"));
        pkg.setDescription(camelMap.get("description"));
        pkg.setName(camelMap.get("name"));
        pkg.setProvides(camelMap.get("provides"));
        pkg.setRequires(camelMap.get("requires"));
        pkg.setSummary(camelMap.get("summary"));

        pkg.setBinDownloadUrl(camelMap.get("baseUrl") + camelMap.get("locationHref"));
        pkg.setChangeLog("");

        pkg.setOs(camelMap.get("osName") + "-" + camelMap.get("osVer"));

        // 版本支持情况
        pkg.setOsSupport(camelMap.get("osName") + "-" + camelMap.get("osVer"));

        pkg.setRepo("");
        try {
            pkg.setRepo(objectMapper.writeValueAsString(Map.ofEntries(
                Map.entry("type", "openEuler官方仓库"),
                Map.entry("url", "https://gitee.com/src-openeuler/")
            )));
        } catch (JsonProcessingException e) {
            log.error(MessageCode.EC00014.getMsgEn(), e);
        }

        Long cTime = Long.parseLong(camelMap.get("timeFile"));
        String fTime = new SimpleDateFormat("yyyy-MM-dd").format(new Date(cTime * 1000));
        pkg.setRpmUpdateAt(fTime);
        
        double sSize = (double) Integer.parseInt(camelMap.get("sizePackage")) / 1024 / 1024;
        String fSize = String.format("%.2fMB", sSize);
        pkg.setRpmSize(fSize);

        pkg.setSecurity("");
        pkg.setSimilarPkgs("");
     
        pkg.setSrcDownloadUrl("");

        pkg.setSrcRepo(camelMap.get("url"));

        String formatS = String.format("1. 添加源\n`dnf config-manager --add-repo %s `\n2. 更新源索引\n" +
                "`dnf clean all && dnf makecache`\n3. 安装 %s 软件包\n`dnf install %s`", camelMap.get("baseUrl"),
                pkg.getName(), pkg.getName());
        
        pkg.setInstallation(formatS);
        pkg.setUpStream("");
        pkg.setVersion(camelMap.get("versionVer") + "-" + camelMap.get("versionRel"));

        pkg.setPkgId(camelMap.get("checksum"));
        return pkg;
       
    }
}
