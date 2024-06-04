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

package com.easysoftwareinput.infrastructure.domainpackage;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.easysoftwareinput.domain.domainpackage.ability.DomainPackageConverter;
import com.easysoftwareinput.domain.domainpackage.model.DomainPackage;
import com.easysoftwareinput.infrastructure.mapper.DomainPkgMapper;

@Service
public class DomainPkgGatewayImpl extends ServiceImpl<DomainPkgMapper, DomainPkgDO> {
    /**
     * converter.
     */
    @Autowired
    private DomainPackageConverter converter;

    /**
     * save all pkg.
     * @param fList
     * @return boolean.
     */
    public boolean saveAll(List<DomainPackage> fList) {
        List<DomainPkgDO> dList = converter.toDo(fList);
        return saveOrUpdateBatch(dList, 1000);
    }
}
