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

package com.easysoftwareinput.domain.externalos.ability;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.easysoftwareinput.domain.externalos.model.ExternalOs;

public final class ExternalOsConverter {
    // Private constructor to prevent instantiation of the utility class
    private ExternalOsConverter() {
        // private constructor to hide the implicit public one
        throw new AssertionError("ClientUtil class cannot be instantiated.");
    }

    /**
     * convert map to list of externalos.
     * @param map map.
     * @return list of externalos.
     */
    public static List<ExternalOs> toEntityList(Map<String, Object> map) {
        List<ExternalOs> list = new ArrayList<>();
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            ExternalOs exOs = toEntity(entry);
            list.add(exOs);
        }
        return list;
    }

    /**
     * convert map to externalos.
     * @param entry map.
     * @return externalos.
     */
    public static ExternalOs toEntity(Map.Entry<String, Object> entry) {
        String key = entry.getKey();
        String value = (String) entry.getValue();
        ExternalOs exOs = new ExternalOs();
        exOs.setOriginOsName("ubuntu");
        exOs.setOriginOsVer("");
        exOs.setOriginPkg(key);
        exOs.setTargetOsName("openEuler");
        exOs.setTargetOsVer("");
        exOs.setTargetPkg(value);
        return exOs;
    }
}
