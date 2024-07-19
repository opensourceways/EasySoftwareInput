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

package com.easysoftwareinput.infrastructure.rpmpkg;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
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
import com.easysoftwareinput.domain.rpmpackage.ability.RPMPackageConverter;
import com.easysoftwareinput.domain.rpmpackage.model.RPMPackage;
import com.easysoftwareinput.domain.rpmpackage.model.RPMPackageDO;
import com.easysoftwareinput.infrastructure.archnum.OsArchNumDO;
import com.easysoftwareinput.infrastructure.archnum.converter.ArchNumConverter;
import com.easysoftwareinput.infrastructure.mapper.RPMPackageDOMapper;

@Component
public class RpmGatewayImpl extends ServiceImpl<RPMPackageDOMapper, RPMPackageDO> {
    /**
     * logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(RpmGatewayImpl.class);

    /**
     * converter.
     */
    @Autowired
    private RPMPackageConverter converter;

    /**
     * ArchNumConverter.
     */
    @Autowired
    private ArchNumConverter archNumConverter;

    /**
     * mapper.
     */
    @Autowired
    private RPMPackageDOMapper mapper;

    /**
     * save all data to database.
     * @param list lsit of pkg.
     * @param existedPkgIdSet existedPkgIdSet.
     * @return boolean.
     */
    public boolean saveAll(List<RPMPackage> list, Set<String> existedPkgIdSet) {
        List<RPMPackageDO> dList = converter.toDO(list);

        Map<Boolean, List<RPMPackageDO>> map = dList.stream().collect(Collectors.partitioningBy(
                d -> existedPkgIdSet.contains(d.getPkgId())));
        List<RPMPackageDO> existed = map.get(true);
        List<RPMPackageDO> unexisted = map.get(false);
        return synSave(existed, unexisted);
    }

    /**
     * get existed pkgids from table.
     * @return set of pkgids.
     */
    public Set<String> getExistedIds() {
        QueryWrapper<RPMPackageDO> wrapper = new QueryWrapper<>();
        wrapper.select("distinct (pkg_id)");
        List<RPMPackageDO> list = mapper.selectList(wrapper);
        return list.stream().map(RPMPackageDO::getPkgId).collect(Collectors.toSet());
    }

    /**
     * save the data.
     * @param existed if existed, update the row.
     * @param unexisted if unexisted, insert the row.
     * @return boolean.
     */
    public synchronized boolean synSave(List<RPMPackageDO> existed, List<RPMPackageDO> unexisted) {
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
     * get distinct os from table.
     * @return list of os.
     */
    public List<String> getOs() {
        QueryWrapper<RPMPackageDO> wrapper = new QueryWrapper<>();
        wrapper.select("distinct os");
        List<RPMPackageDO> doList = mapper.selectList(wrapper);
        List<String> osList = new ArrayList<>();
        for (RPMPackageDO pkg : doList) {
            osList.add(pkg.getOs());
        }
        return osList;
    }

    /**
     * get length of data row from table.
     * @param startTime startTime.
     * @return length of data.
     */
    public long getChangedRow(long startTime) {
        SimpleDateFormat format =  new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String time = format.format(startTime);
        QueryWrapper<RPMPackageDO> wrapper = new QueryWrapper<>();
        wrapper.ge("update_at", time);
        return mapper.selectCount(wrapper);
    }

    /**
     * get pkg by os.
     * @param os os.
     * @return lsit of pkgs.
     */
    public List<RPMPackageDO> getPkg(String os) {
        QueryWrapper<RPMPackageDO> wrapper = new QueryWrapper<>();
        wrapper.select("os, arch, name, version, category, pkg_id, description, maintainer_id");
        wrapper.eq("os", os);
        wrapper.and(i -> i.notLike("sub_path", "update"));
        wrapper.and(i -> i.likeRight("sub_path", "EPOL").or()
                .likeRight("sub_path", "everything").or()
                .likeRight("sub_path", "OS"));
        return mapper.selectList(wrapper);
    }

    /**
     * get pkgs which will be converted to domain pkg.
     * @return list of pkg.
     */
    public List<RPMPackageDO> getDomain() {
        QueryWrapper<RPMPackageDO> wrapper = new QueryWrapper<>();
        wrapper.in("category", List.of("AI", "大数据", "分布式存储", "数据库", "云服务", "HPC"));
        wrapper.select("os, arch, name, version, category, pkg_id, description");
        return mapper.selectList(wrapper);
    }

    /**
     * get one pkg by name.
     * @param name name.
     * @return one pkg.
     */
    public RPMPackageDO queryPkgIdByName(String name) {
        QueryWrapper<RPMPackageDO> wrapper = new QueryWrapper<>();
        wrapper.select("name, pkg_id");
        wrapper.eq("name", name);
        wrapper.last("limit 1");
        List<RPMPackageDO> list = mapper.selectList(wrapper);
        if (list.size() >= 1) {
            return list.get(0);
        }
        return new RPMPackageDO();
    }

    /**
     * get the pkgs group by os and arch.
     * @return list of OsArchNumDO.
     */
    public List<OsArchNumDO> getOsArchNum() {
        List<RPMPackageDO> list = lambdaQuery()
                .select(RPMPackageDO::getOs, RPMPackageDO::getArch, RPMPackageDO::getCount)
                .groupBy(RPMPackageDO::getOs, RPMPackageDO::getArch).list();
        return archNumConverter.ofList(list, "RPM");
    }


    /**
     * get srcdownloadurls.
     * @return list of srcdownloadurls.
     */
    public Set<String> getSrcDownloadUrls() {
        List<RPMPackageDO> list = lambdaQuery().select(RPMPackageDO::getSrcDownloadUrl)
                .isNotNull(RPMPackageDO::getSrcDownloadUrl).list();
        return list.stream().map(RPMPackageDO::getSrcDownloadUrl).collect(Collectors.toSet());
    }

}
