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

package com.easysoftwareinput.easysoftwareinput.srcrepo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.easysoftwareinput.application.repopkgnamemapper.RepoPkgNameMapperService;
import com.easysoftwareinput.application.rpmpackage.GitRepoBatchService;
import com.easysoftwareinput.domain.repopkgnamemapper.RepoPkg;
import com.easysoftwareinput.domain.repopkgnamemapper.RepoPkgNamePkg;

@SpringBootTest
public class SrcRepoChangedTest {
    @Autowired
    private RepoPkgNameMapperService repoService;

    @Autowired
    private GitRepoBatchService gitService;

    /**
     * 测试：spec文件中name字段以`\t`分隔，仍然获取字段
     */
    @Test
    public void test_name_with_tap() {
        List<String> lines = List.of("Name:		%{package_name}"
                , "%global		package_name ovirt-iso-uploader");
        String name2 = repoService.getSimpleName("package_name", lines, "ovirt-iso-uploader");
        assertEquals(name2, "ovirt-iso-uploader");
    }

    /**
     * 测试：spec文件中值为`%{nil}`的字段为空字符串
     */
    @Test
    public void test_name_with_nil() {
        List<String> lines = List.of("%global gcc_ver %{nil}"
                , "Name: %{?scl_prefix}gcc%{gcc_ver}"
                , "%global scl_prefix gcc-toolset-10-");
        List<String> nameList = repoService.getSpecficName(lines, "gcc-10");
        assertEquals(nameList.get(0), "gcc-toolset-10-gcc");
    }

    /**
     * 测试：name字段中的`ros_distro`解析为`humble`和`noetic`。
     */
    @Test
    public void test_name_with_ros_distro() {
        List<String> lines = List.of("%define RosPkgName      acado-vendor"
                , "Name:           ros-%{ros_distro}-%{RosPkgName}");
        List<String> nameList = repoService.getSpecficName(lines, "acado-vendor");
        Assertions.assertThat(nameList).containsExactly("ros-humble-acado-vendor", "ros-noetic-acado-vendor");
    }

    /**
     * 测试：解析同一个分支下多个spec文件
     */
    @Test
    public void test_name_with_multi_spec() {
        Set<String> repoSet = new HashSet<>();
        repoSet.add("ament_cmake");
        List<RepoPkgNamePkg> repoList = gitService.getAllBranches("src-openEuler", repoSet);
        List<RepoPkgNamePkg> pkgList = gitService.getAllSpecName(repoList);
        boolean valid = false;
        for (RepoPkgNamePkg pkg : pkgList) {
            List<RepoPkg> names = repoService.parseSpecOfEachPkg(pkg);
            if (names.size() > 1) {
                valid = true;
            }
        }
        assertTrue(valid);
    }
}
