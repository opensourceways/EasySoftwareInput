package com.example.demo;

import java.util.List;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

import com.baomidou.mybatisplus.autoconfigure.DdlApplicationRunner;
import com.example.service.ParseSrcPkg;
import com.example.service.ParseXml;
import com.gitee.sunchenbin.mybatis.actable.manager.handler.StartUpHandler;

@SpringBootApplication
@ComponentScan(basePackages = {"com.example.*"})
@MapperScan("com.example.mapper")
@MapperScan("com.gitee.sunchenbin.mybatis.actable.dao.*")
@ComponentScan(basePackages = { "com.gitee.sunchenbin.mybatis.actable.manager.*"})
public class DemoApplication {

    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(DemoApplication.class, args);

        //启动mybatisplus actable
        // StartUpHandler bean = (StartUpHandler) context.getBean(StartUpHandler.class, args);
        // bean.startHandler();

        // 解析 /repodata/primary.xml
        ParseXml parseXml = (ParseXml) context.getBean("parseXml");
        parseXml.run();

        // 解析 /repodata中的源码包(即问价名后缀为.src.rpm的包)
        // ParseSrcPkg parseSrcPkg = (ParseSrcPkg) context.getBean("parseSrcPkg");
        // parseSrcPkg.run();

	}

	@Bean
    public DdlApplicationRunner ddlApplicationRunner(@Autowired(required = false) List ddlList) {
        return new DdlApplicationRunner(ddlList);
    }

}
