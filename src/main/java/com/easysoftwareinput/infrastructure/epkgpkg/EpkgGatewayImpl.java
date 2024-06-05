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

package com.easysoftwareinput.infrastructure.epkgpkg;

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
import com.easysoftwareinput.domain.epkgpackage.model.EPKGPackage;
import com.easysoftwareinput.infrastructure.epkgpkg.converter.EpkgConverter;
import com.easysoftwareinput.infrastructure.epkgpkg.dataobject.EpkgDo;
import com.easysoftwareinput.infrastructure.mapper.EpkgDoMapper;

@Component
public class EpkgGatewayImpl extends ServiceImpl<EpkgDoMapper, EpkgDo> {
    /**
     * logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(EpkgGatewayImpl.class);

    /**
     * mapper.
     */
    @Autowired
    private EpkgDoMapper mapper;

    /**
     * converter.
     */
    @Autowired
    private EpkgConverter converter;

    /**
     * save pkg to database.
     * @param epkgList list of pkgs.
     * @return boolean.
     */
    public boolean saveAll(List<EPKGPackage> epkgList) {
        List<EpkgDo> dList = converter.toDo(epkgList);

        List<EpkgDo> existed = new ArrayList<>();
        List<EpkgDo> unexisted = new ArrayList<>();
        fillListById(dList, existed, unexisted);
        return synSave(existed, unexisted);
    }

    /**
     * divide dlist to existed and unexisted.
     * @param dList dlist.
     * @param existed existed.
     * @param unexisted unexisted.
     */
    public void fillListById(List<EpkgDo> dList, List<EpkgDo> existed, List<EpkgDo> unexisted) {
        Set<String> existedPkgIdSet = getExistedIds();
        Map<Boolean, List<EpkgDo>> map = dList.stream()
                .collect(Collectors.partitioningBy(d -> existedPkgIdSet.contains(d.getPkgId())));
        existed.addAll(map.get(true));
        unexisted.addAll(map.get(false));
    }

    /**
     * get existed pkgids from table.
     * @return list of pkgids.
     */
    public Set<String> getExistedIds() {
        QueryWrapper<EpkgDo> wrapper = new QueryWrapper<>();
        wrapper.select("distinct (pkg_id)");
        List<EpkgDo> list = mapper.selectList(wrapper);
        return list.stream().map(EpkgDo::getPkgId).collect(Collectors.toSet());
    }

    /**
     * if existed, update row, if unexisted, insert row.
     * @param existed existed.
     * @param unexisted unexisted.
     * @return boolean.
     */
    public synchronized boolean synSave(List<EpkgDo> existed, List<EpkgDo> unexisted) {
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
        QueryWrapper<EpkgDo> wrapper = new QueryWrapper<>();
        wrapper.select("distinct os");
        List<EpkgDo> doList = mapper.selectList(wrapper);
        List<String> osList = new ArrayList<>();
        for (EpkgDo pkg : doList) {
            osList.add(pkg.getOs());
        }
        return osList;
    }

    /**
     * get list of pkgs by os.
     * @param os os.
     * @return list of pkgs.
     */
    public List<EpkgDo> getPkg(String os) {
        QueryWrapper<EpkgDo> wrapper = new QueryWrapper<>();
        wrapper.select("os, arch, name, version, category, pkg_id, description");
        wrapper.eq("os", os);
        return mapper.selectList(wrapper);
    }

    /**
     * get one pkgid by name.
     * @param name name.
     * @return one pkgid.
     */
    public EpkgDo queryPkgIdByName(String name) {
        QueryWrapper<EpkgDo> wrapper = new QueryWrapper<>();
        wrapper.select("name, pkg_id");
        wrapper.eq("name", name);
        wrapper.last("limit 1");
        List<EpkgDo> list = mapper.selectList(wrapper);
        if (list.size() >= 1) {
            return list.get(0);
        }
        return new EpkgDo();
    }
}
