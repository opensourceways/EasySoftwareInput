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
    @Bean
    public MySqlInjector mySqlInjector(){
        return new MySqlInjector();
    }

    @Bean
    public GlobalConfig globalConfiguration() {
        GlobalConfig globalConfig = new GlobalConfig();
        globalConfig.setSqlInjector(mySqlInjector());
        return globalConfig;
    }
}
