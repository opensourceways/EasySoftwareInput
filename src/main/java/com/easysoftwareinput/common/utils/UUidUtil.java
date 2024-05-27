package com.easysoftwareinput.common.utils;

import java.util.UUID;

public class UUidUtil {
    public static String getUUID32() {
        return UUID.randomUUID().toString().replace("-", "").toLowerCase();
    }
}
