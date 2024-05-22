package com.easysoftwareinput.infrastructure.epkgpkg.converter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.easysoftwareinput.infrastructure.epkgpkg.dataobject.EpkgDo;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class EpkgConverter {
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

    private List<EpkgDo> filterDuplicate(List<EpkgDo> doList) {
        Map<String, List<EpkgDo>> map = groupByName(doList);

        List<EpkgDo> singleList = new ArrayList<>();
        for (Map.Entry<String, List<EpkgDo>> entry : map.entrySet()) {
            List<EpkgDo> list = entry.getValue();
            singleList.add(getLatest(list));
        }
        return singleList;
    }

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
