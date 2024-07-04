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

package com.easysoftwareinput.domain.rpmpackage.ability;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import com.easysoftwareinput.common.constant.MapConstant;
import com.easysoftwareinput.common.entity.MessageCode;
import com.easysoftwareinput.common.utils.UUidUtil;
import com.easysoftwareinput.domain.rpmpackage.model.RPMPackage;
import com.easysoftwareinput.domain.rpmpackage.model.RPMPackageDO;
import com.easysoftwareinput.infrastructure.BasePackageDO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.power.common.util.StringUtil;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class RPMPackageConverter {
    /**
     * list of os in archive1.
     */
    @Value("${rpm.official}")
    private String official;

    /**
     * url of archive1.
     */
    @Value("${rpm.archive1.url}")
    private String arUrl1;

    /**
     * url of archive2.
     */
    @Value("${rpm.archive2.url}")
    private String arUrl2;

    /**
     * objectmapper.
     */
    @Autowired
    private ObjectMapper objectMapper;

    /**
     * convert rpm pkg to rpm data object.
     * @param pkgList lsit of rpm pkg.
     * @return list of rpm data objects.
     */
    public List<RPMPackageDO> toDO(List<RPMPackage> pkgList) {
        List<RPMPackageDO> doList = new ArrayList<>();
        for (RPMPackage pkg : pkgList) {
            doList.add(toDO(pkg));
        }
        return doList;
    }

    /**
     * convert rpm pkg to rpm data object.
     * @param pkg pkg.
     * @return rpm data object.
     */
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

    /**
     * assemble downloadurl of pkg.
     * @param pkg pkg.
     * @param camelMap map.
     * @param srcUrls map of src pkg and url.
     */
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
        if (StringUtils.isBlank(src) || (!srcUrls.containsKey(src))) {
            return;
        }

        pkg.setSrcDownloadUrl(srcUrls.get(src));
    }

    /**
     * set repo and repoType of pkg.
     * @param pkg pkg.
     * @param camelMap camelMap.
     */
    public void setPkgRepoAndRepoType(RPMPackage pkg, Map<String, String> camelMap) {
        if (StringUtils.isBlank(official)) {
            log.error("no env: rpm.official");
            return;
        }

        try {
            pkg.setRepo(objectMapper.writeValueAsString(Map.ofEntries(
                Map.entry("type", "openEuler官方仓库"),
                Map.entry("url", official)
            )));
        } catch (JsonProcessingException e) {
            log.error(MessageCode.EC00014.getMsgEn(), e);
            pkg.setRepo("");
        }

        try {
            pkg.setRepoType(objectMapper.writeValueAsString(Map.ofEntries(
                Map.entry("type", camelMap.get("osType")),
                Map.entry("url", official + pkg.getName())
            )));
        } catch (JsonProcessingException e) {
            log.error("", e);
            pkg.setRepoType("");
        }
    }

    /**
     * convert map to pkg.
     * @param underLineMap map.
     * @param srcUrls map of src pkg and url.
     * @param maintainers maintianers.
     * @return rpm pkg.
     */
    public RPMPackage toEntity(Map<String, String> underLineMap, Map<String, String> srcUrls,
            Map<String, BasePackageDO> maintainers) {
        Map<String, String> camelMap = new HashMap<>();
        for (Map.Entry<String, String> entry : underLineMap.entrySet()) {
            String camelKey = StringUtil.underlineToCamel(entry.getKey());
            camelMap.put(camelKey, entry.getValue());
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

        setPkgRepoAndRepoType(pkg, camelMap);

        Long cTime = Long.parseLong(camelMap.get("timeFile"));
        String fTime = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(new Date(cTime * 1000));
        pkg.setRpmUpdateAt(fTime);

        double sSize = (double) Double.parseDouble(camelMap.get("sizePackage")) / 1024 / 1024;

        String fSize = String.format("%.2fMB", sSize);
        pkg.setRpmSize(fSize);

        pkg.setSecurity("");
        pkg.setSimilarPkgs("");

        pkg.setSrcRepo(camelMap.get("url"));

        String formatS = String.format("- 添加源%n```%ndnf config-manager --add-repo %s%n```%n- 更新源索引%n"
                + "```%ndnf clean all && dnf makecache%n```%n- 安装 %s 软件包%n```%ndnf install %s%n```",
                camelMap.get("baseUrl"), pkg.getName(), pkg.getName());

        pkg.setInstallation(formatS);
        pkg.setUpStream("");
        pkg.setVersion(camelMap.get("versionVer") + "-" + camelMap.get("versionRel"));

        setPkgSubPath(pkg, camelMap);
        setPkgPkgId(pkg);

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

    /**
     * set pkgId of pkg.
     * @param pkg pkg.
     */
    public void setPkgPkgId(RPMPackage pkg) {
        String pkgId = String.join(pkg.getOs(), pkg.getSubPath(), pkg.getName(), pkg.getVersion(), pkg.getArch());
        pkg.setPkgId(pkgId);
    }

    /**
     * set subPath of pkg.
     * @param pkg pkg.
     * @param camelMap map.
     */
    public void setPkgSubPath(RPMPackage pkg, Map<String, String> camelMap) {
        String cBase = camelMap.get("baseUrl");
        if (StringUtils.isBlank(cBase)) {
            log.error("no baseurl");
            return;
        }

        String[] cSplits;
        if (StringUtils.isBlank(arUrl1) || StringUtils.isBlank(arUrl2)) {
            log.error("no env: rpm.archvie1.url, rpm.archive2.url");
            return;
        }
        if (cBase.contains(arUrl1)) {
            cSplits = cBase.split("cn");
        } else if (cBase.contains(arUrl2)) {
            cSplits = cBase.split("org");
        } else {
            log.error("unrecognized url: {}", cBase);
            return;
        }

        String subPath;
        if (cSplits.length >= 2) {
            subPath = cSplits[1];
        } else {
            subPath = "";
        }
        subPath = subPath.replaceAll("/", "");

        pkg.setSubPath(subPath);
    }
}
