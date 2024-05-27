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

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class EpkgConverter {
    private static final List<String> ORDER = new ArrayList<>(){{add("x86_64"); add("aarch64");}};

    public List<EpkgDo> toDo(List<EPKGPackage> epkg) {
        return epkg.stream().map(this::toDo).collect(Collectors.toList());
    }

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

    private List<EpkgDo> filterDuplicate(List<EpkgDo> doList) {
        Map<String, List<EpkgDo>> map = groupByName(doList);

        List<EpkgDo> singleList = new ArrayList<>();
        for (Map.Entry<String, List<EpkgDo>> entry : map.entrySet()) {
            List<EpkgDo> list = entry.getValue();
            singleList.add(pickOne(list));
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

    private  EpkgDo pickOne(List<EpkgDo> list) {
        Integer size = list.size();
        if (size == 0) {
            return null;
        } else if (size == 1) {
            return list.get(0);
        } else {
            return pickLatest(list);
        }
    }

    private EpkgDo pickLatest(List<EpkgDo> list) {
        List<EpkgDo> sort = list.stream().sorted(
                Comparator.comparing(EpkgDo::getVersion, Comparator.reverseOrder())
                .thenComparing(pkg -> ORDER.indexOf(pkg.getArch()), Comparator.reverseOrder())
        ).collect(Collectors.toList());
        return sort.get(0);
    }
}
