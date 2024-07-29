package com.easysoftwareinput.common.components;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.easysoftwareinput.domain.rpmpackage.model.RPMPackageDO;
import com.easysoftwareinput.infrastructure.apppkg.dataobject.AppDo;
import com.easysoftwareinput.infrastructure.epkgpkg.dataobject.EpkgDo;
import com.easysoftwareinput.infrastructure.rpmpkg.converter.IConverter;

@Component
public class ComponentMap {
    /**
     * iConverterMap.
     */
    @Autowired
    private Map<String, IConverter> iConverterMap;

    /**
     * get converter.
     * @param cls cls.
     * @return Converter.
     */
    public IConverter getConverter(Class<?> cls) {
        if (RPMPackageDO.class.equals(cls)) {
            return iConverterMap.get("RPM");
        } else if (EpkgDo.class.equals(cls)) {
            return iConverterMap.get("EPKG");
        } else if (AppDo.class.equals(cls)) {
            return iConverterMap.get("APP");
        } else {
            return null;
        }
    }
}
