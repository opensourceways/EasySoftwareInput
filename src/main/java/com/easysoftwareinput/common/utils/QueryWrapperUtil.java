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

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.easysoftwareinput.common.entity.MessageCode;
import com.power.common.util.StringUtil;


public final class QueryWrapperUtil {
    // Private constructor to prevent instantiation of the utility class
    private QueryWrapperUtil() {
        // private constructor to hide the implicit public one
        throw new AssertionError("ClientUtil class cannot be instantiated.");
    }

    /**
     * logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(QueryWrapperUtil.class);

    /**
     * create querywrapper.
     * @param <T> generic type.
     * @param <U> generic type.
     * @param t object.
     * @param u object.
     * @return querywraper.
     */
    public static <T, U> QueryWrapper<T> createQueryWrapper(T t, U u) {
        QueryWrapper<T> wrapper = new QueryWrapper<>();

        Field[] fields = u.getClass().getDeclaredFields();
        for (Field field: fields) {
            field.setAccessible(true);

            Object value = null;
            try {
                value = field.get(u);
            } catch (Exception e) {
                LOGGER.error(MessageCode.EC00011.getMsgEn(), e);
            }
            if (!(value instanceof String)) {
                continue;
            }

            String vStr = (String) value;
            vStr = StringUtils.trimToEmpty(vStr);
            if (StringUtils.isBlank(vStr)) {
                continue;
            }

            String undLine = StringUtil.camelToUnderline(field.getName());

            //","代表该字段有多个参数
            if (vStr.contains(",")) {
                List<String> items = splitStr(vStr);
                if (items.size() == 0) {
                    continue;
                }
                wrapper.in(undLine, items);
            } else {
                vStr = StringUtils.trimToEmpty(vStr);
                wrapper.eq(undLine, vStr);
            }
        }
        return wrapper;
    }

    /**
     * split strs.
     * @param vStr origin str.
     * @return a list of string.
     */
    private static List<String> splitStr(String vStr) {
        List<String> res = new ArrayList<>();

        String[] sps = StringUtils.split(vStr, ",");
        if (sps.length == 0) {
            return res;
        }

        for (String sp : sps) {
            if (StringUtils.isBlank(StringUtils.trimToEmpty(sp))) {
                continue;
            }
            res.add(sp);
        }
        return res;
    }
}
