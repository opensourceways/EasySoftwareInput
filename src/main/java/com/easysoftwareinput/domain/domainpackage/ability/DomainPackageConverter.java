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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.easysoftwareinput.common.utils.ObjectMapperUtil;
import com.easysoftwareinput.domain.domainpackage.model.DomainPackage;
import com.easysoftwareinput.infrastructure.domainpackage.DomainPkgDO;

@Service
public class DomainPackageConverter {
    /**
     * env.
     */
    @Autowired
    private Environment env;

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

        Map<String, String> rpmRes = new HashMap<>();
        if (StringUtils.isNotBlank(rpmId)) {
            rpmRes = getPkgRes("rpmpkg?pkgId=", rpmId);
        }

        Map<String, String> appRes = new HashMap<>();
        if (StringUtils.isNotBlank(imageId)) {
            appRes = getPkgRes("apppkg?pkgId=", imageId);
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
        String appIconUrl = appRes.get("iconUrl");
        String defaultIconUrl = env.getProperty("domain.icon");
        String iconUrl = StringUtils.isNotBlank(appIconUrl) ? appIconUrl : defaultIconUrl;
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
     * @param prefix prefix indicates uri.
     * @param id pkgid.
     * @return pkg.
     */
    private Map<String, String> getPkgRes(String prefix, String id) {
        String url = env.getProperty("domain.url");
        RestTemplate restTemplate = new RestTemplate();
        Map<String, Object> res = restTemplate.getForObject(url + prefix + id, Map.class);
        Map<String, Object> data = (Map<String, Object>) res.get("data");
        List<Map<String, String>> list = (List<Map<String, String>>) data.get("list");
        if (list.size() == 0) {
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
}
