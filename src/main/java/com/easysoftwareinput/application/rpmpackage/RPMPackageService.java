package com.easysoftwareinput.application.rpmpackage;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import com.easysoftwareinput.common.entity.MessageCode;
import com.easysoftwareinput.infrastructure.BasePackageDO;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
// @Profile("rpmpkg")
public class RPMPackageService {
    @Autowired
    Environment env;

    @Autowired
    @Qualifier("asyncServiceExecutor")
    ThreadPoolTaskExecutor executor;

    @Autowired
    MyThreadPool threadPool;

    @Autowired
    PkgService pkgService;

    @Autowired
    BatchServiceImpl batchService;

    private static SAXReader reader = new SAXReader();

    private List<String> validFiles(File[] xmlFiles, String rpmDir) {
        List<String> files = new ArrayList<>();
        for (File file : xmlFiles) {
            String filePath = validFile(file, rpmDir);
            if (StringUtils.isNotBlank(filePath)) {
                files.add(filePath);
            }
        }
        return files;
    }

    private String validFile(File file, String rpmDir) {
        String filePath = "";
        try {
            filePath = file.getCanonicalPath();
        } catch (IOException e) {
            log.error(MessageCode.EC00016.getMsgEn());
            return "";
        }
        if (! filePath.startsWith(rpmDir)) {
            log.error(MessageCode.EC00016.getMsgEn());
            return "";
        }
        return filePath;
    }

    private List<String> listSubMenus(String rpmDir) {
        File fDir = new File(rpmDir);
        if (! fDir.isDirectory()) {
            log.error(MessageCode.EC00016.getMsgEn());
            return Collections.emptyList();
        }
        File[] xmlFiles = fDir.listFiles();

        return validFiles(xmlFiles, rpmDir);
    }

    private Document parseDocument(String filePath) {
        Document document = null;
        try {
            document = reader.read(filePath);
        } catch (DocumentException e) {
            log.error(MessageCode.EC00016.getMsgEn());
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
            String src = osMes.get("baseUrl") + "/" + href;
            map.put(name, src);
        }
        return map;
    }

    private Map<String, String> getSrcUrls(List<String> files) {
        Map<String, String> srcUrls = new HashMap<>();
        for (String filePath : files) {
            if (! filePath.contains("source")) {
                continue;
            }

            Map<String, String> osMes = parseFileName(filePath);
            Document document = parseDocument(filePath);
            if (document == null) {
                continue;
            }

            List<Element> pkgs = document.getRootElement().elements();
            Map<String, String> srcMap = initSrc(pkgs, osMes);
            srcUrls.putAll(srcMap);
        }
        return srcUrls;
    }

    public void run() {
        List<String> files = listSubMenus(env.getProperty("rpm.dir"));

        Map<String, String> srcUrls = getSrcUrls(files);

        // 获取maintainer信息
        Map<String, BasePackageDO> maintainers = batchService.getNames();

        // 正式处理
        for (int fileIndex = 0; fileIndex < files.size(); fileIndex++) {
            String filePath = files.get(fileIndex);
            Map<String, String> osMes = parseFileName(filePath);

            Document document = parseDocument(filePath);
            if (document == null) {
                continue;
            }

            while (executor.getQueueSize() > 3) {
            }

            threadPool.parseXml(document, osMes, fileIndex, srcUrls, maintainers);
        }

        while (executor.getQueueSize() > 0 || executor.getActiveCount() > 0) {
        }

        log.info("finish-rpm-package");
    }

    private Map<String, String> parseFileName(String filePath) {
        String[] pathSplits = filePath.split(File.separator);
        String filename = pathSplits[pathSplits.length - 1];
        String[] nameSplits = filename.split("_a_");

        String osVer = nameSplits[0].replace("openEuler-", "");
        String osType = "";
        try {
            osType = nameSplits[1];
        } catch (Exception e) {
            log.info("nameSplits: {}, filePaht: {}", nameSplits, filePath);
        }

        String baseUrlS = assembleBaseUrl(nameSplits);

        return Map.ofEntries(
            Map.entry("osName", "openEuler"),
            Map.entry("osVer", osVer),
            Map.entry("osType", osType),
            Map.entry("baseUrl", baseUrlS)
        );
    }

    private String assembleBaseUrl(String[] nameSplits) {
        StringBuilder baseUrl = new StringBuilder();
        List<String> archive1 = (List<String>) env.getProperty("rpm.archive1.name", List.class);
        if (archive1.contains(nameSplits[0])) {
            baseUrl.append(env.getProperty("rpm.archive1.url"));
        } else {
            baseUrl.append(env.getProperty("rpm.archive2.url"));
        }
        for (int i = 0; i < nameSplits.length - 1; i++) {
            baseUrl.append("/");
            baseUrl.append(nameSplits[i]);
        }
        return baseUrl.toString();
    }
}
