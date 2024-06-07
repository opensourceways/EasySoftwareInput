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
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;
import com.easysoftwareinput.domain.rpmpackage.model.RPMPackageDO;

@Component
public class RpmConverter {
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
            return pickLatestFromList(list);
        }
    }

    /**
     * get latest pkg from list. 排序规则：1. 取最新版本 2. 如果版本相同，aarch64>x86_64>other
     * @param list list of pkg.
     * @return pkg.
     */
    private  RPMPackageDO pickLatestFromList(List<RPMPackageDO> list) {
        List<RPMPackageDO> sort = list.stream().sorted(
                Comparator.comparing(RPMPackageDO::getVersion, Comparator.reverseOrder())
                .thenComparing(pkg -> ORDER.indexOf(pkg.getArch()), Comparator.reverseOrder())
        ).collect(Collectors.toList());
        return sort.get(0);
    }
}
