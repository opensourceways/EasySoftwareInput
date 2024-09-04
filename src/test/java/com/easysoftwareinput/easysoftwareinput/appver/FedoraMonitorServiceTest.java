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

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.easysoftwareinput.application.appver.FedoraMonitorService;
import com.easysoftwareinput.domain.appver.FedoraMonitorVO;

@SpringBootTest
public class FedoraMonitorServiceTest {
    @Autowired
    private FedoraMonitorService service;
    /**
     * 测试：从fedora api获取上游信息。
     */
    @Test
    public void test() {
        String pkgName = "Cython";
        FedoraMonitorVO res = service.getUpstream(pkgName);
        assertNotNull(res);
        assertNotNull(res.getBackend());
        assertNotNull(res.getFirstStableVersion());
    }

    /**
     * 测试：从fedora api获取上游信息。无法获取`python-branca`包，可以获取`branca`包
     */
    @Test
    public void test_without_prefix() {
        String pkgName = "python-branca";
        FedoraMonitorVO res = service.getUpstream(pkgName);
        assertNotNull(res);
        assertNotNull(res.getBackend());
        assertNotNull(res.getFirstStableVersion());
    }

    /**
     * 测试：从fedora api获取上游信息。无法获取`qt6`包，可以获取`qt`包
     */
    @Test
    public void test_without_suffix() {
        String pkgName = "qt6";
        FedoraMonitorVO res = service.getUpstream(pkgName);
        assertNotNull(res);
        assertNotNull(res.getBackend());
        assertNotNull(res.getFirstStableVersion());
    }
}
