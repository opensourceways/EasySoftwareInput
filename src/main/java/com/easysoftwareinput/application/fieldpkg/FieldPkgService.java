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
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import com.easysoftwareinput.domain.fieldpkg.model.Field;
import com.easysoftwareinput.domain.fieldpkg.model.FieldDTO;
import com.easysoftwareinput.domain.rpmpackage.model.RPMPackageDO;
import com.easysoftwareinput.infrastructure.apppkg.AppGatewayImpl;
import com.easysoftwareinput.infrastructure.apppkg.dataobject.AppDo;
import com.easysoftwareinput.infrastructure.epkgpkg.EpkgGatewayImpl;
import com.easysoftwareinput.infrastructure.epkgpkg.dataobject.EpkgDo;
import com.easysoftwareinput.infrastructure.fieldpkg.FieldGatewayImpl;
import com.easysoftwareinput.infrastructure.fieldpkg.converter.FieldConverter;
import com.easysoftwareinput.infrastructure.rpmpkg.IDataObject;
import com.easysoftwareinput.infrastructure.rpmpkg.RpmGatewayImpl;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class FieldPkgService {
    /**
     * order.
     */
    private static final List<Class<?>> ORDER = List.of(AppDo.class, RPMPackageDO.class, EpkgDo.class);
    /**
     * rpm gateway.
     */
    @Autowired
    private RpmGatewayImpl rpmGateway;

    /**
     * field converter.
     */
    @Autowired
    private FieldConverter fieldConverter;

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
     * env.
     */
    @Autowired
    private Environment env;

    /**
     * epkg enable.
     */
    @Value("${epkg.enable}")
    private String epkgEnable;

    /**
     * retry os enable.
     */
    @Value("${filed.retry-os-enable}")
    private String retryOsEnable;

    /**
     * list of retry os.
     */
    @Value("${filed.retry-os}")
    private List<String> retryOsList;

    /**
     * get rpm.
     *
     * @param os os.
     * @return map of rpm.
     */

    private List<FieldDTO> getRPMList(String os) {
        List doList = rpmGateway.getPkg(os);
        return fieldConverter.toFieldDto(doList);
    }

    /**
     * get epkg.
     *
     * @param os os.
     * @return map of epkg.
     */
    private List<FieldDTO> getEpkgList(String os) {
        List doList = epkgGateway.getPkg(os);
        return fieldConverter.toFieldDto(doList);
    }

    /**
     * get app.
     *
     * @param os os.
     * @return map of app.
     */
    private List<FieldDTO> getAppList(String os) {
        List doList = appGateway.getPkg(os);
        return fieldConverter.toFieldDto(doList);
    }

    /**
     * convert rpm, epkg, app to field pkg.
     *
     * @param rpm  rpm.
     * @param epkg epkg.
     * @param app  app.
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
     * convert rpm, epkg, app to field pkg.
     *
     * @param rpm  rpm.
     * @param epkg epkg.
     * @param app  app.
     * @return list of field pkg.
     */
    private List<Field> mapToField(List<FieldDTO> rpm, List<FieldDTO> epkg, List<FieldDTO> app) {
        Map<String, List<FieldDTO>> uniqueMap = new HashMap<>();
        for (List<FieldDTO> list : List.of(rpm, epkg, app)) {
            Map<String, List<FieldDTO>> curMap = list.stream().collect(Collectors.groupingBy(
                pkg -> pkg.getOs() + pkg.getName() + pkg.getArch()
            ));
            putMap(uniqueMap, curMap);
        }

        List<Field> res = new ArrayList<>();
        for (List<FieldDTO> list : uniqueMap.values()) {
            res.add(mergeToOneField(list));
        }
        res = res.stream().filter(pkg -> !Objects.isNull(pkg)).collect(Collectors.toList());
        return res;
    }

    /**
     * merge list of FieldDTO to Field.
     * @param list list of FieldDTO.
     * @return Field.
     */
    public Field mergeToOneField(List<FieldDTO> list) {
        if (list == null || list.isEmpty()) {
            return null;
        }

        Field field = new Field();

        list.sort(Comparator.comparingInt(dto -> ORDER.indexOf(dto.getPkg().getClass())));
        fillField(field, list.get(0).getPkg());

        for (FieldDTO fDto : list) {
            IDataObject iDataObject = fDto.getPkg();
            Class<?> cls = iDataObject.getClass();
            if (RPMPackageDO.class.equals(cls)) {
                setTag(field, "RPM", iDataObject);
            } else if (EpkgDo.class.equals(cls)) {
                setTag(field, "EPKG", iDataObject);
            } else if (AppDo.class.equals(cls)) {
                setTag(field, "APP", iDataObject);
            }
        }

        String defaultIcon = env.getProperty("domain.icon");
        if (StringUtils.isBlank(field.getIconUrl())) {
            field.setIconUrl(defaultIcon);
        }

        return field;
    }

    /**
     * fill the Field with pkg.
     * @param field Field.
     * @param idaDataObject pkg.
     */
    public void fillField(Field field, IDataObject idaDataObject) {
        field.setOs(idaDataObject.getOs());
        field.setArch(idaDataObject.getArch());
        field.setName(idaDataObject.getName());
        if (RPMPackageDO.class.equals(idaDataObject.getClass()) || EpkgDo.class.equals(idaDataObject.getClass())) {
            field.setVersion(idaDataObject.getVersion());
        } else if (AppDo.class.equals(idaDataObject.getClass())) {
            field.setVersion(idaDataObject.getAppVer());
        }
        field.setCategory(idaDataObject.getCategory());
        field.setDescription(idaDataObject.getDescription());
    }

    /**
     * set tag.
     * @param f field.
     * @param tag tag.
     * @param iDataObject pkg.
     */
    public void setTag(Field f, String tag, IDataObject iDataObject) {
        f.getTags().add(tag);
        f.getPkgIds().put(tag, iDataObject.getPkgId());
        f.getMaintianers().put(tag, iDataObject.getMaintainerId());
    }

    /**
     * fill uniqueMap with curMap.
     * @param uniqueMap uniqueMap.
     * @param curMap curMap.
     */
    public void putMap(Map<String, List<FieldDTO>> uniqueMap, Map<String, List<FieldDTO>> curMap) {
        for (Map.Entry<String, List<FieldDTO>> entry : curMap.entrySet()) {
            String key = entry.getKey();
            List<FieldDTO> list = uniqueMap.get(key);
            if (list == null) {
                list = new ArrayList<>();
                uniqueMap.put(key, list);
            }
            list.addAll(entry.getValue());
        }
    }

    /**
     * run the program.
     */
    public void run() {
        List<String> rpmOsList = rpmGateway.getOs();
        List<String> epkgOsList = epkgGateway.getOs();
        List<String> appOsList = appGateway.getOs();

        Set<String> osSet = new HashSet<>();
        osSet.addAll(rpmOsList);
        osSet.addAll(epkgOsList);
        osSet.addAll(appOsList);

        for (String os : osSet) {
            // 是否启动单刷os
            if (retryOsEnable.equals("true") && !retryOsList.contains(os)) {
                continue;
            }
            Set<String> existedPkgs = fieldGateway.getPkgIds(os);

            List<FieldDTO> rpm = getRPMList(os);

            List<FieldDTO> epkg = Collections.emptyList();

            // 是否忽略epkg
            if (epkgEnable.equals("true")) {
                epkg = getEpkgList(os);
            }

            List<FieldDTO> app = getAppList(os);

            List<Field> fList = mapToField(rpm, epkg, app);

            fieldGateway.saveAll(fList, existedPkgs);

            log.info("finish field pkg:" + os);
        }
    }
}
