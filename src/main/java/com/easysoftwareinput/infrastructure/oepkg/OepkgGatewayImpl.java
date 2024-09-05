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

package com.easysoftwareinput.infrastructure.oepkg;

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
import com.easysoftwareinput.domain.oepkg.model.OePkg;
import com.easysoftwareinput.domain.oepkg.model.OePkgEntity;
import com.easysoftwareinput.domain.oepkg.model.ThreadPkgEntity;
import com.easysoftwareinput.infrastructure.archnum.OsArchNumDO;
import com.easysoftwareinput.infrastructure.archnum.converter.ArchNumConverter;
import com.easysoftwareinput.infrastructure.mapper.OepkgDOMapper;
import com.easysoftwareinput.infrastructure.oepkg.converter.OepkgConverter;
import com.easysoftwareinput.infrastructure.oepkg.dataobject.OepkgDO;

import lombok.Getter;
import lombok.Setter;

@Component
@Getter
@Setter
public class OepkgGatewayImpl extends ServiceImpl<OepkgDOMapper, OepkgDO> {
    /**
     * logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(OepkgGatewayImpl.class);

    /**
     * converter.
     */
    @Autowired
    private OepkgConverter converter;

    /**
     * ArchNumConverter.
     */
    @Autowired
    private ArchNumConverter archNumConverter;

    /**
     * save all data to database.
     * @param tPkg ThreadPkgEntity.
     * @param oePkgEntity OePkgEntity.
     * @return boolean.
     */
    public boolean saveAll(ThreadPkgEntity tPkg, OePkgEntity oePkgEntity) {
        List<OePkg> list = tPkg.getPkgs();
        Set<String> existedPkgIdSet = oePkgEntity.getExistedPkgIds();

        List<OepkgDO> dList = converter.toDO(list);
        Map<Boolean, List<OepkgDO>> map = dList.stream().collect(Collectors.partitioningBy(
            d -> existedPkgIdSet.contains(d.getPkgId())
        ));
        List<OepkgDO> existed = map.get(true);
        List<OepkgDO> unexisted = map.get(false);
        boolean res = synSave(existed, unexisted, tPkg, oePkgEntity);
        return res;
    }

    /**
     * get existed pkgids from table.
     * @return set of pkgids.
     */
    public Set<String> getExistedIds() {
        QueryWrapper<OepkgDO> wrapper = new QueryWrapper<>();
        wrapper.select("distinct (pkg_id)");
        List<OepkgDO> list = baseMapper.selectList(wrapper);
        return list.stream().map(OepkgDO::getPkgId).collect(Collectors.toSet());
    }

    /**
     * save the data.
     * @param existed if existed, update the row.
     * @param unexisted if unexisted, insert the row.
     * @param tPkg entity of this thread.
     * @param oePkgEntity entity of oepkg.
     * @return boolean.
     */
    public boolean synSave(List<OepkgDO> existed, List<OepkgDO> unexisted, ThreadPkgEntity tPkg,
            OePkgEntity oePkgEntity) {
        boolean inserted = false;
        try {
            inserted = saveBatch(unexisted);
        } catch (Exception e) {
            LOGGER.error("fail-to-write, e: {}, filename: {}", e.getMessage(), tPkg.getFileName());
        }

        boolean updated = false;
        try {
            updated = updateBatchById(existed);
        } catch (Exception e) {
            LOGGER.error("fail-to-update, e: {}, filename: {}", e.getMessage(), tPkg.getFileName());
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
        QueryWrapper<OepkgDO> wrapper = new QueryWrapper<>();
        wrapper.select("distinct os");
        List<OepkgDO> doList = baseMapper.selectList(wrapper);
        List<String> osList = new ArrayList<>();
        for (OepkgDO pkg : doList) {
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
        QueryWrapper<OepkgDO> wrapper = new QueryWrapper<>();
        wrapper.ge("update_at", time);
        return baseMapper.selectCount(wrapper);
    }

    /**
     * get pkg by os.
     * @param os os.
     * @return lsit of pkgs.
     */
    public List<OepkgDO> getPkg(String os) {
        QueryWrapper<OepkgDO> wrapper = new QueryWrapper<>();
        wrapper.select("os, arch, name, version, category, pkg_id, description");
        wrapper.eq("os", os);
        return baseMapper.selectList(wrapper);
    }

    /**
     * get pkgs which will be converted to domain pkg.
     * @return list of pkg.
     */
    public List<OepkgDO> getDomain() {
        QueryWrapper<OepkgDO> wrapper = new QueryWrapper<>();
        wrapper.in("category", List.of("AI", "大数据", "分布式存储", "数据库", "云服务", "HPC"));
        wrapper.select("os, arch, name, version, category, pkg_id, description");
        return baseMapper.selectList(wrapper);
    }

    /**
     * get one pkg by name.
     * @param name name.
     * @return one pkg.
     */
    public OepkgDO queryPkgIdByName(String name) {
        QueryWrapper<OepkgDO> wrapper = new QueryWrapper<>();
        wrapper.select("name, pkg_id");
        wrapper.eq("name", name);
        wrapper.last("limit 1");
        List<OepkgDO> list = baseMapper.selectList(wrapper);
        if (list.size() >= 1) {
            return list.get(0);
        }
        return new OepkgDO();
    }

    /**
     * get the pkgs group by os and arch.
     * @return list of OsArchNumDO.
     */
    public List<OsArchNumDO> getOsArchNum() {
        List<OepkgDO> list = lambdaQuery()
                .select(OepkgDO::getOs, OepkgDO::getArch, OepkgDO::getCount)
                .groupBy(OepkgDO::getOs, OepkgDO::getArch).list();
        return archNumConverter.ofList(list, "OEPKG");
    }
}
