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

package com.easysoftwareinput.easysoftwareinput.field;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.easysoftwareinput.application.fieldpkg.FieldIconUrlService;
import com.easysoftwareinput.domain.fieldpkg.model.FieldIconUrlConfig;
import com.easysoftwareinput.domain.fieldpkg.model.IconUrl;
import com.easysoftwareinput.infrastructure.fieldpkg.FieldGatewayImpl;
import com.easysoftwareinput.infrastructure.fieldpkg.dataobject.FieldDo;

@SpringBootTest
public class FieldIconUrlServiceTest {
    @Autowired
    private FieldIconUrlService service;

    @Autowired
    private FieldGatewayImpl gateway;

    @Autowired
    private FieldIconUrlConfig config;
    /**
     * 正常情况
     */
    @Test
    public void test() {
        initDB();
        service.updateIconUrlList();
        validData();
    }

    /**
     * 异常情况1：断开MySQL服务器
     */
    @Test
    public void test_without_sql() throws Exception {
        // 删除日志
        String logPath = "/var/local/inputeasysoftware/log/spring.log";
        FileUtils.writeStringToFile(new File(logPath), "", StandardCharsets.UTF_8);
        // 初始化数据库
        initDB();
        // 执行业务
        service.updateIconUrlList();
        // 验证数据

        validLog(logPath, "fail update, name: ");
    }

    /**
     * 异常情况2：配置文件中没有配置名称
     */
    @Test
    public void test_without_iconurl() throws Exception {
        String logPath = "/var/local/inputeasysoftware/log/spring.log";
        FileUtils.writeStringToFile(new File(logPath), "", StandardCharsets.UTF_8);
        // 初始化数据库
        initDB();
        // 执行业务
        service.updateIconUrlList();
        // 验证数据
        validLog(logPath, "no name in config");
    }

    public void validLog(String logPath, String keyLog) throws Exception {
        String log = FileUtils.readFileToString(new File(logPath), StandardCharsets.UTF_8);
        long num = log.lines().filter(line -> line.contains(keyLog)).count();
        assertTrue(num != 0);
    }

    public void validData() {
        List<IconUrl> iconUrlList = config.getIconUrlList();
        Map<String, IconUrl> configUrlMap = iconUrlList.stream().collect(
            Collectors.toMap(IconUrl::getName, Function.identity(), (oldObj, newObj) -> oldObj)
        );
        
        QueryWrapper<FieldDo> wrapper = new QueryWrapper<>();
        wrapper.select("name", "icon_url");
        List<FieldDo> list = gateway.getBaseMapper().selectList(wrapper);
        for (FieldDo pkg : list) {
            IconUrl iconUrl = configUrlMap.get(pkg.getName());
            assertEquals(iconUrl.getUrl(), pkg.getIconUrl());
        }
    }

    public void initDB() {
        Map<String, Object> deleteMap = Map.of("1", "1");
        gateway.getBaseMapper().deleteByMap(deleteMap);
        List<String> names = List.of("VSCode", "eulercopilot-cli", "x2openEuler");
        for (int i = 0; i < names.size(); i++) {
            FieldDo oepkg = new FieldDo();
            oepkg.setPkgIds(String.valueOf(i));
            oepkg.setOs("openEuler-22.09");
            oepkg.setName(names.get(i));
            gateway.getBaseMapper().insert(oepkg);
        }
    }
}
