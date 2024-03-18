package com.easysoftwareinput.common.constant;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class MapConstant {
    public static final Map<String, String> CATEGORY_MAP;
    public static final Map<String, String> MAINTAINER;

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
        Map<String, String> maintainerMap = new HashMap<>();
        maintainerMap.put("id", "openEuler Community");
        maintainerMap.put("email", "contact@openeuler.io");
        maintainerMap.put("gitee_id", "openeuler-ci-bot");
        MAINTAINER = Collections.unmodifiableMap(maintainerMap);
    }
}
