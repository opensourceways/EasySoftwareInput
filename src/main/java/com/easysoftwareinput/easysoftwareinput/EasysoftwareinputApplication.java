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

package com.easysoftwareinput.easysoftwareinput;

import java.util.List;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import com.baomidou.mybatisplus.autoconfigure.DdlApplicationRunner;

@EnableAsync
@EnableRetry
@SpringBootApplication
@ComponentScan(basePackages = {"com.easysoftwareinput.*"})
@MapperScan("com.easysoftwareinput.infrastructure.mapper")
@EnableTransactionManagement
public class EasysoftwareinputApplication {
    /**
     * run the program.
     * @param args args.
     */
    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(EasysoftwareinputApplication.class, args);

        TaskWithArgs task = (TaskWithArgs) context.getBean(TaskWithArgs.class);
        task.execArgs();
        System.exit(0);
    }

    /**
     * slove the issue of mybatis with springboot3.
     * @param ddlList ddlList.
     * @return DdlApplicationRunner.
     */
    @Bean
    public DdlApplicationRunner ddlApplicationRunner(@Autowired(required = false) List ddlList) {
        return new DdlApplicationRunner(ddlList);
    }
}
