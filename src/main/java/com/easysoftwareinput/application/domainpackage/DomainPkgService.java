package com.easysoftwareinput.application.domainpackage;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.data.repository.support.DomainClassConverter;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.easysoftwareinput.common.utils.ObjectMapperUtil;
import com.easysoftwareinput.domain.domainpackage.ability.DomainPackageConverter;
import com.easysoftwareinput.domain.domainpackage.model.DomainPackage;
import com.easysoftwareinput.domain.rpmpackage.model.RPMPackage;
import com.easysoftwareinput.domain.rpmpackage.model.RPMPackageDO;
import com.easysoftwareinput.infrastructure.apppkg.AppGatewayImpl;
import com.easysoftwareinput.infrastructure.apppkg.dataobject.AppDo;
import com.easysoftwareinput.infrastructure.domainpackage.DomainPkgDO;
import com.easysoftwareinput.infrastructure.domainpackage.DomainPkgGatewayImpl;
import com.easysoftwareinput.infrastructure.epkgpkg.EpkgGatewayImpl;
import com.easysoftwareinput.infrastructure.epkgpkg.dataobject.EpkgDo;
import com.easysoftwareinput.infrastructure.mapper.DomainPkgMapper;
import com.easysoftwareinput.infrastructure.rpmpkg.RpmGatewayImpl;
import com.fasterxml.jackson.core.exc.StreamReadException;
import com.fasterxml.jackson.databind.DatabindException;
import com.fasterxml.jackson.databind.ObjectMapper;

import ch.qos.logback.core.joran.util.beans.BeanUtil;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class DomainPkgService {
    @Value("${domain.file}")
    private String path;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    DomainPackageConverter converter;

    @Autowired
    DomainPkgGatewayImpl gateway;

    @Autowired
    AppGatewayImpl appGateway;

    @Autowired
    RpmGatewayImpl rpmGateway;

    @Autowired
    EpkgGatewayImpl epkgGateway;

    @Autowired
    Environment env;

    private void mergeApp(Map<String, DomainPackage> domainMap, List<AppDo> appList) {
        for (AppDo app : appList) {
            String name = app.getName();
            DomainPackage domain = domainMap.get(name);
            if (domain == null) {
                domain = new DomainPackage();
                domainMap.put(name, domain);
            }

            BeanUtils.copyProperties(app, domain);
            domain.getTags().add("IMAGE");
            domain.getPkgIds().put("IMAGE", app.getPkgId());
        }
    }

    private void mergeRpm(Map<String, DomainPackage> domainMap, List<RPMPackageDO> rpmList) {
        for (RPMPackageDO rpm : rpmList) {
            String name = rpm.getName();
            DomainPackage domain = domainMap.get(name);
            if (domain == null) {
                domain = new DomainPackage();
                domainMap.put(name, domain);
            }

            BeanUtils.copyProperties(rpm, domain);
            domain.getTags().add("RPM");
            domain.getPkgIds().put("RPM", rpm.getPkgId());
        }
    }

    private void extendsId(Map<String, DomainPackage> domainMap) {
        for (Map.Entry<String, DomainPackage> entry : domainMap.entrySet()) {
            String name = entry.getKey();
            DomainPackage domain = entry.getValue();
            Set<String> tags = domain.getTags();
            Map<String, String> pkgIds = domain.getPkgIds();

            AppDo app = appGateway.queryPkgIdByName(name);
            if (app != null && StringUtils.isNotBlank(app.getPkgId())) {
                tags.add("IMAGE");
                pkgIds.put("IMAGE", app.getPkgId());
            }

            RPMPackageDO rpm = rpmGateway.queryPkgIdByName(name);
            if (rpm != null && StringUtils.isNotBlank(rpm.getPkgId())) {
                tags.add("RPM");
                pkgIds.put("RPM", rpm.getPkgId());
            }

            EpkgDo epkg = epkgGateway.queryPkgIdByName(name);
            if (epkg != null && StringUtils.isNotBlank(epkg.getPkgId())) {
                tags.add("EPKG");
                pkgIds.put("EPKG", epkg.getPkgId());
            }
        }
    }

    private void setIconUrls(Map<String, DomainPackage> domainMap) {
        String defaltIconUrl = env.getProperty("domain.icon");
        for (DomainPackage domain : domainMap.values()) {
            if (StringUtils.isBlank(domain.getIconUrl())) {
                domain.setIconUrl(defaltIconUrl);
            }
        }
    }

    private List<DomainPackage> toDomainList(List<AppDo> appList, List<RPMPackageDO> rpmList) {
        Map<String, DomainPackage> domainMap = new HashMap<>();

        mergeRpm(domainMap, rpmList);
        mergeApp(domainMap, appList);

        extendsId(domainMap);

        setIconUrls(domainMap);

        return new ArrayList<DomainPackage>(domainMap.values());
    }

    public void run() {

        List<AppDo> appList = appGateway.getDomain();
        List<RPMPackageDO> rpmList = rpmGateway.getDomain();
        List<DomainPackage> domainList = toDomainList(appList, rpmList);

        gateway.saveAll(domainList);

        log.info("finish-write-domain-pkg");
    }

}
