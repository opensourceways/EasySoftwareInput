package com.easysoftwareinput.easysoftwareinput;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import com.easysoftwareinput.application.elasticsearch.SearchAppService;
import com.easysoftwareinput.application.elasticsearch.SearchAppverService;
import com.easysoftwareinput.application.elasticsearch.SearchEpkgService;
import com.easysoftwareinput.application.elasticsearch.SearchRpmService;
import com.easysoftwareinput.application.elasticsearch.SearchFieldService;
import com.easysoftwareinput.application.elasticsearch.SearchOepkgService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;

import com.easysoftwareinput.application.apppackage.AppPackageService;
import com.easysoftwareinput.application.appver.AppVerService;
import com.easysoftwareinput.application.appver.RpmVerService;
import com.easysoftwareinput.application.archnum.ArchNumService;
import com.easysoftwareinput.application.domainpackage.DomainPkgService;
import com.easysoftwareinput.application.epkgpackage.EPKGPackageService;
import com.easysoftwareinput.application.externalos.ExternalOsService;
import com.easysoftwareinput.application.fieldpkg.FieldPkgService;
import com.easysoftwareinput.application.oepkg.OepkgService;
import com.easysoftwareinput.application.operationconfig.OperationConfigService;
import com.easysoftwareinput.application.rpmpackage.BatchServiceImpl;
import com.easysoftwareinput.application.rpmpackage.RPMPackageService;
import com.easysoftwareinput.common.dag.DagTaskExecutor;


@Component
public class TaskWithArgs {
    /**
     * logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(TaskWithArgs.class);

    /**
     * arguments.
     */
    @Autowired
    private ApplicationArguments arguments;

    /**
     * context.
     */
    @Autowired
    private ConfigurableApplicationContext context;

    /**
     * get service.
     *
     * @return list of services.
     */
    public List<String> getService() {
        List<String> services = arguments.getOptionValues("iservice");

        if (services == null || services.isEmpty()) {
            return Collections.emptyList();
        }
        String service1 = services.get(0);

        String[] splits = service1.split(",");
        return Arrays.stream(splits).filter(
                s -> !StringUtils.isBlank(StringUtils.trimToEmpty(s))
        ).collect(Collectors.toList());
    }

    /**
     * exec with args.
     */
    public void execArgs() {
        List<String> services = getService();
        if (services == null || services.isEmpty()) {
            LOGGER.error("unrecognized args: iservice");
            return;
        }

        for (String service : services) {
            execService(service);
        }
    }

