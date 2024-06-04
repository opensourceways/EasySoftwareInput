package com.easysoftwareinput.common.utils;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

import com.easysoftwareinput.common.entity.MessageCode;

public final class YamlUtil {
    // Private constructor to prevent instantiation of the utility class
    private YamlUtil() {
        // private constructor to hide the implicit public one
        throw new AssertionError("ClientUtil class cannot be instantiated.");
    }

    /**
     * logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(YamlUtil.class);

    /**
     * parse yaml file.
     * @param yamlPath file.
     * @return map.
     */
    public static Map<String, Object> parseYaml(String yamlPath) {
        Yaml yaml = new Yaml();

        InputStream inputStream = null;
        Map<String, Object> map = new HashMap<>();
        try {
            inputStream = new FileInputStream(yamlPath);
            map = yaml.load(inputStream);
        } catch (FileNotFoundException e) {
            LOGGER.error(MessageCode.EC0009.getMsgEn(), yamlPath);
        }
        return map;
    }

    /**
     * parse yaml files.
     * @param yamlPaths list of files.
     * @return list of maps.
     */
    public static List<Map<String, Object>> parseYaml(List<String> yamlPaths) {
        List<Map<String, Object>> mapList = new ArrayList<>();
        for (String yamlPath : yamlPaths) {
            Map<String, Object> map = parseYaml(yamlPath);
            mapList.add(map);
        }
        return mapList;
    }
}
