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

package com.easysoftwareinput.common.constant;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public final class MapConstant {
    // Private constructor to prevent instantiation of the utility class
    private MapConstant() {
        // private constructor to hide the implicit public one
        throw new AssertionError("ClientUtil class cannot be instantiated.");
    }

    /**
     * category map.
     */
    public static final Map<String, String> CATEGORY_MAP;

    /**
     * maintainer map.
     */
    public static final Map<String, String> MAINTAINER;

    /**
     * app category map.
     */
    public static final Map<String, String> APP_CATEGORY_MAP;

    /**
     * key is the pkg name, value is the repo, for repo_pkg_name table.
     */
    public static final Map<String, String> PKG_REPO_MAP;

    static {
        Map<String, String> categoryMap = new HashMap<>();
        categoryMap.put("bigdata", "大数据");
        categoryMap.put("ai", "AI");
        categoryMap.put("Storage", "分布式存储");
        categoryMap.put("sig-CloudNative", "云服务");
        categoryMap.put("sig-HPC", "HPC");
        categoryMap.put("Other", "其他");
        CATEGORY_MAP = Collections.unmodifiableMap(categoryMap);
    }

    static {
        Map<String, String> categoryMap = new HashMap<>();
        categoryMap.put("bigdata", "大数据");
        categoryMap.put("ai", "AI");
        categoryMap.put("storage", "分布式存储");
        categoryMap.put("database", "数据库");
        categoryMap.put("cloud", "云服务");
        categoryMap.put("hpc", "HPC");
        categoryMap.put("others", "其他");
        APP_CATEGORY_MAP = Collections.unmodifiableMap(categoryMap);
    }

    static {
        Map<String, String> maintainerMap = new HashMap<>();
        maintainerMap.put("id", "");
        maintainerMap.put("email", "");
        maintainerMap.put("gitee_id", "");
        MAINTAINER = Collections.unmodifiableMap(maintainerMap);
    }

    static {
        Map<String, String> pkgRepoMap = new HashMap<>();
        pkgRepoMap.put("NestOS-kernel", "nestos-kernel");
        pkgRepoMap.put("clang12", "clang");
        pkgRepoMap.put("clang15", "clang-15");
        pkgRepoMap.put("compiler-rt12", "compiler-rt");
        pkgRepoMap.put("llvm-libunwind12", "llvm-libunwind");
        pkgRepoMap.put("llvm12", "llvm");
        pkgRepoMap.put("libomp12", "libomp");

        pkgRepoMap.put("kernel-visionfive-devel", "kernel");
        pkgRepoMap.put("kernel-hck-devel", "kernel");
        pkgRepoMap.put("kernel-hck", "kernel");
        pkgRepoMap.put("kernel-d1-official-devel", "kernel");
        pkgRepoMap.put("patch-kernel-5.10.0-153.12.0.92.oe2203sp2-ACC", "kernel");
        pkgRepoMap.put("patch-kernel-5.10.0-182.0.0.95.oe2203sp3-ACC", "kernel");
        pkgRepoMap.put("kernel-unmatched-devel", "kernel");
        pkgRepoMap.put("kernel-d1-devel", "kernel");
        pkgRepoMap.put("kernel-visionfive-source", "kernel");
        pkgRepoMap.put("kernel-d1-official-source", "kernel");
        pkgRepoMap.put("kernel-d1-official-headers", "kernel");
        pkgRepoMap.put("patch-kernel-5.10.0-136.12.0.86.oe2203sp1-ACC", "kernel");
        pkgRepoMap.put("patch-kernel-4.19.90-2312.1.0.0255.oe2003sp4-ACC", "kernel");
        pkgRepoMap.put("kernel-hck-debuginfo", "kernel");
        pkgRepoMap.put("kernel-visionfive", "kernel");
        pkgRepoMap.put("kernel-d1", "kernel");
        pkgRepoMap.put("kernel-d1-headers", "kernel");
        pkgRepoMap.put("kernel-unmatched-source", "kernel");
        pkgRepoMap.put("kernel-hck-debugsource", "kernel");
        pkgRepoMap.put("patch-kernel-4.19.90-2112.8.0.0131.oe1-ACC", "kernel");
        pkgRepoMap.put("kernel-d1-official", "kernel");
        pkgRepoMap.put("kernel-hck-headers", "kernel");
        pkgRepoMap.put("kernel-unmatched-headers", "kernel");
        pkgRepoMap.put("kernel-visionfive-headers", "kernel");
        pkgRepoMap.put("kernel-unmatched", "kernel");
        pkgRepoMap.put("patch-kernel-4.19.90-2207.2.0.0158.oe1-ACC", "kernel");
        pkgRepoMap.put("kernel-d1-source", "kernel");

        pkgRepoMap.put("ros-noetic-catkin", "ros_comm");
        pkgRepoMap.put("ros-humble-octovis", "ros_comm");
        pkgRepoMap.put("ros-humble-dynamic-edt-3d", "ros_comm");
        pkgRepoMap.put("ros-noetic-rosconsole-bridge", "ros_comm");
        pkgRepoMap.put("ros-humble-octovis-debuginfo", "ros_comm");
        pkgRepoMap.put("ros-humble-dynamic-edt-3d-debugsource", "ros_comm");
        pkgRepoMap.put("ros-humble-laser-proc-debugsource", "ros_comm");
        pkgRepoMap.put("ros-noetic-rosconsole-bridge-debugsource", "ros_comm");
        pkgRepoMap.put("ros-humble-can-msgs", "ros_comm");
        pkgRepoMap.put("ros-humble-laser-proc", "ros_comm");
        pkgRepoMap.put("ros-humble-dynamic-edt-3d-debuginfo", "ros_comm");
        pkgRepoMap.put("ros-humble-octomap-debuginfo", "ros_comm");
        pkgRepoMap.put("ros-noetic-laser-proc", "ros_comm");
        pkgRepoMap.put("ros-humble-laser-proc-debuginfo", "ros_comm");
        pkgRepoMap.put("ros-humble-octomap", "ros_comm");
        pkgRepoMap.put("ros-noetic-laser-proc-debuginfo", "ros_comm");
        pkgRepoMap.put("ros-noetic-laser-proc-debugsource", "ros_comm");
        pkgRepoMap.put("ros-humble-octomap-debugsource", "ros_comm");

        pkgRepoMap.put("umdk-urma-debuginfo", "umdk");
        pkgRepoMap.put("umdk-urma-bin", "umdk");
        pkgRepoMap.put("umdk-urma-compat-hns-lib", "umdk");
        pkgRepoMap.put("umdk-urma-tools", "umdk");
        pkgRepoMap.put("umdk-urma-debugsource", "umdk");
        pkgRepoMap.put("umdk-urma-lib", "umdk");
        pkgRepoMap.put("umdk-urma-devel", "umdk");

        pkgRepoMap.put("python-ovs-help", "python-ovsdbapp");
        pkgRepoMap.put("python3-ovs", "python-ovsdbapp");

        pkgRepoMap.put("python3-appdirs", "python-appdirs");

        pkgRepoMap.put("python3-keras-applications", "python-Keras");

        pkgRepoMap.put("containerd.io", "containerd");

        pkgRepoMap.put("lustre-client", "lustre");

        PKG_REPO_MAP = Collections.unmodifiableMap(pkgRepoMap);
    }
}
