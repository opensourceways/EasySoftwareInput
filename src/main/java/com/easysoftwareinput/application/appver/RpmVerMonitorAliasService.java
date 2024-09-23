package com.easysoftwareinput.application.appver;

import java.util.HashMap;
import java.util.Map;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.easysoftwareinput.domain.appver.RpmVerMonitorAlias;
import com.easysoftwareinput.domain.appver.RpmVerMonitorAliasConfig;

@Component
public class RpmVerMonitorAliasService {
    /**
     * rpmver monitor alias config.
     */
    @Autowired
    private RpmVerMonitorAliasConfig config;

    /**
     * alias map.
     */
    private Map<String, String> aliasMap = new HashMap<>();

    /**
     * get monitor name.
     * @param name pkg name.
     * @return monitor name.
     */
    public String getMonitorName(String name) {
        if (aliasMap.isEmpty()) {
            initAliasMap();
        }
        String monitorName = aliasMap.get(name);
        if (StringUtils.isBlank(monitorName)) {
            return name;
        } else {
            return monitorName;
        }
    }

    /**
     * init alias map.
     */
    public void initAliasMap() {
        List<RpmVerMonitorAlias> aliasList = config.getRpmMonitorAliasList();
        for (RpmVerMonitorAlias alias : aliasList) {
            String pkgName = alias.getPkgName();
            String monitorName = alias.getMonitorName();
            if (StringUtils.isBlank(pkgName) || StringUtils.isBlank(monitorName)) {
                continue;
            }
            aliasMap.put(pkgName, monitorName);
        }
    }
}
