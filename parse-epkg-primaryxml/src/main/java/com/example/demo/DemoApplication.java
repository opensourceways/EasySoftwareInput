package com.example.demo;

import java.io.UnsupportedEncodingException;
import java.util.List;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

import com.baomidou.mybatisplus.autoconfigure.DdlApplicationRunner;
import com.example.service.epkgpkg.ParseEpkg;
import com.example.service.epkgpkg.ParseEpkgSrcPkg;
import com.gitee.sunchenbin.mybatis.actable.manager.handler.StartUpHandler;

@SpringBootApplication
@ComponentScan(basePackages = {"com.example.*"})
@MapperScan("com.example.mapper")
@MapperScan("com.gitee.sunchenbin.mybatis.actable.dao.*")
@ComponentScan(basePackages = { "com.gitee.sunchenbin.mybatis.actable.manager.*"})
public class DemoApplication {

    public static void main(String[] args) throws UnsupportedEncodingException {
        ConfigurableApplicationContext context = SpringApplication.run(DemoApplication.class, args);

        // 1. 解析 epkg的软件包
        ParseEpkg parseEpkg = (ParseEpkg) context.getBean("parseEpkg");
        
        parseEpkg.run();
        

        // 2. 解析epkg软件包中哪些属于源码包，即文件后缀为.src.rpm
        // ParseEpkgSrcPkg parseSrcPkg = (ParseEpkgSrcPkg) context.getBean("parseSrcPkg");
        // parseSrcPkg.run();

        
	}

	@Bean
    public DdlApplicationRunner ddlApplicationRunner(@Autowired(required = false) List ddlList) {
        return new DdlApplicationRunner(ddlList);
    }

}
