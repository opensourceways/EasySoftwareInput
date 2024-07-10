/* Copyright (c) 2024 openEuler Community
 EasySoftwareInput is licensed under the Mulan PSL v2.
 You can use this software according to the terms and conditions of the Mulan PSL v2.
 You may obtain a copy of Mulan PSL v2 at:
     http://license.coscl.org.cn/MulanPSL2
 THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
 EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
 MERCHANTABILITY OR FIT FOR A PARTICULAR PURPOSE.
 See the Mulan PSL v2 for more details.
*/

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
    /**
     * rpm gateway.
     */
    @Autowired
    private RpmGatewayImpl rpmGateway;

    /**
     * epkg gateway.
     */
    @Autowired
    private EpkgGatewayImpl epkgGateway;

    /**
     * app gateway.
     */
    @Autowired
    private AppGatewayImpl appGateway;

    /**
     * field gateway.
     */
    @Autowired
    private FieldGatewayImpl fieldGateway;

    /**
     * rpm converter.
     */
    @Autowired
    private RpmConverter rpmConverter;

    /**
     * epkg converter.
     */
    @Autowired
    private EpkgConverter epkgConverter;

    /**
     * app converter.
     */
    @Autowired
    private AppConverter appConverter;

    /**
     * env.
     */
    @Autowired
    private Environment env;

    /**
     * get rpm.
     * @return map of rpm.
     */
    private Map<String, Map<String, RPMPackageDO>> getRPMList() {
        List<String> osList = rpmGateway.getOs();

        Map<String, Map<String, RPMPackageDO>> doMap = new HashMap<>();
        for (String os : osList) {
            List<RPMPackageDO> doList = rpmGateway.getPkg(os);
            rpmConverter.convertToMap(doMap, doList, os);
        }
        return doMap;
    }

    /**
     * get epkg.
     * @return map of epkg.
     */
    private Map<String, Map<String, EpkgDo>> getEpkgList() {
        List<String> osList = epkgGateway.getOs();
        Map<String, Map<String, EpkgDo>> doMap = new HashMap<>();
        for (String os : osList) {
            List<EpkgDo> doList = epkgGateway.getPkg(os);
            epkgConverter.convertToMap(doMap, doList, os);
        }
        return doMap;
    }

    /**
     * get app.
     * @return map of app.
     */
    private Map<String, Map<String, AppDo>> getAppList() {
        List<String> osList = appGateway.getOs();
        Map<String, Map<String, AppDo>> doMap = new HashMap<>();
        for (String os : osList) {
            List<AppDo> doList = appGateway.getPkg(os);
            appConverter.convertToMap(doMap, doList, os);
        }
        return doMap;
    }

    /**
     * convert rpm, epkg, app to field pkg.
     * @param rpm rpm.
     * @param epkg epkg.
     * @param app app.
     * @return map of field pkg.
     */
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

    /**
     * fill field pkg with rpm.
     * @param fieldMap field map.
     * @param rpm rpm.
     */
    private void fillFieldWithRpm(Map<String, Field> fieldMap, Map<String, RPMPackageDO> rpm) {
        for (Map.Entry<String, RPMPackageDO> entry : rpm.entrySet()) {
            String name = entry.getKey();
            RPMPackageDO pkg = entry.getValue();
            Field field = fieldMap.get(name);
            BeanUtils.copyProperties(pkg, field);
            field.getPkgIds().put("RPM", pkg.getPkgId());
            field.getTags().add("RPM");

            String maintainer = pkg.getMaintainerId();
            if (!StringUtils.isBlank(maintainer)) {
                field.getMaintianers().put("RPM", maintainer);
            }
        }
    }

    /**
     * fill field pkg with epkg.
     * @param fieldMap field map.
     * @param epkg epkg.
     */
    private void fillFieldWithEpkg(Map<String, Field> fieldMap, Map<String, EpkgDo> epkg) {
        for (Map.Entry<String, EpkgDo> entry : epkg.entrySet()) {
            String name = entry.getKey();
            EpkgDo pkg = entry.getValue();
            Field field = fieldMap.get(name);
            BeanUtils.copyProperties(pkg, field);
            field.getPkgIds().put("EPKG", pkg.getPkgId());
            field.getTags().add("EPKG");

            String maintainer = pkg.getMaintainerId();
            if (!StringUtils.isBlank(maintainer)) {
                field.getMaintianers().put("EPKG", maintainer);
            }
        }
    }

    /**
     * fill field pkg with app.
     * @param fieldMap field map.
     * @param app app.
     */
    private void fillFieldWithApp(Map<String, Field> fieldMap, Map<String, AppDo> app) {
        for (Map.Entry<String, AppDo> entry : app.entrySet()) {
            String name = entry.getKey();
            AppDo pkg = entry.getValue();
            Field field = fieldMap.get(name);
            BeanUtils.copyProperties(pkg, field);
            field.getPkgIds().put("IMAGE", pkg.getPkgId());
            field.getTags().add("IMAGE");
            field.setIconUrl(pkg.getIconUrl());

            String maintainer = pkg.getMaintainerId();
            if (!StringUtils.isBlank(maintainer)) {
                field.getMaintianers().put("IMAGE", maintainer);
            }
        }
    }

    /**
     * get field pkg of each os.
     * @param rpm rpm.
     * @param epkg epkg.
     * @param app app.
     * @return list of field pkg.
     */
    private List<Field> getFieldsPerOs(Map<String, RPMPackageDO> rpm, Map<String, EpkgDo> epkg,
            Map<String, AppDo> app) {
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

    /**
     * convert rpm, epkg, app to field pkg.
     * @param rpm rpm.
     * @param epkg epkg.
     * @param app app.
     * @return list of field pkg.
     */
    private List<Field> mapToField(Map<String, Map<String, RPMPackageDO>> rpm, Map<String, Map<String, EpkgDo>> epkg,
            Map<String, Map<String, AppDo>> app) {
        Set<String> set = new HashSet<>();
        set.addAll(rpm.keySet());
        set.addAll(epkg.keySet());
        set.addAll(app.keySet());

        List<Field> fList = new ArrayList<>();
        for (String os : set) {
            List<Field> osList = getFieldsPerOs(rpm.get(os), epkg.get(os), app.get(os));
            fList.addAll(osList);
        }
        return fList;
    }

    /**
     * run the program.
     */
    public void run() {
        Set<String> existedPkgs = fieldGateway.getPkgIds();

        Map<String, Map<String, RPMPackageDO>> rpm = getRPMList();

        Map<String, Map<String, EpkgDo>> epkg = getEpkgList();
        Map<String, Map<String, AppDo>> app = getAppList();

        List<Field> fList = mapToField(rpm, epkg, app);

        fieldGateway.saveAll(fList, existedPkgs);

        log.info("finish field pkg");
    }
}
