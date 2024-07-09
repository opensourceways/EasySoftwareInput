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
        if (services.contains("APP")) {
            AppPackageService app = (AppPackageService) context.getBean(AppPackageService.class);
            app.run();
        }

        if (services.contains("APPVER")) {
            AppVerService appVerService = (AppVerService) context.getBean(AppVerService.class);
            appVerService.run();
        }

        if (services.contains("RPMVER")) {
            RpmVerService rpmVerService = (RpmVerService) context.getBean(RpmVerService.class);
            rpmVerService.run();
        }
    }
}
