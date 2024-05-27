package com.easysoftwareinput.infrastructure.appver;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.easysoftwareinput.domain.appver.AppVersion;
import com.easysoftwareinput.infrastructure.appver.converter.AppVersionConverter;
import com.easysoftwareinput.infrastructure.appver.dataobject.AppVersionDo;
import com.easysoftwareinput.infrastructure.mapper.AppVersionDoMapper;

@Component
public class AppVerGatewayImpl extends ServiceImpl<AppVersionDoMapper, AppVersionDo> {
    @Autowired
    AppVersionConverter converter;

    public boolean saveAll(List<AppVersion> verList) {
        List<AppVersionDo> dList = converter.toDo(verList);
        saveOrUpdateBatch(dList);
        return false;
    }
}
