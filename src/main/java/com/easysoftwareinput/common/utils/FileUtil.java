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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jgit.util.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class FileUtil {
    // Private constructor to prevent instantiation of the utility class
    private FileUtil() {
        // private constructor to hide the implicit public one
        throw new AssertionError("ClientUtil class cannot be instantiated.");
    }

    /**
     * logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(YamlUtil.class);

    /**
     * list sub menus.
     * @param dir dir.
     * @return sub menus.
     */
    public static List<String> listSubMenus(String dir) {
        if (StringUtils.isBlank(dir)) {
            LOGGER.error("no dir: {}", dir);
            return Collections.emptyList();
        }

        File f = new File(dir);
        if (!f.isDirectory()) {
            LOGGER.error("no dir: {}", dir);
            return Collections.emptyList();
        }

        File[] files = f.listFiles();
        if (files == null || files.length == 0) {
            LOGGER.error("no file in dir: {}", dir);
            return Collections.emptyList();
        }

        List<String> fileList = new ArrayList<>();
        for (File fi : files) {
            String fp = validFile(fi, dir);
            if (!StringUtils.isBlank(fp)) {
                fileList.add(fp);
            }
        }
        return fileList;
    }

    /**
     * if the repo does not exist, mkdir.
     * @param repo repo.
     */
    public static void mkdirIfUnexist(File repo) {
        if (repo.exists() && repo.isDirectory()) {
            return;
        }

        try {
            FileUtils.mkdirs(repo, true);
        } catch (IOException e) {
            LOGGER.error("fail to create dir: {}", repo.toString());
        }
    }

    /**
     * valid the file.
     * @param f file.
     * @param dir menu.
     * @return the valid file.
     */
    public static String validFile(File f, String dir) {
        String filePath;
        try {
            filePath = f.getCanonicalPath();
        } catch (IOException e) {
            LOGGER.error("fail to valid file: {}", f.toString());
            return null;
        }

        if (!filePath.startsWith(dir)) {
            LOGGER.error("unpermitted file path: {}", f.toString());
            return null;
        }
        return filePath;
    }

    /**
     * parse file name.
     * @param filePath filename.
     * @return strign array..
     */
    public static String[] parseFileName(String filePath) {
        String[] pathSplits = filePath.split(Pattern.quote(File.separator));
        String filename = pathSplits[pathSplits.length - 1];
        return filename.split("_a_");
    }
}
