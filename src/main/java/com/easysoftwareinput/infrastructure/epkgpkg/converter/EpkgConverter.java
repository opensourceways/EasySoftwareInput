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

package com.easysoftwareinput.infrastructure.epkgpkg.converter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.easysoftwareinput.infrastructure.epkgpkg.dataobject.EpkgDo;

@Component
public class EpkgConverter {
    /**
     * convert list to value of map, key is os.
     * @param doMap map.
     * @param doList list.
     * @param os os.
     */
    public void convertToMap(Map<String, Map<String, EpkgDo>> doMap, List<EpkgDo> doList, String os) {
        Map<String, EpkgDo> curMap = doMap.get(os);
        if (curMap == null) {
            curMap = new HashMap<>();
            doMap.put(os, curMap);
        }

        List<EpkgDo> filterList = filterDuplicate(doList);

        for (EpkgDo pkg : doList) {
            String name = pkg.getName();
            curMap.put(name, pkg);
        }
    }

    /**
     * remove the duplicated pkg.
     * @param doList list of pkg.
     * @return list of pkg.
     */
    private List<EpkgDo> filterDuplicate(List<EpkgDo> doList) {
        Map<String, List<EpkgDo>> map = groupByName(doList);

        List<EpkgDo> singleList = new ArrayList<>();
        for (Map.Entry<String, List<EpkgDo>> entry : map.entrySet()) {
            List<EpkgDo> list = entry.getValue();
            singleList.add(getLatest(list));
        }
        return singleList;
    }

    /**
     * group the list of pkgs by name.
     * @param doList the list of pkgs.
     * @return map.
     */
    private Map<String, List<EpkgDo>> groupByName(List<EpkgDo> doList) {
        Map<String, List<EpkgDo>> map = new HashMap();
        for (EpkgDo pkg : doList) {
            String name = pkg.getName();
            List<EpkgDo> list = map.get(name);
            if (list == null) {
                list = new ArrayList<>();
                map.put(name, list);
            }
            list.add(pkg);
        }
        return map;
    }

    /**
     * get the latest pkg.
     * @param list list of pkg.
     * @return the latest pkg.
     */
    private  EpkgDo getLatest(List<EpkgDo> list) {
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
     * get the latest pkg from list of pkg.
     * @param list list of pkg.
     * @return the latest pkg.
     */
    private  EpkgDo pickLatest(List<EpkgDo> list) {
        String ver = list.get(0).getVersion();
        EpkgDo winner = null;
        for (EpkgDo pkg : list) {
            if (pkg.getVersion().compareTo(ver) > 0) {
                winner = pkg;
            }
        }
        return winner;
    }
}
