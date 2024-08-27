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

package com.easysoftwareinput.easysoftwareinput.rpm;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

import com.easysoftwareinput.common.utils.ObjectMapperUtil;
import com.easysoftwareinput.domain.rpmpackage.ability.RPMPackageConverter;
import com.easysoftwareinput.domain.rpmpackage.model.RPMPackage;

@SpringBootTest
public class RpmRepoTest {
    @Value("${rpm.official}")
    private String official;

    @Autowired
    private RPMPackageConverter converter;

    /**
     * 正常情况：gitee存在仓库
     */
    @Test
    public void test_repo() {
        Map<String, String> defaultRepo = Map.of(
            "type", "openEuler官方仓库",
            "url", official
        );
        RPMPackage pkg = new RPMPackage();
        pkg.setName("Cython");
        pkg.setRepo(ObjectMapperUtil.writeValueAsString(defaultRepo));

        Map<String, String> camelMap = Map.of(
            "rpmSourcerpm", "Cython-0.29.14-2.oe1.src.rpm",
            "arch", "x86_64",
            "osType", "EVERYTHING"
        );

        Set<String> repoNames = Collections.emptySet();
        converter.setPkgRepoAndRepoType(pkg, camelMap, repoNames);
        converter.reSetRepo(pkg, camelMap);
        assertEquals(pkg.getRepo(), ObjectMapperUtil.writeValueAsString(Map.of(
            "type", "openEuler官方仓库",
            "url", official + "Cython"
        )));
    }

    /**
     * 异常情况：不存在gitee仓库
     */
    @Test
    public void test_repo_default() {
        Map<String, String> defaultRepo = Map.of(
            "type", "openEuler官方仓库",
            "url", official
        );
        RPMPackage pkg = new RPMPackage();
        pkg.setName("Cython2");
        pkg.setRepo(ObjectMapperUtil.writeValueAsString(defaultRepo));

        Map<String, String> camelMap = Map.of(
            "rpmSourcerpm", "Cython2-0.29.14-2.oe1.src.rpm",
            "arch", "x86_64",
            "osType", "EVERYTHING"
        );

        Set<String> repoNames = Collections.emptySet();
        converter.setPkgRepoAndRepoType(pkg, camelMap, repoNames);
        converter.reSetRepo(pkg, camelMap);
        assertEquals(pkg.getRepo(), ObjectMapperUtil.writeValueAsString(Map.of(
            "type", "openEuler官方仓库",
            "url", official
        )));
    }
}
