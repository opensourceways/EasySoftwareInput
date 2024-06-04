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

package com.easysoftwareinput.application.domainpackage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import com.easysoftwareinput.domain.domainpackage.model.DomainPackage;
import com.easysoftwareinput.domain.rpmpackage.model.RPMPackageDO;
import com.easysoftwareinput.infrastructure.apppkg.AppGatewayImpl;
import com.easysoftwareinput.infrastructure.apppkg.dataobject.AppDo;
import com.easysoftwareinput.infrastructure.domainpackage.DomainPkgGatewayImpl;
import com.easysoftwareinput.infrastructure.epkgpkg.EpkgGatewayImpl;
import com.easysoftwareinput.infrastructure.epkgpkg.dataobject.EpkgDo;
import com.easysoftwareinput.infrastructure.rpmpkg.RpmGatewayImpl;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class DomainPkgService {
    /**
     * domain pkg gateway.
     */
    @Autowired
    private DomainPkgGatewayImpl gateway;

    /**
     * app pkg gateway.
     */
    @Autowired
    private AppGatewayImpl appGateway;

    /**
     * rpm pkg gateway.
     */
    @Autowired
    private RpmGatewayImpl rpmGateway;

    /**
     * epkg pkg gateway.
     */
    @Autowired
    private EpkgGatewayImpl epkgGateway;

    /**
     * env.
     */
    @Autowired
    private Environment env;

    /**
     * generate domain pkg by app pkg.
     * @param domainMap domainMap to be updated.
     * @param appList applist.
     */
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

    /**
     * generate domain pkg by rpm pkg.
     * @param domainMap domainMap to be updated.
     * @param rpmList  rpmlist.
     */
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

    /**
     * search tags of each domain pkg.
     * @param domainMap domainMap to be updated.
     */
    private void extendsId(Map<String, DomainPackage> domainMap) {
        for (Map.Entry<String, DomainPackage> entry : domainMap.entrySet()) {
            String name = entry.getKey();
            DomainPackage domain = entry.getValue();
            Set<String> tags = domain.getTags();
            Map<String, String> pkgIds = domain.getPkgIds();

            AppDo app = appGateway.queryPkgIdByName(name);
            if (StringUtils.isNotBlank(app.getPkgId())) {
                tags.add("IMAGE");
                pkgIds.put("IMAGE", app.getPkgId());
            }

            RPMPackageDO rpm = rpmGateway.queryPkgIdByName(name);
            if (StringUtils.isNotBlank(rpm.getPkgId())) {
                tags.add("RPM");
                pkgIds.put("RPM", rpm.getPkgId());
            }

            EpkgDo epkg = epkgGateway.queryPkgIdByName(name);
            if (StringUtils.isNotBlank(epkg.getPkgId())) {
                tags.add("EPKG");
                pkgIds.put("EPKG", epkg.getPkgId());
            }
        }
    }

    /**
     * set iconUrl of domain pkg.
     * @param domainMap domainMap.
     */
    private void setIconUrls(Map<String, DomainPackage> domainMap) {
        String defaltIconUrl = env.getProperty("domain.icon");
        for (DomainPackage domain : domainMap.values()) {
            if (StringUtils.isBlank(domain.getIconUrl())) {
                domain.setIconUrl(defaltIconUrl);
            }
        }
    }

    /**
     * generate domain list by app list and rpm list.
     * @param appList applist.
     * @param rpmList rpmlist.
     * @return domain list.
     */
    private List<DomainPackage> toDomainList(List<AppDo> appList, List<RPMPackageDO> rpmList) {
        Map<String, DomainPackage> domainMap = new HashMap<>();

        mergeRpm(domainMap, rpmList);
        mergeApp(domainMap, appList);

        extendsId(domainMap);

        setIconUrls(domainMap);

        return new ArrayList<DomainPackage>(domainMap.values());
    }

    /**
     * run the grogram.
     */
    public void run() {
        List<AppDo> appList = appGateway.getDomain();
        List<RPMPackageDO> rpmList = rpmGateway.getDomain();
        List<DomainPackage> domainList = toDomainList(appList, rpmList);

        gateway.saveAll(domainList);

        log.info("finish-write-domain-pkg");
    }

}
