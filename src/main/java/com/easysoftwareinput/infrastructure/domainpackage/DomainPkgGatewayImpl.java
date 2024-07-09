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

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.easysoftwareinput.domain.domainpackage.model.DomainPkgContext;
import com.easysoftwareinput.infrastructure.mapper.DomainPkgMapper;

@Service
public class DomainPkgGatewayImpl extends ServiceImpl<DomainPkgMapper, DomainPkgDO> {
    /**
     * logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(DomainPkgGatewayImpl.class);

    /**
     * save all pkgs.
     * @param context context.
     * @return boolean.
     */
    public boolean saveAll(DomainPkgContext context) {
        List<DomainPkgDO> dList = context.getPkgList();
        Set<String> existedIds = context.getExistedPkgIds();
        Map<Boolean, List<DomainPkgDO>> map = dList.stream().collect(Collectors.partitioningBy(
            s -> existedIds.contains(s.getPkgIds())
        ));
        List<DomainPkgDO> unexisted = map.get(false);
        List<DomainPkgDO> existed = map.get(true);

        return synSave(unexisted, existed);
    }

    /**
     * save the unexisted, update the existed.
     * @param unexisted unexisted.
     * @param existed existed.
     * @return boolean.
     */
    public boolean synSave(List<DomainPkgDO> unexisted, List<DomainPkgDO> existed) {
        boolean inserted = false;
        try {
            inserted = saveBatch(unexisted);
        } catch (Exception e) {
            LOGGER.error("fail-to-write, e: {}", e.getMessage());
        }

        boolean updated = false;
        try {
            updated = updateBatchById(existed);
        } catch (Exception e) {
            LOGGER.error("fail-to-update, e: {}", e);
        }

        return inserted && updated;
    }

    /**
     * get existed pkg ids in the table.
     * @return set of pkg ids.
     */
    public Set<String> getExistedPkgIds() {
        QueryWrapper<DomainPkgDO> wrapper = new QueryWrapper<>();
        wrapper.select("distinct pkg_ids");
        List<DomainPkgDO> list = this.baseMapper.selectList(wrapper);
        return list.stream().map(DomainPkgDO::getPkgIds).collect(Collectors.toSet());
    }

    /**
     * get changed row.
     * @param startTime start time.
     * @return length.
     */
    public long getChangedRow(long startTime) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String time = format.format(startTime);
        return lambdaQuery().ge(DomainPkgDO::getUpdateAt, time).count();
    }
}
