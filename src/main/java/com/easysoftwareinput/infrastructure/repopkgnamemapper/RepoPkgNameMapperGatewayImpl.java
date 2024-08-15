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

package com.easysoftwareinput.infrastructure.repopkgnamemapper;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.easysoftwareinput.domain.repopkgnamemapper.RepoPkgNamePkg;
import com.easysoftwareinput.infrastructure.mapper.RepoPkgNameDOMapper;
import com.easysoftwareinput.infrastructure.repopkgnamemapper.converter.RepoPkgNameConverter;
import com.easysoftwareinput.infrastructure.repopkgnamemapper.dataobject.RepoPkgNameDO;
import com.easysoftwareinput.infrastructure.rpmpkg.Gateway;

@Component
public class RepoPkgNameMapperGatewayImpl extends ServiceImpl<RepoPkgNameDOMapper, RepoPkgNameDO>
        implements Gateway<RepoPkgNameDO> {
    /**
     * logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(RepoPkgNameMapperGatewayImpl.class);

    /**
     * converter.
     */
    @Autowired
    private RepoPkgNameConverter converter;

    /**
     * get distinct column.
     * @param column column.
     * @return list of fields.
     */
    public List<RepoPkgNameDO> getDistinctColumn(String column) {
        QueryWrapper<RepoPkgNameDO> wrapper = new QueryWrapper<>();
        wrapper.select("distinct " + column);
        return this.baseMapper.selectList(wrapper);
    }

    /**
     * save all data.
     * @param list list of RepoPkgNamePkg.
     * @param existed existed pkgs.
     */
    public void saveAll(List<RepoPkgNamePkg> list, List<String> existed) {
        List<RepoPkgNameDO> doList = converter.toDo(list);
        Map<Boolean, List<RepoPkgNameDO>> map = doList.stream()
                .collect(Collectors.partitioningBy(pkg -> existed.contains(pkg.getPkgId())));
        saveAndUpdate(map);
    }

    /**
     * get logger.
     */
    @Override
    public Logger getLogger() {
        return RepoPkgNameMapperGatewayImpl.LOGGER;
    }
}
