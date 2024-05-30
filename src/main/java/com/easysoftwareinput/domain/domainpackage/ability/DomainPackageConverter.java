package com.easysoftwareinput.domain.domainpackage.ability;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.easysoftwareinput.application.domainpackage.DomainPkgService;
import com.easysoftwareinput.common.utils.ObjectMapperUtil;
import com.easysoftwareinput.domain.domainpackage.model.DomainPackage;
import com.easysoftwareinput.infrastructure.domainpackage.DomainPkgDO;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mysql.cj.x.protobuf.MysqlxCrud.Collection;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class DomainPackageConverter {
    @Autowired
    Environment env;

    public List<DomainPackage> toPkg(List<Map<String, Object>> list) {
        List<DomainPackage> res = new ArrayList<>();
        for (Map<String, Object> map : list) {
            DomainPackage pkg = toPkg(map);
            res.add(pkg);
        }
        return res;
    }

    public DomainPackage toPkg(Map<String, Object> map) {
        DomainPackage pkg = new DomainPackage();
        setSimpleField(pkg, map);
        
        setTags(pkg, map);
        setPkgIds(pkg, map);
        fillPkg(pkg);
        return pkg;
    }

    private void setSimpleField(DomainPackage pkg, Map<String, Object> map) {
        pkg.setCategory((String) map.get("category"));
        pkg.setName((String) map.get("name"));
        pkg.setVersion((String) map.get("version"));
    }

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

    // private void fillMes(DomainPackage pkg, String prefix, String rpmId) {
    //     String url = env.getProperty("domain.url");
    //     RestTemplate restTemplate = new RestTemplate();
    //     Map<String, Object> res = restTemplate.getForObject(url + prefix + "?pkgId=" + rpmId, Map.class);
    //     Map<String, Object> data = (Map<String, Object>) res.get("data");
    //     List<Map<String, String>> list = (List<Map<String, String>>) data.get("list");
    //     Map<String, String> map = list.get(0);
    //     pkg.setArch(map.get("arch"));
    //     pkg.setOs(map.get("os"));
    //     pkg.setVersion(map.get("version"));
    //     return;
    // }

    private void setPkgIds(DomainPackage pkg, Map<String, Object> map) {
        Map<String, String> pkgIds = (Map<String, String>) map.get("pkgIds");
        pkg.setPkgIds(pkgIds);
        return;
    }

    private void setTags(DomainPackage pkg, Map<String, Object> map) {
        List<String> tags = (List<String>) map.get("tags");
        Set<String> set = tags.stream().collect(Collectors.toSet());
        pkg.setTags(set);
        return;
    }
    
    public DomainPkgDO toEntity(DomainPackage pkg) {
        DomainPkgDO pkgDO = new DomainPkgDO();
        BeanUtils.copyProperties(pkg, pkgDO);
        pkgDO.setPkgIds(ObjectMapperUtil.writeValueAsString(pkg.getPkgIds()));
        pkgDO.setTags(ObjectMapperUtil.writeValueAsString(pkg.getTags()));
        return pkgDO;
    }

    public List<DomainPkgDO> toDo(List<DomainPackage> pkgList) {
        List<DomainPkgDO> oList = new ArrayList<>();
        for (DomainPackage pkg : pkgList) {
            oList.add(toEntity(pkg));
        }
        return oList;
    }
}
