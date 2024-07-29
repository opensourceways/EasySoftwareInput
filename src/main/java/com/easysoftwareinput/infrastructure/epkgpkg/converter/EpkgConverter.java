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

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import com.easysoftwareinput.common.utils.UUidUtil;
import com.easysoftwareinput.domain.epkgpackage.model.EPKGPackage;
import com.easysoftwareinput.infrastructure.epkgpkg.dataobject.EpkgDo;
import com.easysoftwareinput.infrastructure.rpmpkg.IDataObject;
import com.easysoftwareinput.infrastructure.rpmpkg.converter.IConverter;

@Component("EPKG")
public class EpkgConverter implements IConverter {
    /**
     * pick order.
     */
    private static final List<String> ORDER = new ArrayList<>() { { add("aarch64"); add("x86_64");
            add("riscv64"); add("loongarch64"); } };

    /**
     * convert pkg to data object.
     * @param epkg list of pkgs.
     * @return list of data objects.
     */
    public List<EpkgDo> toDo(List<EPKGPackage> epkg) {
        return epkg.stream().map(this::toDo).collect(Collectors.toList());
    }

    /**
     * convert pkg to data object.
     * @param pkg pkg.
     * @return data object.
     */
    public EpkgDo toDo(EPKGPackage pkg) {
        EpkgDo d = new EpkgDo();
        BeanUtils.copyProperties(pkg, d);
        Timestamp currentTime = new Timestamp(System.currentTimeMillis());
        d.setUpdateAt(currentTime);
        d.setCreateAt(currentTime);

        String id = UUidUtil.getUUID32();
        d.setId(id);
        return d;
    }

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

        for (EpkgDo pkg : filterList) {
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
     * get the latest pkg from list of pkg.
     * @param list list of pkg.
     * @return the latest pkg.
     */
    private EpkgDo pickLatestFromList(List<EpkgDo> list) {
        List<EpkgDo> sort = list.stream().sorted(
                Comparator.comparing(EpkgDo::getVersion, Comparator.reverseOrder())
                .thenComparing(pkg -> ORDER.indexOf(pkg.getArch()), Comparator.reverseOrder())
        ).collect(Collectors.toList());
        return sort.get(0);
    }

    /**
     * group list by version.
     */
    @Override
    public Map<String, List<IDataObject>> getVersionMap(List<IDataObject> list) {
        return list.stream().collect(Collectors.groupingBy(IDataObject::getVersion));
    }

    /**
     * pick one from list.
     */
    @Override
    public IDataObject pickOneFromList(List<IDataObject> list) {
        List<IDataObject> versionList = getLatestVersion(list);
        if (versionList == null || versionList.isEmpty()) {
            return null;
        } else {
            return versionList.get(0);
        }
    }
}
