package com.easysoftwareinput.domain.operationconfig.ability;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.easysoftwareinput.domain.operationconfig.model.OpCo;

public class OpCoConverter {
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
