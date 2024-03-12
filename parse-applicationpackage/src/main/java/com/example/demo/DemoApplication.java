package com.example.demo;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

import com.baomidou.mybatisplus.autoconfigure.DdlApplicationRunner;
import com.example.service.epkgpkg.GitRepo;

@SpringBootApplication
@ComponentScan(basePackages = {"com.example.*"})
public class DemoApplication {

    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(DemoApplication.class, args);

        // 1. 根据仓库更新数据库
        GitRepo gitrRepo = (GitRepo) context.getBean(GitRepo.class);
        gitrRepo.run();
	}

	@Bean
    public DdlApplicationRunner ddlApplicationRunner(@Autowired(required = false) List ddlList) {
        return new DdlApplicationRunner(ddlList);
    }

}
