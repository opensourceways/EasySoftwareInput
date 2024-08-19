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

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;

import java.util.Collections;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.easysoftwareinput.common.constant.MapConstant;
import com.easysoftwareinput.domain.maintainer.MaintainerConfig;
import com.easysoftwareinput.domain.rpmpackage.ability.RPMPackageConverter;
import com.easysoftwareinput.domain.rpmpackage.model.RPMPackage;
import com.easysoftwareinput.infrastructure.BasePackageDO;
import com.easysoftwareinput.infrastructure.repopkgnamemapper.RepoPkgNameMapperGatewayImpl;
import com.easysoftwareinput.infrastructure.repopkgnamemapper.dataobject.RepoPkgNameDO;

@SpringBootTest
public class SrcRepoTest {
    @Autowired
    private RepoPkgNameMapperGatewayImpl repoGateway;

    @Autowired
    private RPMPackageConverter converter;

    @Autowired
    private MaintainerConfig maintainerConfig;

    /**
     * 流程。
     *     1. 从xml中获取`rpmSourcerpm`字段，此处为`myname-3.3.2-1.oe2403.src.rpm`，并解析为`myname`。
     *     2. 将`myname`作为`name`字段的值在`src_rpeo_pkg`表查`repo_url`字段。此处`repo_url`为`https://gitee.com/src-openEuler/myrepo`，并解析为`myrepo`。
     *     3. 将`myrepo`作为name字段在`base_package_info`表查maintaienr信息。
     * 正常情况：`src_rpeo_pkg`表存在数据，且`base_package_info`表存在数据。
     */
    @Test
    public void test() {
        // 清空数据库
        Map<String, Object> deleteMap = Map.of("1", "1");
        repoGateway.getBaseMapper().deleteByMap(deleteMap);

        // 模拟数据
        mockPkg();
        Map<String, BasePackageDO> maintainers = mockMaintainer("myrepo");
        RPMPackage pkg = mockRpmPkg();
        Map<String, String> camelMap = Map.of("rpmSourcerpm", "myname-3.3.2-1.oe2403.src.rpm");
        converter.setPkgMaintainers(maintainers, pkg, camelMap);
        
        // 验证结果
        assertTrue(pkg.getMaintainerEmail().equals("myemail"));
        assertTrue(pkg.getMaintainerGiteeId().equals("mygiteeid"));
        assertTrue(pkg.getMaintainerId().equals("myid"));
        assertTrue(pkg.getCategory().equals("mycategory"));
    }

    public RPMPackage mockRpmPkg() {
        RPMPackage pkg = new RPMPackage();
        pkg.setArch("nosrc");
        pkg.setName("pkgname");
        return pkg;
    }

    public Map<String, BasePackageDO> mockMaintainer(String name) {
        BasePackageDO pkg = new BasePackageDO();
        pkg.setName(name);
        pkg.setCategory("mycategory");
        pkg.setMaintainerGiteeId("mygiteeid");
        pkg.setMaintainerEmail("myemail");
        pkg.setMaintainerId("myid");
        return Map.of(name, pkg);
    }

    public void mockPkg() {
        RepoPkgNameDO pkg = new RepoPkgNameDO();
        pkg.setName("myname");
        pkg.setOs("myos");
        pkg.setRepoUrl("https://gitee.com/src-openEuler/myrepo");
        pkg.setPkgId(pkg.getRepoUrl() + pkg.getOs() + pkg.getName());
        repoGateway.getBaseMapper().insert(pkg);
    }

    /**
     * 正常情况2。`src_rpeo_pkg`表存在数据，但是`base_package_info`表不存在数据。则maintainer为默认值。
     */
    @Test
    public void test_no_maintainer() {
        // 清空数据库
        Map<String, Object> deleteMap = Map.of("1", "1");
        repoGateway.getBaseMapper().deleteByMap(deleteMap);

        // 模拟数据
        mockPkg();
        Map<String, BasePackageDO> maintainers = Collections.emptyMap();
        RPMPackage pkg = mockRpmPkg();
        Map<String, String> camelMap = Map.of("rpmSourcerpm", "myname-3.3.2-1.oe2403.src.rpm");
        converter.setPkgMaintainers(maintainers, pkg, camelMap);

        // 验证结果
        assertTrue(pkg.getMaintainerEmail().equals(maintainerConfig.getEmail()));
        assertTrue(pkg.getMaintainerGiteeId().equals(maintainerConfig.getGiteeId()));
        assertTrue(pkg.getMaintainerId().equals(maintainerConfig.getId()));
        assertTrue(pkg.getCategory().equals(MapConstant.CATEGORY_MAP.get("Other")));
    }

    /**
     * 正常情况3.`src_rpeo_pkg`表不存在数据，但是`base_package_info`表存在`myname`数据。则maintainer为`myname`对应maintainer。
     */
    @Test
    public void test_no_repo() {
        // 清空数据库
        Map<String, Object> deleteMap = Map.of("1", "1");
        repoGateway.getBaseMapper().deleteByMap(deleteMap);

        // 模拟数据
        Map<String, BasePackageDO> maintainers = mockMaintainer("myname");
        RPMPackage pkg = mockRpmPkg();
        Map<String, String> camelMap = Map.of("rpmSourcerpm", "myname-3.3.2-1.oe2403.src.rpm");
        converter.setPkgMaintainers(maintainers, pkg, camelMap);

        // 验证结果
        assertTrue(pkg.getMaintainerEmail().equals("myemail"));
        assertTrue(pkg.getMaintainerGiteeId().equals("mygiteeid"));
        assertTrue(pkg.getMaintainerId().equals("myid"));
        assertTrue(pkg.getCategory().equals("mycategory"));
    }
}
