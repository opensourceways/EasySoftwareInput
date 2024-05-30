package com.easysoftwareinput.domain.externalos.ability;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.easysoftwareinput.domain.externalos.model.ExternalOs;

public class ExternalOsConverter {
    
    public static List<ExternalOs> toEntityList(Map<String, Object> map) {
        List<ExternalOs> list = new ArrayList<>();
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            ExternalOs exOs = toEntity(entry);
            list.add(exOs);
        }
        return list;
    }

    public static ExternalOs toEntity(Map.Entry<String, Object> entry) {
        String key = entry.getKey();
        String value = (String) entry.getValue();
        ExternalOs exOs = new ExternalOs();
        exOs.setOriginOsName("ubuntu");
        exOs.setOriginOsVer("");
        exOs.setOriginPkg(key);
        exOs.setTargetOsName("openEuler");
        exOs.setTargetOsVer("");
        exOs.setTargetPkg(value);
        return exOs;
    }
}
