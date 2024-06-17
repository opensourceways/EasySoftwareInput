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

package com.easysoftwareinput.common.utils;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
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

        Map<String, Object> map;
        try (InputStream inputStream = new FileInputStream(yamlPath)) {
            map = yaml.load(inputStream);
        } catch (IOException e) {
            LOGGER.error(MessageCode.EC0009.getMsgEn(), yamlPath);
            map = Collections.emptyMap();
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
