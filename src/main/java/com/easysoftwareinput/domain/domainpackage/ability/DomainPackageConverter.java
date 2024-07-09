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

package com.easysoftwareinput.domain.domainpackage.ability;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.easysoftwareinput.common.utils.ObjectMapperUtil;
import com.easysoftwareinput.domain.domainpackage.model.DomainPackage;
import com.easysoftwareinput.infrastructure.domainpackage.DomainPkgDO;
import com.easysoftwareinput.infrastructure.fieldpkg.dataobject.FieldDo;

@Service
public class DomainPackageConverter {
    /**
     * logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(DomainPackageConverter.class);

    /**
     * url of domain.
     */
    @Value("${domain.url}")
    private String domainUrl;

    /**
     * icon of domain.
     */
    @Value("${domain.icon}")
    private String domainIconUrl;


    /**
     * convert list of maps to list of pkgs.
     * @param list list of map.
     * @return list of domain pkg.
     */
    public List<DomainPackage> toPkg(List<Map<String, Object>> list) {
        List<DomainPackage> res = new ArrayList<>();
        for (Map<String, Object> map : list) {
            DomainPackage pkg = toPkg(map);
            res.add(pkg);
        }
        return res;
    }

    /**
     * convert map to pkg.
     * @param map map.
     * @return pkg.
     */
    public DomainPackage toPkg(Map<String, Object> map) {
        DomainPackage pkg = new DomainPackage();
        setSimpleField(pkg, map);
        setTags(pkg, map);
        setPkgIds(pkg, map);
        fillPkg(pkg);
        return pkg;
    }

    /**
     * set domain pkg with map.
     * @param pkg pkg.
     * @param map map.
     */
    private void setSimpleField(DomainPackage pkg, Map<String, Object> map) {
        pkg.setCategory((String) map.get("category"));
        pkg.setName((String) map.get("name"));
        pkg.setVersion((String) map.get("version"));
    }

    /**
     * fill the domain pkg with app pkg and rpm pkg.
     * @param pkg domain pkg.
     */
    private void fillPkg(DomainPackage pkg) {
        Map<String, String> pkgIds = pkg.getPkgIds();
        String imageId = pkgIds.get("IMAGE");
        String rpmId = pkgIds.get("RPM");

        if (StringUtils.isBlank(domainUrl)) {
            LOGGER.error("no env: domain.url");
            return;
        }
        Map<String, String> rpmRes = new HashMap<>();
        if (StringUtils.isNotBlank(rpmId)) {
            rpmRes = getPkgRes(domainUrl, "rpmpkg?pkgId=", rpmId);
        }

        Map<String, String> appRes = new HashMap<>();
        if (StringUtils.isNotBlank(imageId)) {
            appRes = getPkgRes(domainUrl, "apppkg?pkgId=", imageId);
        }

        fillPkg(pkg, rpmRes, appRes);
        return;
    }

    /**
     * fill the domain pkg with app pkg and rpm pkg.
     * @param pkg pkg.
     * @param rpmRes rpm.
     * @param appRes app.
     */
    private void fillPkg(DomainPackage pkg, Map<String, String> rpmRes, Map<String, String> appRes) {
        // iconUrl
        if (StringUtils.isBlank(domainIconUrl)) {
            LOGGER.error("no env: domain.icon");
        }
        String appIconUrl = appRes.get("iconUrl");
        String iconUrl = StringUtils.isNotBlank(appIconUrl) ? appIconUrl : domainIconUrl;
        pkg.setIconUrl(iconUrl);

        // arch
        String appArch = appRes.get("arch");
        String rpmArch = rpmRes.get("arch");
        String arch = StringUtils.isNotBlank(appArch) ? appArch : rpmArch;
        pkg.setArch(arch);

        // os
        String appOs = appRes.get("os");
        String rpmOs = rpmRes.get("os");
        String os = StringUtils.isNotBlank(appOs) ? appOs : rpmOs;
        pkg.setOs(os);

        // version
        String appVer = appRes.get("version");
        String rpmVer = rpmRes.get("version");
        String ver = StringUtils.isNotBlank(appVer) ? appVer : rpmVer;
        pkg.setVersion(ver);

        // description
        String appDes = appRes.get("description");
        // log.info("appDes: {}", appDes);
        String rpmDes = rpmRes.get("description");
        if (StringUtils.isNotBlank(appDes)) {
            pkg.setDescription(appDes);
        } else {
            pkg.setDescription(rpmDes);
        }
    }

