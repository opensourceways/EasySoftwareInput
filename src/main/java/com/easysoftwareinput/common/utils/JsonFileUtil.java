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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;


public final class JsonFileUtil {
    private JsonFileUtil() {
        // private constructor to hide the implicit public one
        throw new AssertionError("JsonFileUtil class cannot be instantiated.");
    }

    /**
     * logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(JsonFileUtil.class);

    /**
     * read file to string.
     *
     * @param name filename.
     * @return String.
     */
    public static String read(String name) {
        BufferedReader bufferedReader = null;
        try {
            bufferedReader = new BufferedReader(
                    new InputStreamReader(JsonFileUtil.class.getClassLoader().getResourceAsStream(name), "UTF-8"));
            StringBuilder stringBuilder = new StringBuilder();
            String line = null;
            while ((line = bufferedReader.readLine()) != null) {
                stringBuilder.append(line);
            }
            return stringBuilder.toString();
        } catch (Exception e) {
            LOGGER.error("JsonFileUtil 文件读取错误：", e.getMessage());
            return "";
        } finally {
            if (bufferedReader != null) {
                try {
                    bufferedReader.close();
                } catch (IOException e) {
                    LOGGER.error("JsonFileUtil 关流错误：", e.getMessage());
                }
            }
        }
    }
}
