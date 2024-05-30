package com.easysoftwareinput.application.fieldpkg;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import com.easysoftwareinput.domain.fieldpkg.model.Field;
import com.easysoftwareinput.domain.rpmpackage.model.RPMPackageDO;
import com.easysoftwareinput.infrastructure.apppkg.AppGatewayImpl;
import com.easysoftwareinput.infrastructure.apppkg.converter.AppConverter;
import com.easysoftwareinput.infrastructure.apppkg.dataobject.AppDo;
import com.easysoftwareinput.infrastructure.epkgpkg.EpkgGatewayImpl;
import com.easysoftwareinput.infrastructure.epkgpkg.converter.EpkgConverter;
import com.easysoftwareinput.infrastructure.epkgpkg.dataobject.EpkgDo;
import com.easysoftwareinput.infrastructure.fieldpkg.FieldGatewayImpl;
import com.easysoftwareinput.infrastructure.rpmpkg.RpmGatewayImpl;
import com.easysoftwareinput.infrastructure.rpmpkg.converter.RpmConverter;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class FieldPkgService {
    @Autowired
    RpmGatewayImpl rpmGateway;

    @Autowired
    EpkgGatewayImpl epkgGateway;

    @Autowired
    AppGatewayImpl appGateway;

    @Autowired
    FieldGatewayImpl fieldGateway;

    @Autowired
    RpmConverter rpmConverter;

    @Autowired
    EpkgConverter epkgConverter;

    @Autowired
    AppConverter appConverter;

    @Autowired
    Environment env;

    private Map<String, Map<String, RPMPackageDO>> getRPMList() {
        List<String> osList = rpmGateway.getDistinctOs();

        Map<String, Map<String, RPMPackageDO>> doMap = new HashMap<>();
        for (String os : osList) {
            List<RPMPackageDO> doList = rpmGateway.getPkg(os);
            rpmConverter.convertToMap(doMap, doList, os);
        }
        return doMap;
    }

    private Map<String, Map<String, EpkgDo>> getEpkgList() {
        List<String> osList = epkgGateway.getDistinctOs();
        Map<String, Map<String, EpkgDo>> doMap = new HashMap<>();
        for (String os : osList) {
            List<EpkgDo> doList = epkgGateway.getPkg(os);
            epkgConverter.convertToMap(doMap, doList, os);
        }
        return doMap;
    }

    private Map<String, Map<String, AppDo>> getAppList() {
        List<String> osList = appGateway.getDistinctOs();
        Map<String, Map<String, AppDo>> doMap = new HashMap<>();
        for (String os : osList) {
            List<AppDo> doList = appGateway.getPkg(os);
            appConverter.convertToMap(doMap, doList, os);
        }
        return doMap;
    }

    private Map<String, Field> getFieldMap(Map<String, RPMPackageDO> rpm, Map<String, EpkgDo> epkg,
            Map<String, AppDo> app) {
        Set<String> set = new HashSet<>();

        if (rpm != null) {
            set.addAll(rpm.keySet());
        }
        
        if (epkg != null) {
            set.addAll(epkg.keySet());
        }

        if (app != null) {
            set.addAll(app.keySet());
        }

        Map<String, Field> map = new HashMap<>();
        for (String name : set) {
            Field field = new Field();
            map.put(name, field);
        }
        return map;
    }

    private void fillFieldWithRpm(Map<String, Field> fieldMap, Map<String, RPMPackageDO> rpm) {
        for (Map.Entry<String, RPMPackageDO> entry : rpm.entrySet()) {
            String name = entry.getKey();
            RPMPackageDO pkg = entry.getValue();
            Field field = fieldMap.get(name);
            BeanUtils.copyProperties(pkg, field);
            field.getPkgIds().put("RPM", pkg.getPkgId());
            field.getTags().add("RPM");
        }
    }

    private void fillFieldWithEpkg(Map<String, Field> fieldMap, Map<String, EpkgDo> rpm) {
        for (Map.Entry<String, EpkgDo> entry : rpm.entrySet()) {
            String name = entry.getKey();
            EpkgDo pkg = entry.getValue();
            Field field = fieldMap.get(name);
            BeanUtils.copyProperties(pkg, field);
            field.getPkgIds().put("EPKG", pkg.getPkgId());
            field.getTags().add("EPKG");
        }
    }

    private void fillFieldWithApp(Map<String, Field> fieldMap, Map<String, AppDo> rpm) {
        for (Map.Entry<String, AppDo> entry : rpm.entrySet()) {
            String name = entry.getKey();
            AppDo pkg = entry.getValue();
            Field field = fieldMap.get(name);
            BeanUtils.copyProperties(pkg, field);
            field.getPkgIds().put("IMAGE", pkg.getPkgId());
            field.getTags().add("IMAGE");
            field.setIconUrl(pkg.getIconUrl());
        }
    }

    private List<Field> getFieldsPerOs(Map<String, RPMPackageDO> rpm, Map<String, EpkgDo>epkg, Map<String,  AppDo> app){
        Map<String, Field> fieldMap = getFieldMap(rpm, epkg, app);
        
        if (rpm != null) {
            fillFieldWithRpm(fieldMap, rpm);
        }

        if (epkg != null) {
            fillFieldWithEpkg(fieldMap, epkg);
        }

        if (app != null) {
            fillFieldWithApp(fieldMap, app);
        }

        String defaultIcon = env.getProperty("domain.icon");
        for (Field f : fieldMap.values()) {
            if (StringUtils.isBlank(f.getIconUrl())) {
                f.setIconUrl(defaultIcon);
            }
        }

        return new ArrayList<Field>(fieldMap.values());
    }

    private List<Field> mapToField(Map<String, Map<String, RPMPackageDO>> rpm, Map<String, Map<String, EpkgDo>> epkg,
            Map<String, Map<String, AppDo>> app) {
        Set<String> osSet = new HashSet<>();
        osSet.addAll(rpm.keySet());
        osSet.addAll(epkg.keySet());
        osSet.addAll(app.keySet());
        
        List<Field> fList = new ArrayList<>();
        for (String os : osSet) {
            List<Field> osList = getFieldsPerOs(rpm.get(os), epkg.get(os), app.get(os));
            fList.addAll(osList);
        }
        return fList;
    }

    public void run() {
        long s = System.currentTimeMillis();

        Map<String, Map<String, RPMPackageDO>> rpm = getRPMList();
        Map<String, Map<String, EpkgDo>> epkg = getEpkgList();
        Map<String, Map<String, AppDo>> app = getAppList();

        List<Field> fList = mapToField(rpm, epkg, app);

        long s1 = System.currentTimeMillis();
        log.info("Jiexi shijin: {}", s1 - s);
        fieldGateway.saveAll(fList);

        log.info("finish-write-domain");
    }
}
