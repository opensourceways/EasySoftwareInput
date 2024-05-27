package com.easysoftwareinput.infrastructure.appver.converter;

import java.sql.Timestamp;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.core.util.UuidUtil;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import com.easysoftwareinput.common.utils.UUidUtil;
import com.easysoftwareinput.domain.appver.AppVersion;
import com.easysoftwareinput.infrastructure.appver.dataobject.AppVersionDo;
import com.power.common.util.UUIDUtil;

import co.elastic.clients.elasticsearch._types.Time;

@Component
public class AppVersionConverter {
    public List<AppVersionDo> toDo(List<AppVersion> list) {
        return list.stream().map(this::toDo).collect(Collectors.toList());
    }

    public AppVersionDo toDo(AppVersion v) {
        AppVersionDo d = new AppVersionDo();
        BeanUtils.copyProperties(v, d);
        d.setCreatedAt(new Timestamp(System.currentTimeMillis()));
        d.setUpdateAt(new Timestamp(System.currentTimeMillis()));
        d.setId(UUidUtil.getUUID32());
        return d;
    }
}
