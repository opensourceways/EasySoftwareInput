package com.easysoftwareinput.common.constant;

public final class PkgConstant {
    // Private constructor to prevent instantiation of the utility class
    private PkgConstant() {
        // private constructor to hide the implicit public one
        throw new AssertionError("ClientUtil class cannot be instantiated.");
    }

    /**
     * buffer size.
     */
    public static final int BUFFER_SIZE = 4096;
}
