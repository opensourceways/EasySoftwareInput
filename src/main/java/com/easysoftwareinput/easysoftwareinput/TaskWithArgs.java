package com.easysoftwareinput.easysoftwareinput;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

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
import com.easysoftwareinput.application.domainpackage.DomainPkgService;
import com.easysoftwareinput.application.fieldpkg.FieldPkgService;
import com.easysoftwareinput.application.rpmpackage.RPMPackageService;


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
        } else {
            LOGGER.info("unrecognized service: {}", service);
        }
    }
}
