package com.easysoftwareinput.common.utils;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public final class FieldUtil {
    // Private constructor to prevent instantiation of the utility class
    private FieldUtil() {
        // private constructor to hide the implicit public one
        throw new AssertionError("ClientUtil class cannot be instantiated.");
    }

    /**
     * logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(FieldUtil.class);

    /**
     * get max filed length for list.
     * @param <T> generic type.
     * @param list list.
     * @return map.
     */
    public static <T> Map<String, Integer> getMaxFieldLengthForList(List<T> list) {
        if (list == null || list.size() == 0) {
            LOGGER.info("no list");
            return Collections.emptyMap();
        }

        Map<String, Integer> maxMap = new HashMap<>();
        for (T t : list) {
            Map<String, Integer> curMap = getMaxFieldLengthForObject(t);
            updateMaxMap(maxMap, curMap);
        }
        return maxMap;
    }

    /**
     * update maxMap by curMap.
     * @param maxMap maxMap.
     * @param curMap curMap.
     */
    public static void updateMaxMap(Map<String, Integer> maxMap, Map<String, Integer> curMap) {
        for (Map.Entry<String, Integer> entry : curMap.entrySet()) {
            String key = entry.getKey();
            if (!maxMap.containsKey(key)) {
                maxMap.put(key, entry.getValue());
            }

            if (entry.getValue() > maxMap.get(key)) {
                maxMap.put(key, entry.getValue());
            }
        }
    }

    /**
     * get max length for each field.
     * @param <T> generic type.
     * @param t object.
     * @return map.
     */
    public static <T> Map<String, Integer> getMaxFieldLengthForObject(T t) {
        if (t == null) {
            LOGGER.info("no object");
            return Collections.emptyMap();
        }

        Map<String, Integer> res = new HashMap<>();
        Field[] fields = t.getClass().getDeclaredFields();
        for (Field f : fields) {
            int s = getLength(f, t);
            if (s != -1) {
                res.put(f.getName(), s);
            }
        }
        return res;
    }

    /**
     * get length of current filed.
     * @param f field.
     * @param t object.
     * @return length.
     */
    public static int getLength(Field f, Object t) {
        f.setAccessible(true);
        Object v;
        try {
            v = f.get(t);
        } catch (Exception e) {
            LOGGER.error("can not resolve field: {}, object: {}", f, t);
            return -1;
        }
        if (v == null || !v.getClass().equals(String.class)) {
            return -1;
        }

        String s = (String) v;
        return s.length();
    }
}
