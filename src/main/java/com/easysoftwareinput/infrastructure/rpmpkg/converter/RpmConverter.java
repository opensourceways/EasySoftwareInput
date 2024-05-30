package com.easysoftwareinput.infrastructure.rpmpkg.converter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.easysoftwareinput.domain.rpmpackage.model.RPMPackageDO;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class RpmConverter {
    private static final List<String> ORDER = new ArrayList<>(){{add("x86_64"); add("aarch64");}};

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

    public List<RPMPackageDO> filterDuplicate(List<RPMPackageDO> doList) {
        Map<String, List<RPMPackageDO>> map = groupByName(doList);

        List<RPMPackageDO> singleList = new ArrayList<>();
        for (Map.Entry<String, List<RPMPackageDO>> entry : map.entrySet()) {
            List<RPMPackageDO> list = entry.getValue();
            singleList.add(pickOne(list));
        }
        return singleList;
    }

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

    private  RPMPackageDO pickOne(List<RPMPackageDO> list) {
        Integer size = list.size();
        if (size == 0) {
            return null;
        } else if (size == 1) {
            return list.get(0);
        } else {
            return pickLatest(list);
        }
    }

    // 排序规则：1. 取最新版本 2. 如果版本相同，aarch64>x86_64>other
    private  RPMPackageDO pickLatest(List<RPMPackageDO> list) {
        List<RPMPackageDO> sort = list.stream().sorted(
                Comparator.comparing(RPMPackageDO::getVersion, Comparator.reverseOrder())
                .thenComparing(pkg -> ORDER.indexOf(pkg.getArch()), Comparator.reverseOrder())
        ).collect(Collectors.toList());
        return sort.get(0);
    }
}
