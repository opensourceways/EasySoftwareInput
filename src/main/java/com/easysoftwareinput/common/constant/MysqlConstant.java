package com.easysoftwareinput.common.constant;

public final class MysqlConstant {
    // Private constructor to prevent instantiation of the utility class
    private MysqlConstant() {
        // private constructor to hide the implicit public one
        throw new AssertionError("ClientUtil class cannot be instantiated.");
    }

    /**
     * batch length.
     */
    public static final int BATCH_LENGHT = 5000;
}
