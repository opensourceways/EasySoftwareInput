package com.easysoftwareinput.infrastructure.apppkg.converter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import com.easysoftwareinput.domain.apppackage.model.AppPackage;
import com.easysoftwareinput.infrastructure.apppkg.dataobject.AppDo;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class AppConverter {
    public List<AppDo> toDo(List<AppPackage> aList) {
        List<AppDo> oList = new ArrayList<>();
        for (AppPackage pkg : aList) {
            oList.add(toDo(pkg));
        }
        return oList;
    }

    public AppDo toDo(AppPackage a) {
        AppDo o = new AppDo();
        BeanUtils.copyProperties(a, o);
        return o;
    }

    public void convertToMap(Map<String, Map<String, AppDo>> doMap, List<AppDo> doList, String os) {
        Map<String, AppDo> curMap = doMap.get(os);
        if (curMap == null) {
            curMap = new HashMap<>();
            doMap.put(os, curMap);
        }

        List<AppDo> filterList = filterDuplicate(doList);

        for (AppDo pkg : doList) {
            String name = pkg.getName();
            curMap.put(name, pkg);
        }
    }

    private List<AppDo> filterDuplicate(List<AppDo> doList) {
        Map<String, List<AppDo>> map = groupByName(doList);

        List<AppDo> singleList = new ArrayList<>();
        for (List<AppDo> list : map.values()) {
            singleList.add(getLatest(list));
        }
        return singleList;
    }

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

    private AppDo pickLatest(List<AppDo> list) {
        String ver = list.get(0).getAppVer();
        AppDo winner = null;
        for (AppDo pkg : list) {
            if (pkg.getAppVer().compareTo(ver) > 0) {
                winner = pkg;
            }
        }
        return winner;
    }

  
}