    /**
     * get pkg from url.
     * @param url url.
     * @param prefix prefix indicates uri.
     * @param id pkgid.
     * @return pkg.
     */
    private Map<String, String> getPkgRes(String url, String prefix, String id) {
        RestTemplate restTemplate = new RestTemplate();
        String rUrl = String.join(url, prefix, id);
        Map<String, Object> res = restTemplate.getForObject(rUrl, Map.class);
        if (res == null || res.size() == 0) {
            LOGGER.info("no res from url: {}", rUrl);
            return Collections.emptyMap();
        }

        Map<String, Object> data = (Map<String, Object>) res.get("data");
        if (data == null || data.size() == 0) {
            LOGGER.info("no `data` from url: {}", rUrl);
            return Collections.emptyMap();
        }
        List<Map<String, String>> list = (List<Map<String, String>>) data.get("list");
        if (list == null || list.size() == 0) {
            LOGGER.info("no `list` from url: {}", rUrl);
            return Collections.emptyMap();
        }
        return list.get(0);
    }

    /**
     * set pkgid.
     * @param pkg pkg.
     * @param map map.
     */
    private void setPkgIds(DomainPackage pkg, Map<String, Object> map) {
        Map<String, String> pkgIds = (Map<String, String>) map.get("pkgIds");
        pkg.setPkgIds(pkgIds);
        return;
    }

    /**
     * set tags.
     * @param pkg pkg.
     * @param map map.
     */
    private void setTags(DomainPackage pkg, Map<String, Object> map) {
        List<String> tags = (List<String>) map.get("tags");
        Set<String> set = tags.stream().collect(Collectors.toSet());
        pkg.setTags(set);
        return;
    }

    /**
     * convert domain pkg to domain data object.
     * @param pkg pkg.
     * @return domain data object.
     */
    public DomainPkgDO toEntity(DomainPackage pkg) {
        DomainPkgDO pkgDO = new DomainPkgDO();
        BeanUtils.copyProperties(pkg, pkgDO);
        pkgDO.setPkgIds(ObjectMapperUtil.writeValueAsString(pkg.getPkgIds()));
        pkgDO.setTags(ObjectMapperUtil.writeValueAsString(pkg.getTags()));

        Timestamp currentTime = new Timestamp(System.currentTimeMillis());
        pkgDO.setUpdateAt(currentTime);
        return pkgDO;
    }

    /**
     * convert list of domain pkgs to list ofdomain data objects.
     * @param pkgList
     * @return list of domain data objects.
     */
    public List<DomainPkgDO> toDo(List<DomainPackage> pkgList) {
        List<DomainPkgDO> oList = new ArrayList<>();
        for (DomainPackage pkg : pkgList) {
            oList.add(toEntity(pkg));
        }
        return oList;
    }

    /**
     * convert FieldDo to DomainPackage.
     * @param fList list of FieldDo.
     * @return list of DomainPackage.
     */
    public List<DomainPkgDO> ofFieldDO(List<FieldDo> fList) {
        if (fList == null || fList.isEmpty()) {
            return Collections.emptyList();
        }
        return fList.stream().map(this::ofFieldDO).collect(Collectors.toList());
    }

    /**
     * convert FieldDo to DomainPackage.
     * @param f FieldDo.
     * @return DomainPackage.
     */
    public DomainPkgDO ofFieldDO(FieldDo f) {
        if (f == null) {
            return null;
        }
        DomainPkgDO d = new DomainPkgDO();
        BeanUtils.copyProperties(f, d);
        d.setUpdateAt(new Timestamp(System.currentTimeMillis()));
        return d;
    }
}
