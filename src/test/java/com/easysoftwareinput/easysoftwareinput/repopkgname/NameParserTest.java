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

package com.easysoftwareinput.easysoftwareinput.repopkgname;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.easysoftwareinput.application.repopkgnamemapper.RepoPkgNameMapperService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.List;

@SpringBootTest
public class NameParserTest {
    @Autowired
    private RepoPkgNameMapperService service;

    /**
     * 测试：无法获取宏
     */
    @Test
    public void test_get_simple_name_with_null() {
        List<String> lines = List.of("%global __provides_exclude_from ^/opt/ros/%{ros_distro}/.*$"
                ,"%define RosPkgName      py-trees-ros"
                , "Name:           ros-%{ros_distro}-%{RosPkgName}");
        String value = service.getSimpleName("ros_distro", lines, "py_trees_ros");
        assertNull(value);
    }

    /**
     * 测试：可以获取宏
     */
    @Test
    public void test_get_simple_name() {
        List<String> lines = List.of("%global javaver         1.%{majorver}.0"
                , "%global majorver 8"
                , "%global origin          openjdk"
                , "Name:    java-%{javaver}-%{origin}");
        String value = service.getSimpleName("javaver", lines, "jdk-1.8.0");
        assertEquals("1.%{majorver}.0", value);
    }
}
