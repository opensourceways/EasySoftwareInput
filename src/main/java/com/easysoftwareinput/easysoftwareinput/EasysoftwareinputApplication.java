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
import com.easysoftwareinput.application.apppackage.AppPackageService;
import com.easysoftwareinput.application.appver.AppVerService;
import com.easysoftwareinput.application.domainpackage.DomainPkgService;
import com.easysoftwareinput.application.epkgpackage.EPKGPackageService;
import com.easysoftwareinput.application.externalos.ExternalOsService;
import com.easysoftwareinput.application.fieldpkg.FieldPkgService;
import com.easysoftwareinput.application.operationconfig.OperationConfigService;
import com.easysoftwareinput.application.rpmpackage.RPMPackageService;


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

        // 1. 解析容器镜像
        AppPackageService appPackageService = (AppPackageService) context.getBean(AppPackageService.class);
        appPackageService.run();

        // 2. 解析openEuler官网rpm软件包
        RPMPackageService rpmPackageService = (RPMPackageService) context.getBean(RPMPackageService.class);
        rpmPackageService.run();

        // 3. 解析epkg软件包
        EPKGPackageService epkgPackageService = (EPKGPackageService) context.getBean(EPKGPackageService.class);
        epkgPackageService.run();

        // 4. 解析operation_config
        OperationConfigService opCoService = (OperationConfigService) context.getBean(OperationConfigService.class);
        opCoService.run();

        // 5. 解析  externalos
        ExternalOsService externalOsService = (ExternalOsService) context.getBean(ExternalOsService.class);
        externalOsService.run();

        // 6. domain数据表
        DomainPkgService domainPkgService = (DomainPkgService) context.getBean(DomainPkgService.class);
        domainPkgService.run();

        // 7. 解析上游兼容性
        AppVerService appVerService = (AppVerService) context.getBean(AppVerService.class);
        appVerService.run();

        // 9. 解析领域应用
        FieldPkgService fieldPkgService = (FieldPkgService) context.getBean(FieldPkgService.class);
        fieldPkgService.run();
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
