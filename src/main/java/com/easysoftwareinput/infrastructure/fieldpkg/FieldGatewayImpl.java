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

package com.easysoftwareinput.infrastructure.fieldpkg;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.easysoftwareinput.common.constant.MapConstant;
import com.easysoftwareinput.domain.fieldpkg.model.Field;
import com.easysoftwareinput.infrastructure.archnum.OsArchNumDO;
import com.easysoftwareinput.infrastructure.archnum.converter.ArchNumConverter;
import com.easysoftwareinput.infrastructure.fieldpkg.converter.FieldConverter;
import com.easysoftwareinput.infrastructure.fieldpkg.dataobject.FieldDo;
import com.easysoftwareinput.infrastructure.mapper.FieldDoMapper;

@Component
public class FieldGatewayImpl extends ServiceImpl<FieldDoMapper, FieldDo> {
    /**
     * logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(FieldGatewayImpl.class);

    /**
     * mapper.
     */
    @Autowired
    private FieldDoMapper mapper;

    /**
     * converter.
     */
    @Autowired
    private FieldConverter converter;

    /**
     * ArchNumConverter.
     */
    @Autowired
    private ArchNumConverter archNumConverter;

    /**
     * save all the pkg.
     * @param fList list of pkg.
     * @param existedPkgIds existed pkgs.
     * @return boolean.
     */
    public boolean saveAll(List<Field> fList, Set<String> existedPkgIds) {
        List<FieldDo> dList = converter.toDo(fList);

        Map<Boolean, List<FieldDo>> map = dList.stream().collect(Collectors.partitioningBy(
                d -> existedPkgIds.contains(d.getPkgIds())));
        List<FieldDo> existed = map.get(true);
        List<FieldDo> unexisted = map.get(false);
        return save(existed, unexisted);
    }

    /**
     * if existed, update; if unexisted, insert.
     * @param existed existed.
     * @param unexisted unexisted.
     * @return boolean.
     */
    public boolean save(List<FieldDo> existed, List<FieldDo> unexisted) {
        boolean inserted = false;
        try {
            inserted = saveBatch(unexisted, 1000);
        } catch (Exception e) {
            LOGGER.error("fail-to-write, e: {}", e.getMessage());
        }

        boolean updated = false;
        try {
            updated = updateBatchById(existed);
        } catch (Exception e) {
            LOGGER.error("fail-to-update, e: {}", e.getMessage());
        }
        if (inserted && updated) {
            return true;
        }
        return false;
    }

    /**
     * get pkg ids from table.
     * @return set of ids.
     */
    public Set<String> getPkgIds() {
        QueryWrapper<FieldDo> wrapper = new QueryWrapper<>();
        wrapper.select("distinct (pkg_ids)");
        List<FieldDo> dList = mapper.selectList(wrapper);
        return dList.stream().map(FieldDo::getPkgIds).collect(Collectors.toSet());
    }

    /**
     * get list of FieldDo for mainpage.
     * @return list of FieldDo.
     */
    public List<FieldDo> getMainPage() {
        String unCate = MapConstant.CATEGORY_MAP.get("Other");
        List<FieldDo> dList = lambdaQuery()
                .select(FieldDo::getPkgIds, FieldDo::getOs, FieldDo::getArch, FieldDo::getName, FieldDo::getVersion,
                        FieldDo::getCategory, FieldDo::getIconUrl, FieldDo::getTags, FieldDo::getDescription)
                .like(FieldDo::getTags, "image")
                .or(i -> i.like(FieldDo::getTags, "rpm")
                .and(f -> f.ne(FieldDo::getCategory, unCate))).list();
        Map<String, List<FieldDo>> dMap = dList.stream().collect(Collectors.groupingBy(FieldDo::getName));
        Map<String, FieldDo> fMap = dMap.entrySet().stream().collect(
            Collectors.toMap(Map.Entry::getKey, e -> this.pickNewestOs(e.getValue()))
        );
        return fMap.values().stream().collect(Collectors.toList());
    }

    /**
     * pick the FieldDo with newest os.
     * @param dList origin list.
     * @return FieldDo.
     */
    public FieldDo pickNewestOs(List<FieldDo> dList) {
        if (dList == null || dList.isEmpty()) {
            return null;
        }

        List<FieldDo> sList = dList.stream().sorted(
            Comparator.comparing(FieldDo::getOs, Comparator.reverseOrder())
        ).collect(Collectors.toList());
        return sList.get(0);
    }

    /**
     * get the pkgs group by os and arch.
     * @return list of OsArchNumDO.
     */
    public List<OsArchNumDO> getOsArchNum() {
        List<FieldDo> list = lambdaQuery()
                .select(FieldDo::getOs, FieldDo::getArch, FieldDo::getCount)
                .groupBy(FieldDo::getOs, FieldDo::getArch).list();
        return archNumConverter.ofList(list, "FIELD");
    }
}
