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
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
                setTag(field, "IMAGE", iDataObject);
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
        field.setIconUrl(idaDataObject.getIconUrl());
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
     * refresh by os.
     * @param os          os
     * @param maxCount    data count limit
     * @param nameCountMap analysis data: name and count group by name
     * @param tableMap    table configuration
     */
    public void refreshByOsAsync(String os, int maxCount, Map<String, Integer> nameCountMap,
                                 Map<String, Boolean> tableMap) {
        Set<String> existedPkgs = fieldGateway.getPkgIds(os);
        int maxCountLimit = maxCount;
        String beginName = null;
        String endName = null;
        int size = nameCountMap.size();
        for (Map.Entry<String, Integer> nameCount : nameCountMap.entrySet()) {
            String name = nameCount.getKey();
            Integer count = nameCount.getValue();
            if (StringUtils.isEmpty(beginName)) {
                beginName = name;
            }
            if (count > maxCountLimit || size == 1) {
                endName = size == 1 ? name : endName;
                refreshByNameAndOs(os, beginName, endName, tableMap, existedPkgs);
                maxCountLimit = maxCount - count;
                beginName = name;
            } else {
                maxCountLimit -= count;
            }
            endName = name;
            size--;
        }
        nameCountMap.clear();
        log.info("{} refresh success!", os);
    }

    /**
     * refresh by os and name interval.
     * @param os          os
     * @param beginName   begin of name interval
     * @param endName     end of name interval
     * @param tableMap    table configuration
     * @param existedPkgs exist pkgId collection
     */
    public void refreshByNameAndOs(String os, String beginName, String endName,
                                   Map<String, Boolean> tableMap, Set<String> existedPkgs) {
        boolean rpmEnable = tableMap.get("rpm");
        boolean epkgEnable = tableMap.get("epkg");
        boolean appEnable = tableMap.get("app");

        List<FieldDTO> rpmFieldList = new ArrayList<>();
        if (rpmEnable) {
            List rpmPackageDOList = rpmGateway.getPkg(os, beginName, endName);
            if (!rpmPackageDOList.isEmpty()) {
                rpmFieldList = fieldConverter.toFieldDto(rpmPackageDOList);
            }
            rpmPackageDOList.clear();
        }

        List<FieldDTO> epkgFieldList = new ArrayList<>();
        if (epkgEnable) {
            List epkgDoList = epkgGateway.getPkg(os, beginName, endName);
            if (!epkgDoList.isEmpty()) {
                epkgFieldList = fieldConverter.toFieldDto(epkgDoList);
            }
            epkgDoList.clear();
        }

        List<FieldDTO> appFieldList = new ArrayList<>();
        if (appEnable) {
            List appDoList = appGateway.getPkg(os, beginName, endName);
            if (!appDoList.isEmpty()) {
                appFieldList = fieldConverter.toFieldDto(appDoList);
            }
            appDoList.clear();
        }

        List<Field> fList = mapToField(rpmFieldList, epkgFieldList, appFieldList);
        rpmFieldList.clear();
        epkgFieldList.clear();
        appFieldList.clear();
        fieldGateway.saveAll(fList, existedPkgs);
        existedPkgs.clear();
        fList.clear();
    }

    /**
     * aggregate list data to map : key is os, value is TreeMap; TreeMap key is name, value is count.
     * when both os and name are equal, add the count;
     * use TreeMap with CASE_INSENSITIVE_ORDER to match mysql order
     * @param list list data
     * @return map
     */
    private Map<String, Map<String, Integer>> parseListToMap(List<IDataObject> list) {
        return list.stream().
                filter(IDataObject -> !StringUtils.isEmpty(IDataObject.getOs())).
                collect(Collectors.groupingBy(IDataObject::getOs,
                        Collectors.toMap(IDataObject::getName, IDataObject::getCount, Integer::sum,
                                () -> new TreeMap<>(String.CASE_INSENSITIVE_ORDER))));
    }

    /**
     * run the program.
     */
    public void run() {
        Set<String> osSet = new HashSet<>();
        String updateOs = env.getProperty("field.refresh.os");
        if (StringUtils.isNotEmpty(updateOs)) {
            Arrays.stream(updateOs.split(",")).map(String::trim).forEach(osSet::add);
        } else {
            osSet.addAll(rpmGateway.getOs());
            osSet.addAll(epkgGateway.getOs());
            osSet.addAll(appGateway.getOs());
        }
        if (osSet.isEmpty()) {
            log.info("can not find os that needs to be refreshed!");
            return;
        }

        String limitCountStr = env.getProperty("field.refresh.limitCount");
        if (StringUtils.isEmpty(limitCountStr)) {
            log.error("required max limit count unset!");
            return;
        }

        String tables = env.getProperty("field.refresh.tables");
        if (StringUtils.isEmpty(tables)) {
            log.error("refresh tables unset!");
            return;
        }

        boolean rpmEnable = false;
        boolean epkgEnable = false;
        boolean appEnable = false;
        String[] tableArr = tables.split(",");
        for (String table : tableArr) {
            if (table.trim().equals("rpm")) {
                rpmEnable = true;
            }
            if (table.trim().equals("epkg")) {
                epkgEnable = true;
            }
            if (table.trim().equals("app")) {
                appEnable = true;
            }
        }
        if (!rpmEnable && !epkgEnable && !appEnable) {
            log.error("tables value only supports 'rpm','epkg','app'!");
            return;
        }
        Map<String, Boolean> tableMap = new HashMap<>();
        tableMap.put("rpm", rpmEnable);
        tableMap.put("epkg", epkgEnable);
        tableMap.put("app", appEnable);

        int maxLimitCountByOs = Integer.parseInt(limitCountStr.trim()) / osSet.size();
        List<String> osList = osSet.stream().toList();

        Map<String, Map<String, Integer>> rpmOsNameCountMap = new HashMap<>();
        if (rpmEnable) {
            List rpmPackageDOList = rpmGateway.getNameCountByOs(osList);
            rpmOsNameCountMap = parseListToMap(rpmPackageDOList);
            rpmPackageDOList.clear();
        }
        Map<String, Map<String, Integer>> epkgOsNameCountMap = new HashMap<>();
        if (epkgEnable) {
            List epkgDoList = epkgGateway.getNameCountByOs(osList);
            epkgOsNameCountMap = parseListToMap(epkgDoList);
            epkgDoList.clear();
        }
        Map<String, Map<String, Integer>> appOsNameCountMap = new HashMap<>();
        if (appEnable) {
            List appDoList = appGateway.getNameCountByOs(osList);
            appOsNameCountMap = parseListToMap(appDoList);
            appDoList.clear();
        }

        Map<String, Map<String, Integer>> osNameCountMap =
                Stream.of(rpmOsNameCountMap, epkgOsNameCountMap, appOsNameCountMap)
                        .flatMap(stringMapMap -> stringMapMap.entrySet().stream())
                        .collect(Collectors.toMap(
                                Map.Entry::getKey,
                                Map.Entry::getValue,
                                (map1, map2) -> {
                                    map2.forEach((k, v) -> map1.merge(k, v, Integer::sum));
                                    return map1;
                                }
                        ));
        rpmOsNameCountMap.clear();
        epkgOsNameCountMap.clear();
        appOsNameCountMap.clear();

        String taskCountStr = env.getProperty("field.refresh.taskCount");
        int taskCount = StringUtils.isEmpty(taskCountStr) ? 4 : Integer.parseInt(taskCountStr.trim());
        ExecutorService executorService = Executors.newFixedThreadPool(taskCount);
        for (String os : osSet) {
            Map<String, Integer> nameCountMap = osNameCountMap.get(os);
            executorService.submit(new Runnable() {
                @Override
                public void run() {
                    refreshByOsAsync(os, maxLimitCountByOs, nameCountMap, tableMap);
                }
            });
        }
        executorService.shutdown();
        while (true) {
            if (executorService.isTerminated()) {
                log.info("finish field pkg");
                break;
            }
        }
    }
}
