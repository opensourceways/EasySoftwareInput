package com.easysoftwareinput.application.epkgpackage;

import java.io.File;
import java.io.IOException;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

import org.apache.commons.lang3.StringUtils;
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
import com.power.common.util.StringUtil;
import lombok.extern.slf4j.Slf4j;

@Service
public class EPKGPackageService {
    private static final Logger logger = LoggerFactory.getLogger(EPKGPackageService.class);

    @Value("${epkg.post.url}")
    String postUrl;

    @Autowired
    Environment env;

    @Autowired
    PkgService pkgService;

    @Autowired
    EPKGAsyncService asyncService;

    @Autowired
    @Qualifier("epkgasyncServiceExecutor")
    ThreadPoolTaskExecutor executor;

    private static SAXReader reader = new SAXReader();

    private Map<String, String> initMap(){
        return Map.ofEntries(
            Map.entry("osName", env.getProperty("epkg.os-name")),
            Map.entry("osVer", env.getProperty("epkg.os-ver")),
            Map.entry("osType", env.getProperty("epkg.os-type")),
            Map.entry("baseUrl", env.getProperty("epkg.base-url"))
        );
    }

    private Document parseDocument(String filePath) {
        Document document = null;
        try {
            document = reader.read(filePath);
        } catch (DocumentException e) {
            logger.error(MessageCode.EC00016.getMsgEn());
        }
        return document;
    }

    private Map<String, String> initSrc(List<Element> pkgs, Map<String, String> osMes) {
        Map<String, String> map = new HashMap<>();
        for (Element p : pkgs) {
            Map<String, String> res = pkgService.parseSrc(p, osMes);

            String href = StringUtils.trimToEmpty(res.get("location_href"));
            if (StringUtils.isBlank(href) || ! "src".equals(res.get("arch"))) {
                continue;
            }

            String name = href.split("/")[1];
            String src = osMes.get("baseUrl") + href;
            map.put(name, src);
        }
        return map;
    }

    public void run() {
        String epkgPath = env.getProperty("epkg.path");

        Map<String, String> osMes = initMap();
        Document document = parseDocument(epkgPath);

        List<Element> pkgs = document.getRootElement().elements();
        Map<String, String> srcMap = initSrc(pkgs, osMes);

        int batchSize = 1000;

        for (int i = 0; i < pkgs.size(); i += batchSize) {
            int end = Math.min(i + batchSize, pkgs.size());
            List<Element> eList = pkgs.subList(i, end);

            while (executor.getQueueSize() > 3) {
            }

            asyncService.executeAsync(eList, osMes, i, postUrl, srcMap);
        }

        while (executor.getQueueSize() > 0 || executor.getActiveCount() > 0) {
        }

        logger.info("finish-epkg-package");
    }
}

