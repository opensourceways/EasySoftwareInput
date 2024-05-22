package com.easysoftwareinput.infrastructure.apppkg;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.easysoftwareinput.domain.apppackage.model.AppPackage;
import com.easysoftwareinput.infrastructure.apppkg.converter.AppConverter;
import com.easysoftwareinput.infrastructure.apppkg.dataobject.AppDo;
import com.easysoftwareinput.infrastructure.epkgpkg.dataobject.EpkgDo;
import com.easysoftwareinput.infrastructure.mapper.AppDoMapper;

@Component
public class AppGatewayImpl extends ServiceImpl<AppDoMapper, AppDo> {
    @Autowired
    AppDoMapper mapper;

    @Autowired
    AppConverter converter;

    public boolean saveAll(List<AppPackage> appList) {
        List<AppDo> dList = converter.toDo(appList);
        return saveOrUpdateBatch(dList, 50);
    }

    public List<String> getOs() {
        QueryWrapper<AppDo> wrapper = new QueryWrapper<>();
        wrapper.select("distinct os");
        List<AppDo> doList = mapper.selectList(wrapper);
        List<String> osList = new ArrayList<>();
        for (AppDo pkg : doList) {
            osList.add(pkg.getOs());
        }
        return osList;
    }

    public List<AppDo> getPkg(String os) {
        QueryWrapper<AppDo> wrapper = new QueryWrapper<>();
        wrapper.select("os, arch, name, app_ver, category, icon_url, pkg_id, description");
        wrapper.eq("os", os);
        return mapper.selectList(wrapper);
    }

    public List<AppDo> getDomain() {
        QueryWrapper<AppDo> wrapper = new QueryWrapper<>();
        wrapper.select("os, arch, name, app_ver, category, icon_url, pkg_id, description");
        wrapper.groupBy("name");
        return mapper.selectList(wrapper);
    }

    public AppDo queryPkgIdByName(String name) {
        QueryWrapper<AppDo> wrapper = new QueryWrapper<>();
        wrapper.select("name, pkg_id");
        wrapper.eq("name", name);
        wrapper.last("limit 1");
        List<AppDo> list = mapper.selectList(wrapper);
        if (list.size() >= 1) {
            return list.get(0);
        }
        return new AppDo();
    }
}
