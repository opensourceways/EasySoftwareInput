package com.easysoftwareinput.infrastructure.rpmpkg;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.easysoftwareinput.domain.rpmpackage.ability.RPMPackageConverter;
import com.easysoftwareinput.domain.rpmpackage.model.RPMPackage;
import com.easysoftwareinput.domain.rpmpackage.model.RPMPackageDO;
import com.easysoftwareinput.infrastructure.fieldpkg.dataobject.FieldDo;
import com.easysoftwareinput.infrastructure.mapper.RPMPackageDOMapper;
import com.easysoftwareinput.infrastructure.rpmpkg.converter.RpmConverter;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class RpmGatewayImpl extends ServiceImpl<RPMPackageDOMapper, RPMPackageDO> {
    @Autowired
    RPMPackageDOMapper mapper;

    @Autowired
    RPMPackageConverter converter;

    @Autowired
    RpmConverter convert2;

    @Transactional(rollbackFor = Exception.class)
    public boolean saveAll(List<RPMPackage> list) {
        List<RPMPackageDO> dList = converter.toDO(list);

        List<RPMPackageDO> existed = new ArrayList<>();
        List<RPMPackageDO> unexisted = new ArrayList<>();
        fillListById(dList, existed, unexisted);
        return synSave(existed, unexisted);
    }

    @Transactional(rollbackFor = Exception.class)
    public synchronized boolean synSave(List<RPMPackageDO> existed, List<RPMPackageDO> unexisted) {
        boolean inserted = saveBatch(unexisted, 1000);
        boolean updated = updateBatchById(existed);
        if (inserted && updated) {
            return true;
        }
        return false;
    }

    public void fillListById(List<RPMPackageDO> dList, List<RPMPackageDO> existed, List<RPMPackageDO> unexisted) {
        Set<String> existedPkgIdSet = getExistedIds();
        for (RPMPackageDO d : dList) {
            String pkgId = d.getPkgId();
            if (existedPkgIdSet.contains(pkgId)) {
                existed.add(d);
            } else {
                unexisted.add(d);
            }
        }
    }

    public Set<String> getExistedIds() {
        QueryWrapper<RPMPackageDO> wrapper = new QueryWrapper<>();
        wrapper.select("distinct (pkg_id)");
        List<RPMPackageDO> list = mapper.selectList(wrapper);
        return list.stream().map(RPMPackageDO::getPkgId).collect(Collectors.toSet());
    }

    public List<String> getDistinctOs() {
        QueryWrapper<RPMPackageDO> wrapper = new QueryWrapper<>();
        wrapper.select("distinct os");
        List<RPMPackageDO> doList = mapper.selectList(wrapper);
        return doList.stream().map(pkg -> pkg.getOs()).collect(Collectors.toList());
    }

    public List<RPMPackageDO> getPkg(String os) {
        QueryWrapper<RPMPackageDO> wrapper = new QueryWrapper<>();
        wrapper.select("os, arch, name, version, category, pkg_id, description");
        wrapper.and(i -> i.likeRight("sub_path", "everything").or().likeRight("sub_path", "EPOL"));
        wrapper.eq("os", os);
        return mapper.selectList(wrapper);
    }

    public List<RPMPackageDO> getDomain() {
        QueryWrapper<RPMPackageDO> wrapper = new QueryWrapper<>();
        wrapper.in("category", List.of("AI", "大数据", "分布式存储", "数据库", "云服务", "HPC"));
        wrapper.select("os, arch, name, version, category, pkg_id, description");
        List<RPMPackageDO> dList = mapper.selectList(wrapper);
        return convert2.filterDuplicate(dList);
    }

    public RPMPackageDO queryPkgIdByName(String name) {
        name = StringUtils.trimToEmpty(name);
        if (StringUtils.isBlank(name)) {
            return null;
        }

        QueryWrapper<RPMPackageDO> wrapper = new QueryWrapper<>();
        wrapper.select("name, pkg_id");
        wrapper.eq("name", name);
        wrapper.last("limit 1");
        List<RPMPackageDO> list = mapper.selectList(wrapper);
        if (list.size() >= 1) {
            return list.get(0);
        }
        return null;
    }
}
