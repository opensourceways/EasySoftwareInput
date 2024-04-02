package com.easysoftwareinput.application.epkgpackage;

import java.io.File;
import java.io.IOException;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.Namespace;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import com.easysoftwareinput.application.rpmpackage.HttpService;
import com.easysoftwareinput.application.rpmpackage.PkgService;
import com.easysoftwareinput.common.entity.MessageCode;
import com.easysoftwareinput.domain.epkgpackage.ability.EPKGPackageConverter;
import com.easysoftwareinput.domain.epkgpackage.model.EPKGPackage;
import com.easysoftwareinput.domain.rpmpackage.ability.RPMPackageConverter;
import com.easysoftwareinput.domain.rpmpackage.model.RPMPackage;

import co.elastic.clients.elasticsearch._types.Time;
import lombok.extern.slf4j.Slf4j;

@Service
public class EPKGPackageService {
    private static final Logger logger = LoggerFactory.getLogger(EPKGPackageService.class);

    @Value("${epkg.path}")
    private String epkgPath;

    @Value("${epkg.post.url}")
    String postUrl;

    @Autowired
    Environment env;

    @Autowired
    EPKGAsyncService asyncService;

    @Autowired
    @Qualifier("epkgasyncServiceExecutor")
    ThreadPoolTaskExecutor executor;

    @Value("${async.executor.thread.queue_capacity}")
    private int queueCapacity;

    public void run() {
        Map<String, String> osMes = Map.ofEntries(
            Map.entry("osName", env.getProperty("epkg.os-name")),
            Map.entry("osVer", env.getProperty("epkg.os-ver")),
            Map.entry("osType", env.getProperty("epkg.os-type")),
            Map.entry("baseUrl", env.getProperty("epkg.base-url"))
        );

        SAXReader reader = new SAXReader();
        Document document = null;
        try {
            document = reader.read(epkgPath);
        } catch (DocumentException e) {
            logger.error(MessageCode.EC00016.getMsgEn(), e);
        }
        
        List<Element> pkgs = document.getRootElement().elements();
        for (int i = 0; i < pkgs.size(); i++) {
            logger.info("queue size : {}", executor.getQueueSize());
            if (executor.getQueueSize() > (int) queueCapacity / 2) {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    logger.error(MessageCode.EC00017.getMsgEn(), e);
                }
            }
            Element pkg = pkgs.get(i);
            asyncService.executeAsync(pkg, osMes, i, postUrl);
        }
    }
}

