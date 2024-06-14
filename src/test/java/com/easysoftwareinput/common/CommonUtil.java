package com.easysoftwareinput.common;

import java.util.Map;

public class CommonUtil {
    /**
     * whether the maps are equal.
     * @param map1 map1.
     * @param map2 map2.
     * @return boolean.
     */
        public static boolean assertEqualMap(Map<String, String> map1, Map<String, String> map2) {
        if (map1 == null || map2 == null) {
            return false;
        }
        if (map1.size() != map2.size()) {
            return false;
        }
        for (Map.Entry<String, String> entry1 : map1.entrySet()) {
            String key = entry1.getKey();
            String va1 = entry1.getValue();
            String va2 = map2.get(key);

            if (va1 == null) {
                if (va2 != null) {
                    return false;
                }
            } else {
                if (!va1.equals(va2)) {
                    return false;
                }
            }
        }
        return true;
    }
}
