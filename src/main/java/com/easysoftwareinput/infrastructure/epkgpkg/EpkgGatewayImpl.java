package com.easysoftwareinput.infrastructure.epkgpkg;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
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
    @Autowired
    EpkgDoMapper mapper;

    @Autowired
    EpkgConverter converter;

    public boolean saveAll(List<EPKGPackage> epkgList) {
        List<EpkgDo> dList = converter.toDo(epkgList);

        List<EpkgDo> existed = new ArrayList<>();
        List<EpkgDo> unexisted = new ArrayList<>();
        fillListById(dList, existed, unexisted);
        return synSave(existed, unexisted);
    }

    public synchronized boolean synSave(List<EpkgDo> existed, List<EpkgDo> unexisted) {
        boolean inserted = saveBatch(unexisted, 1000);
        boolean updated = updateBatchById(existed);

        if (inserted && updated) {
            return true;
        }
        return false;
    }

    public void fillListById(List<EpkgDo> dList, List<EpkgDo> existed, List<EpkgDo> unexisted) {
        Set<String> existedPkgIdSet = getExistedIds();
        Map<Boolean, List<EpkgDo>> map = dList.stream()
                .collect(Collectors.partitioningBy(d -> existedPkgIdSet.contains(d.getPkgId())));
        existed.addAll(map.get(true));
        unexisted.addAll(map.get(false));
    }

    public Set<String> getExistedIds() {
        QueryWrapper<EpkgDo> wrapper = new QueryWrapper<>();
        wrapper.select("distinct (pkg_id)");
        List<EpkgDo> list = mapper.selectList(wrapper);
        return list.stream().map(EpkgDo::getPkgId).collect(Collectors.toSet());
    }

    public List<String> getDistinctOs() {
        QueryWrapper<EpkgDo> wrapper = new QueryWrapper<>();
        wrapper.select("distinct os");
        List<EpkgDo> doList = mapper.selectList(wrapper);
        return doList.stream().map(EpkgDo::getOs).collect(Collectors.toList());
    }


    public List<EpkgDo> getPkg(String os) {
        QueryWrapper<EpkgDo> wrapper = new QueryWrapper<>();
        wrapper.select("os, arch, name, version, category, pkg_id, description");
        wrapper.eq("os", os);
        return mapper.selectList(wrapper);
    }

    public EpkgDo queryPkgIdByName(String name) {
        name = StringUtils.trimToEmpty(name);
        if (StringUtils.isBlank(name)) {
            return null;
        }

        QueryWrapper<EpkgDo> wrapper = new QueryWrapper<>();
        wrapper.select("name, pkg_id");
        wrapper.eq("name", name);
        wrapper.last("limit 1");
        List<EpkgDo> list = mapper.selectList(wrapper);
        if (list.size() >= 1) {
            return list.get(0);
        }
        return null;
    }
}
