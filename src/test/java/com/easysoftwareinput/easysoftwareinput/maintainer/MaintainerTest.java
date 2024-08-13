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

package com.easysoftwareinput.easysoftwareinput.maintainer;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.easysoftwareinput.application.rpmpackage.BatchServiceImpl;
import com.easysoftwareinput.common.constant.MapConstant;
import com.easysoftwareinput.domain.maintainer.MaintainerConfig;
import com.easysoftwareinput.infrastructure.BasePackageDO;

@SpringBootTest
public class MaintainerTest {
    private static String localPath = "/var/local/inputeasysoftware/log/spring.log";
    private static String emptyContext = "";

    @Autowired
    private BatchServiceImpl service;

    @Autowired
    private MaintainerConfig config;

    /**
     * 1. 正常情况。
     */
    @Test
    public void test() {
        boolean valid = service.run();
        assertTrue(valid);
    }

    /**
     * 2. 验证数据写入。maintainer和category均不为空
     * @throws IOException 
     */
    @Test
    public void test_data() throws Exception {
        // 清空日志与数据表
        FileUtils.writeStringToFile(new File(localPath), emptyContext, StandardCharsets.UTF_8);
        service.remove(null);

        Map<String, String> mockCategory = Map.of("myName", "myCategory");
        Map<String, Map<String, String>> mockMaintainer = Map.of("myName", Map.of(
            "email", "myEmail", "name", "myId", "gitee_id", "myGiteeId"
        ));
        service.storeData(mockCategory, mockMaintainer);
        Map<String, BasePackageDO> map  =service.getNames();
        BasePackageDO bpDo = map.get("myName");
        // 验证数据表内容为自定义字段
        assertTrue(bpDo.getCategory().equals("myCategory")
                && bpDo.getMaintainerEmail().equals("myEmail")
                && bpDo.getMaintainerGiteeId().equals("myGiteeId")
                && bpDo.getMaintainerId().equals("myId")
                && bpDo.getName().equals("myName"));
    }


    /**
     * 2. 验证数据写入。maintainer和category均为空
     * @throws IOException 
     */
    @Test
    public void test_data_empty() throws Exception {
        // 清空日志与数据表
        FileUtils.writeStringToFile(new File(localPath), emptyContext, StandardCharsets.UTF_8);
        service.remove(null);

        Map<String, String> mockCategory = Map.of("myName", "");
        Map<String, Map<String, String>> mockMaintainer = Map.of("myName", Map.of(
            "email", "", "name", "", "gitee_id", ""
        ));
        service.storeData(mockCategory, mockMaintainer);
        Map<String, BasePackageDO> map  =service.getNames();
        BasePackageDO bpDo = map.get("myName");
        // 验证数据表内容为默认字段
        assertTrue(bpDo.getCategory().equals(MapConstant.CATEGORY_MAP.get("Other"))
                && bpDo.getMaintainerEmail().equals(config.getEmail())
                && bpDo.getMaintainerGiteeId().equals(config.getGiteeId())
                && bpDo.getMaintainerId().equals(config.getId())
                && bpDo.getName().equals("myName"));
    }
}
