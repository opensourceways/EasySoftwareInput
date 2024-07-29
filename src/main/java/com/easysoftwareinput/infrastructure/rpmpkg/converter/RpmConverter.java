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

package com.easysoftwareinput.infrastructure.rpmpkg.converter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.easysoftwareinput.domain.rpmpackage.model.RPMPackageDO;
import com.easysoftwareinput.infrastructure.rpmpkg.IDataObject;

@Component("RPM")
public class RpmConverter implements IConverter {
    /**
     * pick order.
     */
    private static final List<String> ORDER = new ArrayList<>() { { add("aarch64"); add("x86_64");
            add("riscv64"); add("loongarch64"); } };

    /**
     * convert list to value of map, key is os.
     * @param doMap map.
     * @param doList list.
     * @param os os.
     */
    public void convertToMap(Map<String, Map<String, RPMPackageDO>> doMap, List<RPMPackageDO> doList, String os) {
        Map<String, RPMPackageDO> curMap = doMap.get(os);
        if (curMap == null) {
            curMap = new HashMap<>();
            doMap.put(os, curMap);
        }

        List<RPMPackageDO> filterList = filterDuplicate(doList);

        for (RPMPackageDO pkg : filterList) {
            String name = pkg.getName();
            curMap.put(name, pkg);
        }
    }

    /**
     * remove the duplicated pkg.
     * @param doList list of pkg.
     * @return list of pkg.s
     */
    private List<RPMPackageDO> filterDuplicate(List<RPMPackageDO> doList) {
        Map<String, List<RPMPackageDO>> map = groupByName(doList);

        List<RPMPackageDO> singleList = new ArrayList<>();
        for (Map.Entry<String, List<RPMPackageDO>> entry : map.entrySet()) {
            List<RPMPackageDO> list = entry.getValue();
            singleList.add(getLatest(list));
        }
        return singleList;
    }

    /**
     * the list of pkg group by name.
     * @param doList list of pkg.
     * @return map.
     */
    private Map<String, List<RPMPackageDO>> groupByName(List<RPMPackageDO> doList) {
        Map<String, List<RPMPackageDO>> map = new HashMap();
        for (RPMPackageDO pkg : doList) {
            String name = pkg.getName();
            List<RPMPackageDO> list = map.get(name);
            if (list == null) {
                list = new ArrayList<>();
                map.put(name, list);
            }
            list.add(pkg);
        }
        return map;
    }

    /**
     * get latest pkg.
     * @param list list of pkg.
     * @return pkg.
     */
    private  RPMPackageDO getLatest(List<RPMPackageDO> list) {
        int size = list.size();
        if (size == 0) {
            return null;
        } else if (size == 1) {
            return list.get(0);
        } else {
            // return pickLatestFromList(list);
            return null;
        }
    }

    /**
     * pick list of pkgs from list.
     * 排序规则：
     *     1. 根据源排序，os->everything->epol
     *     2. 选取最新版本
     * @param list list of pkg.
     * @return list of pkgs.
     */
    private List<RPMPackageDO> pickLatestFromList(List<RPMPackageDO> list) {
        // List<RPMPackageDO> versionList = getLatestVersion(list);
        // return getPreferSource(versionList);
        return null;
    }

    /**
     * 排序规则：
     *     1. 根据源排序，os->everything->epol
     * @param list list of pkgs.
     * @return list of pkgs.
     */
    public List<RPMPackageDO> getPreferSource(List<RPMPackageDO> list) {
        Map<String, List<RPMPackageDO>> map = list.stream().collect(
            Collectors.groupingBy(RPMPackageDO::getSubPath)
        );

        List<String> osSource = new ArrayList<>();
        List<String> epolSource = new ArrayList<>();
        List<String> everythingSource = new ArrayList<>();

        for (String source : map.keySet()) {
            String upper = source.toUpperCase(Locale.ROOT);
            if (upper.startsWith("OS")) {
                osSource.add(source);
            } else if (upper.startsWith("EPOL")) {
                epolSource.add(source);
            } else if (upper.startsWith("EVERYTHING")) {
                everythingSource.add(source);
            } else {
                int temp = 0;
            }
        }

        if (!osSource.isEmpty()) {
            return osSource.stream().map(s -> map.get(s)).flatMap(List::stream).collect(Collectors.toList());
        } else if (!everythingSource.isEmpty()) {
            return everythingSource.stream().map(s -> map.get(s)).flatMap(List::stream).collect(Collectors.toList());
        } else if (!epolSource.isEmpty()) {
            return epolSource.stream().map(s -> map.get(s)).flatMap(List::stream).collect(Collectors.toList());
        } else {
            return Collections.emptyList();
        }
    }

    /**
     * pick one from list.
     */
    @Override
    public IDataObject pickOneFromList(List<IDataObject> list) {
        List<IDataObject> versionList = getLatestVersion(list);
        List<RPMPackageDO> dList = versionList.stream().
                filter(pkg -> pkg.getClass().equals(RPMPackageDO.class))
                .map(pkg -> (RPMPackageDO) pkg).collect(Collectors.toList());
        List<RPMPackageDO> sourceList = getPreferSource(dList);
        if (sourceList == null || sourceList.isEmpty()) {
            return null;
        } else {
            return sourceList.get(0);
        }
    }

    /**
     * group list by version.
     */
    @Override
    public Map<String, List<IDataObject>> getVersionMap(List<IDataObject> list) {
        return list.stream().collect(Collectors.groupingBy(IDataObject::getVersion));
    }
}
