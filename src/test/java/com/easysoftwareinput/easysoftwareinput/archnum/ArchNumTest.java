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

package com.easysoftwareinput.easysoftwareinput.archnum;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MutablePropertySources;

import com.easysoftwareinput.application.archnum.ArchNumService;
import com.easysoftwareinput.infrastructure.archnum.OsArchNumDO;
import com.easysoftwareinput.infrastructure.epkgpkg.EpkgGatewayImpl;
import com.easysoftwareinput.infrastructure.epkgpkg.dataobject.EpkgDo;

@SpringBootTest
public class ArchNumTest {
    @Autowired
    private ArchNumService archNumService;

    @Autowired
    private EpkgGatewayImpl epkgGateway;

    @Autowired
    private ConfigurableApplicationContext context;

    @Autowired
    private Environment env;

    private void initDB() {
        Map<String, Object> deleteMap = Map.of("1", "1");
        epkgGateway.getBaseMapper().deleteByMap(deleteMap);

        EpkgDo pkg = new EpkgDo();
        pkg.setPkgId("id");
        pkg.setOs("os");
        pkg.setArch("arch");
        pkg.setId("id");
        epkgGateway.getBaseMapper().insert(pkg);
    }

    /**
     * 配置项`epkg.enbale`为`true`则查询epkg的数据。
     * @throws Exception
     */
    @Test
    public void test_true() throws Exception {
        initDB();

        List<OsArchNumDO> list = archNumService.getOsArchNum();
        System.out.println();

        assertTrue(Boolean.parseBoolean(env.getProperty("epkg.enable")));
        assertTrue(containEpkg(list));
    }

    /**
     * 配置项`epkg.enbale`为`false`则不查询epkg的数据。
     * @throws Exception
     */
    @Test
    public void test_false() throws Exception {
        initDB();

        List<OsArchNumDO> list = archNumService.getOsArchNum();
        System.out.println();

        assertFalse(Boolean.parseBoolean(env.getProperty("epkg.enable")));
        assertFalse(containEpkg(list));
    }

    private boolean containEpkg(List<OsArchNumDO> list) {
        OsArchNumDO epkg = list.stream().filter(pkg -> "EPKG".equals(pkg.getType())).findAny().orElse(null);
        return epkg != null;
    }
}
