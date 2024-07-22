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

package com.easysoftwareinput.common.constant;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public final class MapConstant {
    // Private constructor to prevent instantiation of the utility class
    private MapConstant() {
        // private constructor to hide the implicit public one
        throw new AssertionError("ClientUtil class cannot be instantiated.");
    }

    /**
     * category map.
     */
    public static final Map<String, String> CATEGORY_MAP;

    /**
     * maintainer map.
     */
    public static final Map<String, String> MAINTAINER;

    /**
     * app category map.
     */
    public static final Map<String, String> APP_CATEGORY_MAP;

    static {
        Map<String, String> categoryMap = new HashMap<>();
        categoryMap.put("bigdata", "大数据");
        categoryMap.put("ai", "AI");
        categoryMap.put("Storage", "分布式存储");
        categoryMap.put("sig-CloudNative", "云服务");
        categoryMap.put("sig-HPC", "HPC");
        categoryMap.put("Other", "其他");
        CATEGORY_MAP = Collections.unmodifiableMap(categoryMap);
    }

    static {
        Map<String, String> categoryMap = new HashMap<>();
        categoryMap.put("bigdata", "大数据");
        categoryMap.put("ai", "AI");
        categoryMap.put("storage", "分布式存储");
        categoryMap.put("database", "数据库");
        categoryMap.put("cloud", "云服务");
        categoryMap.put("hpc", "HPC");
        categoryMap.put("others", "其他");
        APP_CATEGORY_MAP = Collections.unmodifiableMap(categoryMap);
    }

    static {
        Map<String, String> maintainerMap = new HashMap<>();
        maintainerMap.put("id", "");
        maintainerMap.put("email", "");
        maintainerMap.put("gitee_id", "");
        MAINTAINER = Collections.unmodifiableMap(maintainerMap);
    }
}
