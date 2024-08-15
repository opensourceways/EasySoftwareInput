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

package com.easysoftwareinput.infrastructure.repopkgnamemapper.converter;

import java.sql.Timestamp;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.easysoftwareinput.domain.repopkgnamemapper.RepoPkgNameMapperConfig;
import com.easysoftwareinput.domain.repopkgnamemapper.RepoPkgNamePkg;
import com.easysoftwareinput.infrastructure.repopkgnamemapper.dataobject.RepoPkgNameDO;

@Component
public class RepoPkgNameConverter {
    /**
     * config.
     */
    @Autowired
    private RepoPkgNameMapperConfig config;

    /**
     * convert RepoPkgNamePkg to RepoPkgNameDO.
     * @param list list of RepoPkgNamePkg.
     * @return list of RepoPkgNameDO.
     */
    public List<RepoPkgNameDO> toDo(List<RepoPkgNamePkg> list) {
        List<RepoPkgNameDO> res = list.stream().map(this::toDo).filter(pkg -> !Objects.isNull(pkg))
                .collect(Collectors.toList());
        return res;
    }

    /**
     * convert RepoPkgNamePkg to RepoPkgNameDO.
     * @param pkg RepoPkgNamePkg.
     * @return RepoPkgNameDO.
     */
    public RepoPkgNameDO toDo(RepoPkgNamePkg pkg) {
        if (StringUtils.isBlank(pkg.getPkgName())) {
            return null;
        }
        RepoPkgNameDO pkgDo = new RepoPkgNameDO();
        pkgDo.setOs(pkg.getBranch());
        pkgDo.setName(pkg.getPkgName());
        pkgDo.setUpdateAt(new Timestamp(System.currentTimeMillis()));

        String url = String.format(config.getRepoUrlTemplate(), config.getOrg(), pkg.getRepoName());
        pkgDo.setRepoUrl(url);
        pkgDo.setPkgId(pkgDo.getRepoUrl() + pkgDo.getOs() + pkgDo.getName());
        return pkgDo;
    }
}
