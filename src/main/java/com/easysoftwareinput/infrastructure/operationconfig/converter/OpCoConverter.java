package com.easysoftwareinput.infrastructure.operationconfig.converter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;

import com.easysoftwareinput.domain.operationconfig.model.OpCo;
import com.easysoftwareinput.infrastructure.operationconfig.dataobject.OpCoDo;

public class OpCoConverter {
    public static List<OpCoDo> toDo(List<OpCo> list) {
        return list.stream().map(OpCoConverter::toDo).collect(Collectors.toList());
    }

    public static OpCoDo toDo(OpCo o) {
        OpCoDo d = new OpCoDo();
        BeanUtils.copyProperties(o, d);
        return d;
    }


    public static List<OpCo> toEntity(List<String> rote, Map<String, List<String>> recommends) {
        System.out.println();
        List<OpCo> res = new ArrayList<>();
        for (int i = 0; i < rote.size(); i++) {
            OpCo opCo = new OpCo();
            opCo.setOrderIndex(String.valueOf(i));
            opCo.setCategorys(rote.get(i));
            opCo.setType("domainPage");
            List<String> recom = getRecommends(recommends, rote.get(i));
            if (recom.size() > 0) {
                opCo.setRecommend(String.join(", ", recom));
            }
            res.add(opCo);
        }
        return res;
    }

    private static List<String> getRecommends(Map<String, List<String>> recommends, String key) {
        List<String> res = new ArrayList<>();
        if (recommends.containsKey(key)) {
            res = recommends.get(key);
        }
        return res;
    }
}
