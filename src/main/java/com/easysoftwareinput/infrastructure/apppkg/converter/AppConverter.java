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

package com.easysoftwareinput.infrastructure.apppkg.converter;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;
import com.easysoftwareinput.domain.apppackage.model.AppPackage;
import com.easysoftwareinput.infrastructure.apppkg.dataobject.AppDo;

@Component
public class AppConverter {
    /**
     * pick order.
     */
    private static final List<String> ORDER = new ArrayList<>() { { add("aarch64"); add("x86_64");
            add("riscv64"); add("loongarch64"); } };

    /**
     * convert app pkg to app data object.
     * @param aList list of app pkgs.
     * @return list of app data objects.
     */
    public List<AppDo> toDo(List<AppPackage> aList) {
        List<AppDo> oList = new ArrayList<>();
        for (AppPackage pkg : aList) {
            AppDo o = toDo(pkg);
            if (o != null) {
                oList.add(o);
            }
        }
        return oList;
    }

    /**
     * convert app pkg to app data object.
     * @param a pkg.
     * @return data object.
     */
    public AppDo toDo(AppPackage a) {
        AppDo o = new AppDo();
        BeanUtils.copyProperties(a, o);
        return o;
    }

    /**
     * convert list to value of map, key is os.
     * @param doMap map.
     * @param doList list.
     * @param os os.
     */
    public void convertToMap(Map<String, Map<String, AppDo>> doMap, List<AppDo> doList, String os) {
        Map<String, AppDo> curMap = doMap.get(os);
        if (curMap == null) {
            curMap = new HashMap<>();
            doMap.put(os, curMap);
        }

        List<AppDo> filterList = filterDuplicate(doList);

        for (AppDo pkg : filterList) {
            String name = pkg.getName();
            curMap.put(name, pkg);
        }
    }

    /**
     * remote the duplicated app data object.
     * @param doList list of app data object.
     * @return list of app data object.
     */
    private List<AppDo> filterDuplicate(List<AppDo> doList) {
        Map<String, List<AppDo>> map = groupByName(doList);

        List<AppDo> singleList = new ArrayList<>();
        for (List<AppDo> list : map.values()) {
            singleList.add(getLatest(list));
        }
        return singleList;
    }

    /**
     * list of app data object group by name.
     * @param doList list of app data object.
     * @return map.
     */
    private Map<String, List<AppDo>> groupByName(List<AppDo> doList) {
        Map<String, List<AppDo>> map = new HashMap();
        for (AppDo pkg : doList) {
            String name = pkg.getName();
            List<AppDo> list = map.get(name);
            if (list == null) {
                list = new ArrayList<>();
                map.put(name, list);
            }
            list.add(pkg);
        }
        return map;
    }

    /**
     * get the latest app from list.
     * @param list list.
     * @return the latest app.
     */
    private  AppDo getLatest(List<AppDo> list) {
        Integer size = list.size();
        if (size == 0) {
            return null;
        } else if (size == 1) {
            return list.get(0);
        } else {
            return pickLatest(list);
        }
    }

    /**
     * pick the latest pkg.
     * @param list list of app.
     * @return the latest pkg.
     */
    private AppDo pickLatest(List<AppDo> list) {
        List<AppDo> sort = list.stream().sorted(
                Comparator.comparing(AppDo::getAppVer, Comparator.reverseOrder())
                .thenComparing(pkg -> ORDER.indexOf(pkg.getArch()), Comparator.reverseOrder())
        ).collect(Collectors.toList());
        return sort.get(0);
    }
}
