package com.easysoftwareinput.infrastructure.appver;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import com.easysoftwareinput.common.filter.DataFilter;
import com.easysoftwareinput.domain.appver.AppVersion;

@Component
public class AppverDataFilterImpl implements DataFilter<AppVersion> {
    /**
     * generate appvers by names.
     *
     * @param data names of AppVersion.
     * @return filteed data
     */
    @Override
    public Map<Boolean, List<AppVersion>> filteringData(List<AppVersion> data) {
        Map<Boolean, List<AppVersion>> res = new HashMap<>();
        List<AppVersion> cleanData = new ArrayList<>();
        List<AppVersion> dirtyData = new ArrayList<>();

        for (AppVersion v : data) {
            // 前端展示字段空值全校验
            if (validEmptyPkg(v)) {
                dirtyData.add(v);
                continue;
            }
            // 字段业务含义校验 TODO
            cleanData.add(v);
        }
        res.computeIfAbsent(true, k -> new ArrayList<>()).addAll(cleanData);
        res.computeIfAbsent(false, k -> new ArrayList<>()).addAll(dirtyData);
        return res;
    }

    /**
     * whether the AppVersion is empty or not.
     *
     * @param v AppVersion.
     * @return boolean.
     */
    public boolean validEmptyPkg(AppVersion v) {
        if (StringUtils.isBlank(v.getUpstreamVersion()) && StringUtils.isBlank(v.getUpHomepage())
                && StringUtils.isBlank(v.getCiVersion())
                && StringUtils.isBlank(v.getEulerOsVersion()) && StringUtils.isBlank(v.getOpeneulerVersion())
                && StringUtils.isBlank(v.getEulerHomepage())) {
            return true;
        }
        return false;
    }
}
