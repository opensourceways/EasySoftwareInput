package com.easysoftwareinput.domain.epkgpackage.ability;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import com.easysoftwareinput.application.rpmpackage.BatchServiceImpl;
import com.easysoftwareinput.common.constant.MapConstant;
import com.easysoftwareinput.common.entity.MessageCode;
import com.easysoftwareinput.domain.epkgpackage.model.EPKGPackage;
import com.easysoftwareinput.infrastructure.BasePackageDO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.power.common.util.StringUtil;

import io.micrometer.common.util.StringUtils;

@Service
public class EPKGPackageConverter {
    private static final Logger logger = LoggerFactory.getLogger(EPKGPackageConverter.class);

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    BatchServiceImpl batchService;

    @Autowired
    Environment env;
  
    public EPKGPackage toEntity(Map<String, String> underLineMap) {

        Map<String, String> camelMap = new HashMap<>();
        for (String underLineKey: underLineMap.keySet()) {
            String camelKey = StringUtil.underlineToCamel(underLineKey);
            camelMap.put(camelKey, underLineMap.get(underLineKey));
        }

        EPKGPackage pkg = new EPKGPackage();
        pkg.setArch(camelMap.get("arch"));
        pkg.setConflicts(camelMap.get("conflicts"));
        pkg.setDescription(camelMap.get("description"));
        pkg.setName(camelMap.get("name"));
        pkg.setProvides(camelMap.get("provides"));
        pkg.setRequires(camelMap.get("requires"));
        pkg.setFiles(camelMap.get("files"));
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
                Map.entry("url", env.getProperty("epkg.official"))
            )));
        } catch (JsonProcessingException e) {
            logger.error(MessageCode.EC00014.getMsgEn(), e);
        }

        pkg.setRepoType("");
        try {
            pkg.setRepoType(objectMapper.writeValueAsString(Map.ofEntries(
                Map.entry("type", camelMap.get("osType")),
                Map.entry("url", env.getProperty("epkg.official") + pkg.getName())
            )));
        } catch (JsonProcessingException e) {
            logger.error("", e);
        }

        Long cTime = Long.parseLong(camelMap.get("timeFile"));
        String fTime = new SimpleDateFormat("yyyy-MM-dd").format(new Date(cTime * 1000));
        pkg.setEpkgUpdateAt(fTime);
        
        double sSize = (double) Integer.parseInt(camelMap.get("sizePackage")) / 1024 / 1024;
        String fSize = String.format("%.2fMB", sSize);
        pkg.setEpkgSize(fSize);

        pkg.setSecurity("");
        pkg.setSimilarPkgs("");
     
        pkg.setSrcDownloadUrl("");

        pkg.setSrcRepo(camelMap.get("url"));

        String formatS = String.format("1. 添加源\n```dnf config-manager --add-repo %s ```\n2. 更新源索引\n" +
                "```dnf clean all && dnf makecache```\n3. 安装 %s 软件包\n```dnf install %s```", camelMap.get("baseUrl"),
                pkg.getName(), pkg.getName());
        
        pkg.setInstallation(formatS);
        pkg.setUpStream("");
        pkg.setVersion(camelMap.get("versionVer") + "-" + camelMap.get("versionRel"));

        List<BasePackageDO> baseList = batchService.readFromDatabase(pkg.getName());
        if (baseList.size() >= 1) {
            BasePackageDO base = baseList.get(0);
            pkg.setCategory(base.getCategory());
            pkg.setDownloadCount(base.getDownloadCount());
            pkg.setMaintainerEmail(base.getMaintainerEmail());
            pkg.setMaintainerGiteeId(base.getMaintainerGiteeId());
            pkg.setMaintainerId(base.getMaintainerId());
            pkg.setMaintainerUpdateAt(base.getMaintainerUpdateAt());
        } else {
        }

        if (StringUtils.isBlank(pkg.getCategory())) {
            pkg.setCategory(MapConstant.APP_CATEGORY_MAP.get("Other"));
        }

        StringBuilder cSb = new StringBuilder();
        String os = env.getProperty("epkg.os-name") + "-" + env.getProperty("epkg.os-ver");
        cSb.append(os);
        cSb.append(pkg.getName());
        cSb.append(pkg.getVersion());
        cSb.append(pkg.getArch());

        pkg.setPkgId(cSb.toString());

        return pkg;
    }
}
