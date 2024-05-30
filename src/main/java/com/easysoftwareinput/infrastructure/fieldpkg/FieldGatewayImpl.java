package com.easysoftwareinput.infrastructure.fieldpkg;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.easysoftwareinput.domain.fieldpkg.model.Field;
import com.easysoftwareinput.infrastructure.fieldpkg.converter.FieldConverter;
import com.easysoftwareinput.infrastructure.fieldpkg.dataobject.FieldDo;
import com.easysoftwareinput.infrastructure.mapper.FieldDoMapper;

@Component
public class FieldGatewayImpl extends ServiceImpl<FieldDoMapper, FieldDo> {
    @Autowired
    FieldDoMapper mapper;

    @Autowired
    FieldConverter converter;

    public boolean saveAll(List<Field> fList) {
        List<FieldDo> dList = converter.toDo(fList);

        List<FieldDo> existed = new ArrayList<>();
        List<FieldDo> unexisted = new ArrayList<>();
        fillListById(dList, existed, unexisted);

        boolean insert = saveBatch(unexisted, 1000);
        boolean update = updateBatchById(existed);
        if (insert && update) {
            return true;
        }
        return false;
    }

    public void fillListById(List<FieldDo> dList, List<FieldDo> existed, List<FieldDo> unexisted) {
        Set<String> existedPkgIdSet = getExistedIds();
        for (FieldDo d: dList) {
            String pkgId =  d.getPkgIds();
            if (existedPkgIdSet.contains(pkgId)) {
                existed.add(d);
            } else {
                unexisted.add(d);
            }
        }
    }

    public Set<String> getExistedIds() {
        QueryWrapper<FieldDo> wrapper = new QueryWrapper<>();
        wrapper.select("distinct (pkg_ids)");
        List<FieldDo> list = mapper.selectList(wrapper);
        return list.stream().map(FieldDo::getPkgIds).collect(Collectors.toSet());
    }
}
