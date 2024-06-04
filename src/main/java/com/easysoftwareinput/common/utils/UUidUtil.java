package com.easysoftwareinput.common.utils;

import java.util.UUID;

public final class UUidUtil {
    // Private constructor to prevent instantiation of the utility class
    private UUidUtil() {
        // private constructor to hide the implicit public one
        throw new AssertionError("ClientUtil class cannot be instantiated.");
    }

    /**
     * get uuid which length is 32.
     * @return uuid.
     */
    public static String getUUID32() {
        return UUID.randomUUID().toString().replace("-", "").toLowerCase();
    }
}
