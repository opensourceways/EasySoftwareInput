package com.easysoftwareinput.easysoftwareinput;

import java.util.List;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableAsync;

import com.baomidou.mybatisplus.autoconfigure.DdlApplicationRunner;
import com.easysoftwareinput.application.rpmpackage.RPMPackageService;

import lombok.extern.slf4j.Slf4j;

@EnableAsync
@SpringBootApplication
@ComponentScan(basePackages = {"com.easysoftwareinput.*"})
@MapperScan("com.easysoftwareinput.infrastructure.mapper")
public class EasysoftwareinputApplication {

	public static void main(String[] args) {
		// SpringApplication.run(EasysoftwareinputApplication.class, args);
		ConfigurableApplicationContext context = SpringApplication.run(EasysoftwareinputApplication.class, args);


		// 2. 解析/repodata/primary.xml文件
		RPMPackageService rpmPackageService = (RPMPackageService) context.getBean(RPMPackageService.class);
		rpmPackageService.run();




	}

	@Bean
    public DdlApplicationRunner ddlApplicationRunner(@Autowired(required = false) List ddlList) {
        return new DdlApplicationRunner(ddlList);
    }
}
