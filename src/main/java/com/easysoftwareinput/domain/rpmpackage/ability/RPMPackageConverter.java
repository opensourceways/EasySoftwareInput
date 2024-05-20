package com.easysoftwareinput.domain.rpmpackage.ability;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.easysoftwareinput.application.rpmpackage.BatchService;
import com.easysoftwareinput.application.rpmpackage.BatchServiceImpl;
import com.easysoftwareinput.application.rpmpackage.HttpService;
import com.easysoftwareinput.application.rpmpackage.RPMPackageService;
import com.easysoftwareinput.common.components.UpstreamService;
import com.easysoftwareinput.common.constant.MapConstant;
import com.easysoftwareinput.common.entity.MessageCode;
import com.easysoftwareinput.common.utils.UUidUtil;
import com.easysoftwareinput.domain.rpmpackage.model.BasePackage;
import com.easysoftwareinput.domain.rpmpackage.model.RPMPackage;
import com.easysoftwareinput.domain.rpmpackage.model.RPMPackageDO;
import com.easysoftwareinput.domain.rpmpackage.model.RPMSrcDO;
import com.easysoftwareinput.infrastructure.BasePackageDO;
import com.easysoftwareinput.infrastructure.mapper.RPMSrcDOMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.power.common.util.StringUtil;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
// @Profile("rpmpkg")
public class RPMPackageConverter {
    @Autowired
    Environment env;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    HttpService httpService;

    @Autowired
    UpstreamService<RPMPackage> upstreamService;

    @Autowired
    BatchServiceImpl batchService;

    @Autowired
    RPMSrcDOMapper srcMapper;

    public List<RPMPackageDO> toDO(List<RPMPackage> pkgList) {
        List<RPMPackageDO> doList = new ArrayList<>();
        for (RPMPackage pkg : pkgList) {
            doList.add(toDO(pkg));
        }
        return doList;
    }

    public RPMPackageDO toDO(RPMPackage pkg) {
        RPMPackageDO pkgDO = new RPMPackageDO();
        BeanUtils.copyProperties(pkg, pkgDO);

        Timestamp currentTime = new Timestamp(System.currentTimeMillis());
        pkgDO.setUpdateAt(currentTime);
        pkgDO.setCreateAt(currentTime);

        String id = UUidUtil.getUUID32();
        pkgDO.setId(id);
        return pkgDO;
    }

    private void assembleDownloadUrl(RPMPackage pkg, Map<String, String> camelMap, Map<String, String> srcUrls) {
        String arch = camelMap.get("arch");
        String url = camelMap.get("baseUrl") + "/" + camelMap.get("locationHref");
        // 如果本软件包是源码包，则没有二进制下载地址
        if ("src".equals(arch)) {
            pkg.setSrcDownloadUrl(url);
            pkg.setBinDownloadUrl("");
            return;
        }

        // 本软件包非源码包
        pkg.setBinDownloadUrl(url);

        String src = camelMap.get("rpmSourcerpm");
        if (StringUtils.isBlank(src) || (! srcUrls.containsKey(src))) {
            return;
        }


        pkg.setSrcDownloadUrl(srcUrls.get(src));
    }

    
    public RPMPackage toEntity(Map<String, String> underLineMap, Map<String, String> srcUrls,
            Map<String, BasePackageDO> maintainers) {
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
        pkg.setLicense(camelMap.get("rpmLicense"));

        assembleDownloadUrl(pkg, camelMap, srcUrls);
        pkg.setChangeLog("");

        pkg.setOs(camelMap.get("osName") + "-" + camelMap.get("osVer"));

        // 版本支持情况
        pkg.setOsSupport(camelMap.get("osName") + "-" + camelMap.get("osVer"));

        pkg.setRepo("");
        try {
            pkg.setRepo(objectMapper.writeValueAsString(Map.ofEntries(
                Map.entry("type", "openEuler官方仓库"),
                Map.entry("url", env.getProperty("rpm.official"))
            )));
        } catch (JsonProcessingException e) {
            log.error(MessageCode.EC00014.getMsgEn(), e);
        }

        pkg.setRepoType("");
        try {
            pkg.setRepoType(objectMapper.writeValueAsString(Map.ofEntries(
                Map.entry("type", camelMap.get("osType")),
                Map.entry("url", env.getProperty("rpm.official") + pkg.getName())
            )));
        } catch (JsonProcessingException e) {
            log.error("", e);
        }

        Long cTime = Long.parseLong(camelMap.get("timeFile"));
        String fTime = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(new Date(cTime * 1000));
        pkg.setRpmUpdateAt(fTime);
        
        double sSize = (double) Double.parseDouble(camelMap.get("sizePackage")) / 1024 / 1024;

        String fSize = String.format("%.2fMB", sSize);
        pkg.setRpmSize(fSize);

        pkg.setSecurity("");
        pkg.setSimilarPkgs("");

        pkg.setSrcRepo(camelMap.get("url"));

        String formatS = String.format("1. 添加源\n```\ndnf config-manager --add-repo %s\n```\n2. 更新源索引\n" +
                "```\ndnf clean all && dnf makecache\n```\n3. 安装 %s 软件包\n```\ndnf install %s\n```", 
                camelMap.get("baseUrl"), pkg.getName(), pkg.getName());
        
        pkg.setInstallation(formatS);
        pkg.setUpStream("");
        pkg.setVersion(camelMap.get("versionVer") + "-" + camelMap.get("versionRel"));
    
        pkg.setSubPath("");
        try {
            String cBase = camelMap.get("baseUrl");
            String[] cSplits = null;
            if (cBase.contains(env.getProperty("rpm.archive1.url"))) {
                cSplits = cBase.split("cn");
            } else if (cBase.contains(env.getProperty("rpm.archive2.url"))) {
                cSplits = cBase.split("org");
            }

            String[] urlSplit = cSplits[1].split("/");
            String[] urls = Arrays.stream(urlSplit).filter(s -> StringUtils.isNotBlank(s)).toArray(String[]::new);
            StringBuilder subPathSB = new StringBuilder();
            for (int i = 1; i < urls.length; i++) {
                subPathSB.append(urls[i]);
            }
            String subPath = subPathSB.toString();
            pkg.setSubPath(subPath);


            StringBuilder cSb = new StringBuilder();
            cSb.append(pkg.getOs());
            cSb.append(pkg.getSubPath());
            cSb.append(pkg.getName());
            cSb.append(pkg.getVersion());
            cSb.append(pkg.getArch());
            pkg.setPkgId(cSb.toString());
        } catch (Exception e) {
            synchronized(RPMPackageConverter.class) {
                log.info("url error");
                log.info(camelMap.toString());
            }
        }

        if (maintainers.containsKey(pkg.getName())) {
            BasePackageDO base = maintainers.get(pkg.getName());
            pkg.setCategory(base.getCategory());
            pkg.setDownloadCount(base.getDownloadCount());
            pkg.setMaintainerEmail(base.getMaintainerEmail());
            pkg.setMaintainerGiteeId(base.getMaintainerGiteeId());
            pkg.setMaintainerId(base.getMaintainerId());
            pkg.setMaintainerUpdateAt(base.getMaintainerUpdateAt());
        }
        

        if (StringUtils.isBlank(pkg.getCategory())) {
            pkg.setCategory(MapConstant.APP_CATEGORY_MAP.get("others"));
        }
        return pkg;
    }
}
