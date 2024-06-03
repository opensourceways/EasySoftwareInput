package com.easysoftwareinput.common.utils;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalTimeSerializer;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.TimeZone;


/**
 * Jackson工具类.
 */
public final class ObjectMapperUtil {

    /**
     * objectMapper.
     */
    private static ObjectMapper objectMapper = null;

    // Private constructor to prevent instantiation of the utility class
    private ObjectMapperUtil() {
        // private constructor to hide the implicit public one
        throw new AssertionError("ClientUtil class cannot be instantiated.");
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

    /**
     * writeValueAsString.
     * @param obj object.
     * @return string.
     */
    public static String writeValueAsString(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException var2) {
            throw new RuntimeException("JSON 转化异常！");
        }
    }

    /**
     * string to object.
     * @param <T> generic type.
     * @param json sting,
     * @param valueType class object.
     * @return generic type.
     */
    public static <T> T jsonToObject(String json, Class<T> valueType) {
        try {
            return objectMapper.readValue(json, valueType);
        } catch (Exception e) {
            throw new RuntimeException("字符串转化Java对象时异常");
        }
    }

    /**
     * convert string to object.
     * @param <T> generic type.
     * @param clazz class.
     * @param json string.
     * @return generic object.
     */
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

    /**
     * convert byte array to object.
     * @param <T> generic type.
     * @param clazz class.
     * @param bytes byte array.
     * @return generic type.
     */
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

    /**
     * convert string to jsonnode.
     * @param content string.
     * @return jsonnode.
     */
    public static JsonNode toJsonNode(String content) {
        try {
            return objectMapper.readTree(content);
        } catch (JsonProcessingException var2) {
            return null;
        }
    }

    /**
     * convert object to map.
     * @param <T> generic type.
     * @param obj object.
     * @return map.
     */
    public static <T> Map<String, T> jsonToMap(Object obj) {
        Map<String, T> res = objectMapper.convertValue(obj, new TypeReference<Map<String, T>>() { });
        return res;
    }
}