    /**
     * exec service.
     *
     * @param service service.
     */
    public void execService(String service) {
        if ("APP".equals(service)) {
            AppPackageService app = (AppPackageService) context.getBean(AppPackageService.class);
            app.run();
        } else if ("APPVER".equals(service)) {
            AppVerService appVerService = (AppVerService) context.getBean(AppVerService.class);
            appVerService.run();
        } else if ("RPMVER".equals(service)) {
            RpmVerService rpmVerService = (RpmVerService) context.getBean(RpmVerService.class);
            rpmVerService.run();
        } else if ("RPM".equals(service)) {
            RPMPackageService rpm = (RPMPackageService) context.getBean(RPMPackageService.class);
            rpm.run();
        } else if ("DOMAIN".equals(service)) {
            DomainPkgService domain = (DomainPkgService) context.getBean(DomainPkgService.class);
            domain.run();
        } else if ("FIELD".equals(service)) {
            FieldPkgService field = (FieldPkgService) context.getBean(FieldPkgService.class);
            field.run();
        } else if ("EPKG".equals(service)) {
            EPKGPackageService epkg = (EPKGPackageService) context.getBean(EPKGPackageService.class);
            epkg.run();
        } else if ("OEPKG".equals(service)) {
            OepkgService oepkg = (OepkgService) context.getBean(OepkgService.class);
            oepkg.run();
        } else if ("ARCHNUM".equals(service)) {
            ArchNumService arch = (ArchNumService) context.getBean(ArchNumService.class);
            arch.run();
        } else if ("OPERATIONCONFIG".equals(service)) {
            OperationConfigService op = (OperationConfigService) context.getBean(OperationConfigService.class);
            op.run();
        } else if ("EXTERNALOS".equals(service)) {
            ExternalOsService ex = (ExternalOsService) context.getBean(ExternalOsService.class);
            ex.run();
        } else if ("MAINTAINER".equals(service)) {
            BatchServiceImpl ma = (BatchServiceImpl) context.getBean(BatchServiceImpl.class);
            ma.run();
        } else if ("arrangement".equals(service)) {
            AppPackageService app = (AppPackageService) context.getBean(AppPackageService.class);
            AppVerService appVerService = (AppVerService) context.getBean(AppVerService.class);
            RpmVerService rpmVerService = (RpmVerService) context.getBean(RpmVerService.class);
            RPMPackageService rpm = (RPMPackageService) context.getBean(RPMPackageService.class);
            DomainPkgService domain = (DomainPkgService) context.getBean(DomainPkgService.class);
            FieldPkgService field = (FieldPkgService) context.getBean(FieldPkgService.class);
            EPKGPackageService epkg = (EPKGPackageService) context.getBean(EPKGPackageService.class);
            OepkgService oepkg = (OepkgService) context.getBean(OepkgService.class);
            ArchNumService arch = (ArchNumService) context.getBean(ArchNumService.class);
            BatchServiceImpl ma = (BatchServiceImpl) context.getBean(BatchServiceImpl.class);
            SearchRpmService searchRpm = (SearchRpmService) context.getBean(SearchRpmService.class);

            SearchAppService searchApp = (SearchAppService) context.getBean(SearchAppService.class);
            SearchAppverService searchAppver = (SearchAppverService) context.getBean(SearchAppverService.class);
            SearchEpkgService searchEpkg = (SearchEpkgService) context.getBean(SearchEpkgService.class);
            SearchFieldService searchField = (SearchFieldService) context.getBean(SearchFieldService.class);
            SearchOepkgService searchOepkg = (SearchOepkgService) context.getBean(SearchOepkgService.class);
            DagTaskExecutor executor = new DagTaskExecutor();
            executor.addTaskObj("APP", app);
            executor.addTaskObj("APPVER", appVerService);
            executor.addTaskObj("RPMVER", rpmVerService);
            executor.addTaskObj("RPM", rpm);
            executor.addTaskObj("DOMAIN", domain);
            executor.addTaskObj("FIELD", field);
            executor.addTaskObj("EPKG", epkg);
            executor.addTaskObj("OEPKG", oepkg);
            executor.addTaskObj("ARCH", arch);
            executor.addTaskObj("MAINTAINER", ma);

            executor.addTaskObj("searchRpm", searchRpm);
            executor.addTaskObj("searchApp", searchApp);
            executor.addTaskObj("searchAppver", searchAppver);
            executor.addTaskObj("searchEpkg", searchEpkg);
            executor.addTaskObj("searchField", searchField);
            executor.addTaskObj("searchOepkg", searchOepkg);


            executor.addDependency("APP", "ARCH");
            executor.addDependency("RPM", "ARCH");
            executor.addDependency("FIELD", "ARCH");
            executor.addDependency("EPKG", "ARCH");
            executor.addDependency("OEPKG", "ARCH");

            executor.addDependency("FIELD", "DOMAIN");

            executor.addDependency("APP", "FIELD");
            executor.addDependency("RPM", "FIELD");

            executor.addDependency("MAINTAINER", "RPM");
            executor.addDependency("APP", "APPVER");
            executor.addDependency("RPM", "RPMVER");

            executor.addDependency("APP", "searchApp");
            executor.addDependency("RPM", "searchRpm");
            executor.addDependency("APPVER", "searchAppver");
            executor.addDependency("EPKG", "searchEpkg");
            executor.addDependency("FIELD", "searchField");
            executor.addDependency("OEPKG", "searchOepkg");

            executor.dependencyCheck();
            executor.executeConcurrent();
        } else {
            LOGGER.info("unrecognized service: {}", service);
        }
    }
}
