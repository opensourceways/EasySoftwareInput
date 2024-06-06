package com.easysoftwareinput.infrastructure.appver.converter;

import java.sql.Timestamp;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import com.easysoftwareinput.domain.appver.AppVersion;
import com.easysoftwareinput.infrastructure.appver.dataobject.AppVersionDo;

@Component
public final class AppVersionConverter {
    // Private constructor to prevent instantiation of the utility class
    private AppVersionConverter() {
        // private constructor to hide the implicit public one
        throw new AssertionError("ClientUtil class cannot be instantiated.");
    }

    /**
     * convert pkg to data object.
     * @param list list of pkgs.
     * @return data object.
     */
    public List<AppVersionDo> toDo(List<AppVersion> list) {
        return list.stream().map(this::toDo).collect(Collectors.toList());
    }

    /**
     * convert pkg to data object.
     * @param v pkg.
     * @return data object.
     */
    public AppVersionDo toDo(AppVersion v) {
        AppVersionDo d = new AppVersionDo();
        BeanUtils.copyProperties(v, d);
        d.setCreatedAt(new Timestamp(System.currentTimeMillis()));
        d.setUpdateAt(new Timestamp(System.currentTimeMillis()));
        d.setId(v.getName() + v.getOpeneulerVersion() + v.getEulerOsVersion());
        return d;
    }
}
