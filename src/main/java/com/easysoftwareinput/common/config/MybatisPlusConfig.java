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

package com.easysoftwareinput.common.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import com.baomidou.mybatisplus.core.config.GlobalConfig;

@EnableTransactionManagement
@Configuration
@MapperScan("com.easysoftwareinput.infrastructure.mapper")
public class MybatisPlusConfig {
    /**
     * create a mybatis injector.
     * @return a MySqlInjector.
     */
    @Bean
    public MySqlInjector mySqlInjector() {
        return new MySqlInjector();
    }

    /**
     * config the mybatis.
     * @return globalconfig.
     */
    @Bean
    public GlobalConfig globalConfiguration() {
        GlobalConfig globalConfig = new GlobalConfig();
        globalConfig.setSqlInjector(mySqlInjector());
        return globalConfig;
    }
}
