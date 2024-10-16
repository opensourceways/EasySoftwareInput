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

package com.easysoftwareinput.domain.epkgpackage.ability;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
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
import jakarta.annotation.PostConstruct;

@Service
public class EPKGPackageConverter {
    /**
     * logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(EPKGPackageConverter.class);

    /**
     * objectmaper.
     */
    @Autowired
    private ObjectMapper objectMapper;

    /**
     * batch service.
     */
    @Autowired
    private BatchServiceImpl batchService;

    /**
     * env.
     */
    @Autowired
    private Environment env;

    /**
     * map of base pkg.
     */
    private Map<String, BasePackageDO> bases = new HashMap<>();

    /**
     * init of bases.
     */
    @PostConstruct
    public void init() {
        bases = batchService.getNames();
    }

    /**
     * change the format of key: underline to camel.
     * @param underLineMap map.
     * @return map.
     */
    private Map<String, String> changeMap(Map<String, String> underLineMap) {
        Map<String, String> camelMap = new HashMap<>();
        for (Map.Entry<String, String> entry : underLineMap.entrySet()) {
            String camelKey = StringUtil.underlineToCamel(entry.getKey());
            camelMap.put(camelKey, entry.getValue());
        }

        return camelMap;
    }

    /**
     * set download url of pkg.
     * @param pkg pkg.
     * @param camelMap map.
     * @param srcMap map of src pkgs.
     */
    private void setDownloadUrl(EPKGPackage pkg, Map<String, String> camelMap, Map<String, String> srcMap) {
        String arch = camelMap.get("arch");
        String url = camelMap.get("baseUrl") + "/" + camelMap.get("locationHref");
        // 如果本软件包是源码包，则没有二进制下载地址
        if ("src".equals(arch)) {
            pkg.setSrcDownloadUrl(url);
            pkg.setBinDownloadUrl("");
            return;
        }

        // 本软件包非源码包
        String srcKey = StringUtils.trimToEmpty(camelMap.get("rpmSourcerpm"));
        String srcUrl = StringUtils.trimToEmpty(srcMap.get(srcKey));
        pkg.setSrcDownloadUrl(srcUrl);
        pkg.setBinDownloadUrl(url);
    }

    /**
     * change map to epkg pkg.
     * @param underLineMap map.
     * @param srcMap map of src pkgs.
     * @return epkg pkg.
     */
    public EPKGPackage toEntity(Map<String, String> underLineMap, Map<String, String> srcMap) {
        Map<String, String> camelMap = changeMap(underLineMap);

        EPKGPackage pkg = new EPKGPackage();
        pkg.setArch(camelMap.get("arch"));
        pkg.setConflicts(camelMap.get("conflicts"));
        pkg.setDescription(camelMap.get("description"));
        pkg.setName(camelMap.get("name"));
        pkg.setProvides(camelMap.get("provides"));
        pkg.setRequires(camelMap.get("requires"));
        pkg.setFiles(camelMap.get("files"));
        pkg.setSummary(camelMap.get("summary"));
        pkg.setLicense(camelMap.get("rpmLicense"));

        setDownloadUrl(pkg, camelMap, srcMap);
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
            LOGGER.error(MessageCode.EC00014.getMsgEn(), e);
        }

        pkg.setRepoType("");
        try {
            pkg.setRepoType(objectMapper.writeValueAsString(Map.ofEntries(
                Map.entry("type", camelMap.get("osType")),
                Map.entry("url", env.getProperty("epkg.official") + getRepoName(pkg, camelMap))
            )));
        } catch (JsonProcessingException e) {
            LOGGER.error("", e);
        }

        Long cTime = Long.parseLong(camelMap.get("timeFile"));
        String fTime = new SimpleDateFormat("yyyy-MM-dd").format(new Date(cTime * 1000));
        pkg.setEpkgUpdateAt(fTime);

        double sSize = (double) Integer.parseInt(camelMap.get("sizePackage")) / 1024 / 1024;
        String fSize = String.format("%.2fMB", sSize);
        pkg.setEpkgSize(fSize);

        pkg.setSecurity("");
        pkg.setSimilarPkgs("");

        pkg.setSrcRepo(camelMap.get("url"));

        String formatS = String.format("- 添加源%n  ```%n  dnf config-manager --add-repo %s%n  ```%n%n- 更新源索引%n"
                + "  ```%n  dnf clean all && dnf makecache%n  ```%n%n- 安装 %s 软件包%n  ```%n  dnf install %s%n  ```",
                camelMap.get("baseUrl"), pkg.getName(), pkg.getName());

        pkg.setInstallation(formatS);
        pkg.setUpStream("");
        pkg.setVersion(camelMap.get("versionVer") + "-" + camelMap.get("versionRel"));

        BasePackageDO base = bases.get(pkg.getName());
        if (base != null) {
            pkg.setCategory(base.getCategory());
            pkg.setDownloadCount(base.getDownloadCount());
            pkg.setMaintainerEmail(base.getMaintainerEmail());
            pkg.setMaintainerGiteeId(base.getMaintainerGiteeId());
            pkg.setMaintainerId(base.getMaintainerId());
            pkg.setMaintainerUpdateAt(base.getMaintainerUpdateAt());
        }

        if (StringUtils.isBlank(pkg.getCategory())) {
            String other = MapConstant.APP_CATEGORY_MAP.get("others");
            pkg.setCategory(other);
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

    /**
     * get repo name.
     * @param pkg pkg.
     * @param camelMap map.
     * @return name.
     */
    public String getRepoName(EPKGPackage pkg, Map<String, String> camelMap) {
        if ("src".equals(pkg.getArch())) {
            return pkg.getName();
        }

        String src = camelMap.get("rpmSourcerpm");
        String[] splits = src.split("-");
        if (splits == null || splits.length < 2) {
            return pkg.getName();
        }
        String[] name = Arrays.copyOfRange(splits, 0, splits.length - 2);
        return StringUtils.join(name, "-");
    }
}
