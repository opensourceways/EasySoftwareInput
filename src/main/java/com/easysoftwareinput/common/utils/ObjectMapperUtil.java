package com.easysoftwareinput.common.utils;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalTimeSerializer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Jackson工具类
 */
public class ObjectMapperUtil {

    private static ObjectMapper objectMapper = null;

    private ObjectMapperUtil() {
    }

    static {
        objectMapper = new ObjectMapper();
        JavaTimeModule javaTimeModule = new JavaTimeModule();
        javaTimeModule.addSerializer(
                LocalDateTime.class,
                new LocalDateTimeSerializer(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        javaTimeModule.addSerializer(
                LocalDate.class, new LocalDateSerializer(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        javaTimeModule.addSerializer(
                LocalTime.class, new LocalTimeSerializer(DateTimeFormatter.ofPattern("HH:mm:ss")));
        objectMapper.setSerializationInclusion(Include.NON_NULL);
        // objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        objectMapper.registerModule(javaTimeModule);
        // 设置时区
        objectMapper.setTimeZone(TimeZone.getTimeZone("Asia/Shanghai"));
        // 忽略空bean转json的错误
        objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        // 设置输入时忽略JSON字符串中存在而Java对象实际没有的属性
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    public static String writeValueAsString(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException var2) {
            throw new RuntimeException("JSON 转化异常！");
        }
    }

    public static <T> T jsonToObject(String json, Class<T> valueType) {
        try {
            return objectMapper.readValue(json, valueType);
        } catch (Exception e) {
            throw new RuntimeException("字符串转化Java对象时异常");
        }
    }

    public static String writeValueAsStringForNull(Object obj) {
        objectMapper
                .getSerializerProvider()
                .setNullValueSerializer(
                        new JsonSerializer<Object>() {
                            @Override
                            public void serialize(Object arg0, JsonGenerator arg1, SerializerProvider arg2)
                                    throws IOException, JsonProcessingException {
                                arg1.writeString("");
                            }
                        });

        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException var2) {
            throw new RuntimeException("JSON 转化异常！");
        }
    }

    public static byte[] toJsonByte(Object obj) {
        try {
            return objectMapper.writeValueAsBytes(obj);
        } catch (Exception var2) {
            throw new RuntimeException("将对象转换为JSON字符串二进制数组错误！！");
        }
    }

    public static Map<String, Object> toMap(String json) {
        try {
            return (Map) objectMapper.readValue(json, Map.class);
        } catch (Exception var2) {
            throw new RuntimeException("json字符串转为map异常！！");
        }
    }

    public static OutputStream toJsonOutStream(Object obj) {
        try {
            OutputStream os = new ByteArrayOutputStream();
            objectMapper.writeValue(os, obj);
            return os;
        } catch (Exception var2) {
            throw new RuntimeException("无法转化为字符串流！！");
        }
    }

    public static <T> T toObject(Class<T> clazz, String json) {
        T obj = null;

        try {
            if (json == null) {
                return null;
            } else {
                obj = objectMapper.readValue(json, clazz);
                return obj;
            }
        } catch (Exception var4) {
            throw new RuntimeException("json字符串转java对象异常！！");
        }
    }

    public static <T> T toObject(Class<T> clazz, byte[] bytes) {
        T obj = null;

        try {
            if (bytes != null && bytes.length != 0) {
                obj = objectMapper.readValue(bytes, clazz);
                return obj;
            } else {
                return null;
            }
        } catch (Exception var4) {
            throw new RuntimeException("二进制转化错误！！");
        }
    }

    public static <T> List<T> toObjectList(Class<T> clazz, String json) {
        try {
            JavaType javaType = objectMapper.getTypeFactory().constructParametricType(List.class, clazz);
            return objectMapper.readValue(json, javaType);
        } catch (IOException var3) {
            throw new RuntimeException("json字符串转为list异常！！");
        }
    }

    public static String toJsonTree(List<ConcurrentHashMap<String, Object>> pageMap, Object... count) {
        List<ConcurrentHashMap<String, Object>> myMap = new ArrayList<>();
        for (ConcurrentHashMap<String, Object> map : pageMap) {
            ConcurrentHashMap<String, Object> tempMap = new ConcurrentHashMap<>();
            String key;
            Object value;
            for (Iterator<String> iterator = map.keySet().iterator();
                 iterator.hasNext();
                 tempMap.put(key.toLowerCase(), value)) {
                key = iterator.next();
                value = map.get(key);
                if ("parentid".equals(key)) {
                    tempMap.put("_parentId", value);
                }
            }
            myMap.add(tempMap);
        }
        Map<String, Object> jsonMap = new HashMap<>(2);
        jsonMap.put("total", count);
        jsonMap.put("rows", myMap);
        try {
            return objectMapper.writeValueAsString(jsonMap);
        } catch (JsonProcessingException var9) {
            throw new RuntimeException("转换json树异常！！");
        }
    }

    public static JsonNode toJsonNode(String content) {
        try {
            return objectMapper.readTree(content);
        } catch (JsonProcessingException var2) {
            return null;
        }
    }

    public static <T> Map<String, T> jsonToMap(Object obj) {
        Map<String, T> res = objectMapper.convertValue(obj, new TypeReference<Map<String, T>>() {});
        return res;
    }
}
