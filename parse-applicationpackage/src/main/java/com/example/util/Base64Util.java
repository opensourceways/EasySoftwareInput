package com.example.util;

import java.lang.reflect.Field;
import java.util.Base64;

public class Base64Util {
    public static <T> T encode(T obj) {
        Class<?> clazz = obj.getClass();
        Field[] fields = clazz.getDeclaredFields();

        for (Field field : fields) {
            try {
                field.setAccessible(true);

            Object value = field.get(obj);
            if (!(value instanceof String)) {
                continue;
            }
            String originValue = (String) value;
            String base64Value = Base64.getEncoder().encodeToString(originValue.getBytes());
            field.set(obj, base64Value);
            field.setAccessible(false);
            } catch (Exception e) {
            }
        }
        return obj;
    }
}
