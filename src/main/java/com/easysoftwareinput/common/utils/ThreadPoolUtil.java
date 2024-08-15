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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ThreadPoolUtil {
    /**
     * logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(ThreadPoolUtil.class);

    // Private constructor to prevent instantiation of the utility class
    private ThreadPoolUtil() {
        // private constructor to hide the implicit public one
        throw new AssertionError("ClientUtil class cannot be instantiated.");
    }

    /**
     * get result.
     * @param <T> generic type.
     * @param list list.
     * @return list.
     */
    public static <T> List<T> getResult(List<CompletableFuture<T>> list) {
        CompletableFuture.allOf(list.toArray(new CompletableFuture[0])).join();
        List<T> res = new ArrayList<>();
        for (CompletableFuture<T> task : list) {
            try {
                T t  = task.get();
                res.add(t);
            } catch (Exception e) {
                LOGGER.error("can not get result, cause: {}, task: {}", e.getMessage(), task);
            }
        }
        return res;
    }
}
