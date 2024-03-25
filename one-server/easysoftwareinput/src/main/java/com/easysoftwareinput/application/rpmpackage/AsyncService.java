package com.easysoftwareinput.application.rpmpackage;

import java.util.Map;

import org.dom4j.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import com.easysoftwareinput.application.apppackage.AppPackageService;
import com.easysoftwareinput.domain.rpmpackage.ability.RPMPackageConverter;
import com.easysoftwareinput.domain.rpmpackage.model.RPMPackage;

@Service
public class AsyncService {
    private static final Logger logger = LoggerFactory.getLogger(AsyncService.class);

    @Autowired
    PkgService pkgService;

    @Autowired
    RPMPackageConverter rpmPackageConverter;

    @Autowired
    HttpService httpService;

    @Autowired
    ThreadPoolTaskExecutor executor;

    @Value("${async.executor.thread.queue_capacity}")
    private int queueCapacity;

    @Async("asyncServiceExecutor")
    public void executeAsync(Element ePkg, Map<String, String> osMes, int i, int count, String postUrl) {
        logger.info("thread name: {}, xml index: {}, global index: {}", Thread.currentThread().getName(), i, count);
        Map<String, String> res = pkgService.parsePkg(ePkg, osMes);
        RPMPackage pkg = rpmPackageConverter.toEntity(res);
        httpService.postPkg(pkg, postUrl);
    }
}
