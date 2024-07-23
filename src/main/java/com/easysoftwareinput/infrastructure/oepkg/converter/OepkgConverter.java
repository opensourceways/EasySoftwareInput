package com.easysoftwareinput.infrastructure.oepkg.converter;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import com.easysoftwareinput.common.constant.MapConstant;
import com.easysoftwareinput.common.utils.ObjectMapperUtil;
import com.easysoftwareinput.common.utils.UUidUtil;
import com.easysoftwareinput.domain.oepkg.model.OePkg;
import com.easysoftwareinput.infrastructure.BasePackageDO;
import com.easysoftwareinput.infrastructure.oepkg.dataobject.OepkgDO;
import com.power.common.util.StringUtil;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class OepkgConverter {
        /**
     * convert rpm pkg to rpm data object.
     * @param pkgList lsit of rpm pkg.
     * @return list of rpm data objects.
     */
    public List<OepkgDO> toDO(List<OePkg> pkgList) {
        List<OepkgDO> doList = new ArrayList<>();
        for (OePkg pkg : pkgList) {
            doList.add(toDO(pkg));
        }
        return doList;
    }

    /**
     * convert rpm pkg to rpm data object.
     * @param pkg pkg.
     * @return rpm data object.
     */
    public OepkgDO toDO(OePkg pkg) {
        OepkgDO pkgDO = new OepkgDO();
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
    private void assembleDownloadUrl(OePkg pkg, Map<String, String> camelMap, Map<String, String> srcUrls) {
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
    public void setPkgRepoAndRepoType(OePkg pkg, Map<String, String> camelMap) {
        String repo = ObjectMapperUtil.writeValueAsString(Map.of(
            "type", "oepkg官方仓库",
            "url", ""
        ));
        pkg.setRepo(repo);

        String repoType = ObjectMapperUtil.writeValueAsString(Map.of(
            "type", "",
            "url", ""
        ));
        pkg.setRepoType(repoType);
    }

    /**
     * convert map to pkg.
     * @param underLineMap map.
     * @param srcUrls map of src pkg and url.
     * @param maintainers maintianers.
     * @return rpm pkg.
     */
    public OePkg toEntity(Map<String, String> underLineMap, Map<String, String> srcUrls,
            Map<String, BasePackageDO> maintainers) {
        Map<String, String> camelMap = new HashMap<>();
        for (Map.Entry<String, String> entry : underLineMap.entrySet()) {
            String camelKey = StringUtil.underlineToCamel(entry.getKey());
            camelMap.put(camelKey, entry.getValue());
        }

        OePkg pkg = new OePkg();
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

        String formatS = String.format("- 添加源%n  ```%n  dnf config-manager --add-repo %s%n  ```%n%n- 更新源索引%n"
                + "  ```%n dnf clean all && dnf makecache%n  ```%n%n- 安装 %s 软件包%n  ```%n dnf install %s%n  ```",
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
    public void setPkgPkgId(OePkg pkg) {
        String pkgId = pkg.getOs() + pkg.getSubPath() + pkg.getName() + pkg.getVersion() + pkg.getArch();
        pkg.setPkgId(pkgId);
    }

    /**
     * set subPath of pkg.
     * @param pkg pkg.
     * @param camelMap map.
     */
    public void setPkgSubPath(OePkg pkg, Map<String, String> camelMap) {
        String cBase = camelMap.get("baseUrl");
        if (StringUtils.isBlank(cBase)) {
            log.error("no baseurl");
            return;
        }

        String[] cSplits = splitBase(cBase);

        String subPath;
        if (cSplits != null && cSplits.length >= 2) {
            subPath = cSplits[1];
        } else {
            subPath = "";
        }

        String[] suSplits = subPath.split("/");
        String exactSubPath;
        if (suSplits.length >= 2) {
            List<String> list = Stream.of(suSplits).filter(StringUtils::isNotBlank).collect(Collectors.toList());
            exactSubPath = StringUtils.join(list.subList(1, list.size()), "");
        } else {
            exactSubPath = "";
        }
        pkg.setSubPath(exactSubPath);
    }

    /**
     * split the url.
     * @param cBase url.
     * @return string array.
     */
    public String[] splitBase(String cBase) {
        return cBase.split("/rpm/");
    }
}
