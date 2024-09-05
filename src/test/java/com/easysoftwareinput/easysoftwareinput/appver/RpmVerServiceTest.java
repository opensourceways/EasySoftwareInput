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

package com.easysoftwareinput.easysoftwareinput.appver;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.easysoftwareinput.application.appver.RpmVerService;
import com.easysoftwareinput.domain.appver.AppVersion;

@SpringBootTest
public class RpmVerServiceTest {
    @Autowired
    private RpmVerService rpmVerService;

    /**
     * 测试：获取fedora监控软件包
     */
    @Test
    public void test() {
        List<AppVersion> list = rpmVerService.addFedoraMonitorPkg();
        assertNotNull(list);
        assertTrue(list.size() != 0);
    }
}
