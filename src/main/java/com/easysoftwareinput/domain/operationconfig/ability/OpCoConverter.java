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

package com.easysoftwareinput.domain.operationconfig.ability;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.BeanUtils;

import com.easysoftwareinput.domain.operationconfig.model.OpCo;
import com.easysoftwareinput.infrastructure.opco.dataobject.OpCoDo;

public final class OpCoConverter {
    // Private constructor to prevent instantiation of the utility class
    private OpCoConverter() {
        // private constructor to hide the implicit public one
        throw new AssertionError("ClientUtil class cannot be instantiated.");
    }

    /**
     * convert map to list of OperationConfig object.
     * @param rote rote.
     * @param recommends recommends.
     * @return list of OperationConfig object.
     */
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

    /**
     * get list of String object by key from map.
     * @param recommends map.
     * @param key key.
     * @return list of String object.
     */
    private static List<String> getRecommends(Map<String, List<String>> recommends, String key) {
        List<String> res = new ArrayList<>();
        if (recommends.containsKey(key)) {
            res = recommends.get(key);
        }
        return res;
    }

    /**
     * convert Opco to data object.
     * @param c Opco obejct.
     * @return data object.
     */
    public static OpCoDo toDo(OpCo c) {
        OpCoDo d = new OpCoDo();
        BeanUtils.copyProperties(c, d);
        return d;
    }

    /**
     * convert Opco to data object.
     * @param list list of Opco obejct.
     * @return data object.
     */
    public static List<OpCoDo> toDo(List<OpCo> list) {
        return list.stream().map(OpCoConverter::toDo).collect(Collectors.toList());
    }
}
