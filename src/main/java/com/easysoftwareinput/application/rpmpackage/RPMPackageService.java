/* Copyright (c) 2024 openEuler Community
 EasySoftwareInput is licensed under the Mulan PSL v2.
 You can use this software according to the terms and conditions of the Mulan PSL v2.
 You may obtain a copy of Mulan PSL v2 at:
     http://license.coscl.org.cn/MulanPSL2
 THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
 EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
 MERCHANTABILITY OR FIT FOR A PARTICULAR PURPOSE.
 See the Mulan PSL v2 for more details.
*/

package com.easysoftwareinput.application.rpmpackage;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import com.easysoftwareinput.common.entity.MessageCode;
import com.easysoftwareinput.infrastructure.BasePackageDO;
import com.easysoftwareinput.infrastructure.rpmpkg.RpmGatewayImpl;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@Getter
@Setter
public class RPMPackageService {
    /**
     * valid data.
     */
    @Autowired
    private RpmGatewayImpl gateway;

    /**
     * data updated.
     */
    private static AtomicLong count = new AtomicLong(0L);

    /**
     * env.
     */
    @Autowired
    private Environment env;

    /**
     * thread pool.
     */
    @Autowired
    @Qualifier("asyncServiceExecutor")
    private ThreadPoolTaskExecutor executor;

    /**
     * service to parse each file.
     */
    @Autowired
    private MyThreadPool threadPool;

    /**
     * pkg service.
     */
    @Autowired
    private PkgService pkgService;

    /**
     * batch service.
     */
    @Autowired
    private BatchServiceImpl batchService;

    /**
     * list of os in archive1.
     */
    @Value("${rpm.archive1.name}")
    private List<String> archive1;

    /**
     * url of archive1.
     */
    @Value("${rpm.archive1.url}")
    private String arUrl1;

    /**
     * url of archive2.
     */
    @Value("${rpm.archive2.url}")
    private String arUrl2;

     /**
     * dir of rpm.
     */
    @Value("${rpm.dir}")
    private String rpmDir;

    /**
     * xml file parser.
     */
    private static SAXReader reader = new SAXReader();

    /**
     * start time of current service.
     */
    private long startTime;

    /**
     * changed row of table.
     */
    private long rows;

    /**
     * valid the files.
     * @param xmlFiles all xmlfile.
     * @param rpmDir file.
     * @return list of valiated files.
     */
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

    /**
     * valid each file.
     * @param file file.
     * @param rpmDir file path.
     * @return return the filepath.
     */
    private String validFile(File file, String rpmDir) {
        String filePath = "";
        try {
            filePath = file.getCanonicalPath();
        } catch (IOException e) {
            log.error(MessageCode.EC00016.getMsgEn());
            return "";
        }
        if (!filePath.startsWith(rpmDir)) {
            log.error(MessageCode.EC00016.getMsgEn());
            return "";
        }
        return filePath;
    }

    /**
     * list sub menus.
     * @param rpmDir current directory.
     * @return list of files.
     */
    private List<String> listSubMenus(String rpmDir) {
        File fDir = new File(rpmDir);
        if (!fDir.isDirectory()) {
            log.error(MessageCode.EC00016.getMsgEn());
            return Collections.emptyList();
        }
        File[] xmlFiles = fDir.listFiles();
        if (xmlFiles == null || xmlFiles.length == 0) {
            log.error(MessageCode.EC00016.getMsgEn());
            return Collections.emptyList();
        }

        return validFiles(xmlFiles, rpmDir);
    }

    /**
     * parse document of xml file.
     * @param filePath filepath.
     * @return document.
     */
    private Document parseDocument(String filePath) {
        Document document = null;
        try {
            document = reader.read(filePath);
        } catch (DocumentException e) {
            log.error(MessageCode.EC00016.getMsgEn());
        }
        return document;
    }

    /**
     * init the src pkg.
     * @param pkgs pkgs.
     * @param osMes os.
     * @return map of src pkgs.
     */
    private Map<String, String> initSrc(List<Element> pkgs, Map<String, String> osMes) {
        Map<String, String> map = new HashMap<>();
        for (Element p : pkgs) {
            Map<String, String> res = pkgService.parseSrc(p, osMes);

            String href = StringUtils.trimToEmpty(res.get("location_href"));
            if (StringUtils.isBlank(href) || !"src".equals(res.get("arch"))) {
                continue;
            }

            String name = href.split("/")[1];
            String src = osMes.get("baseUrl") + "/" + href;
            map.put(name, src);
        }
        return map;
    }

    /**
     * run the program.
     */
    public void run() {
        if (archive1 == null || archive1.size() == 0
                || StringUtils.isBlank(arUrl1) || StringUtils.isBlank(arUrl2) || StringUtils.isBlank(rpmDir)) {
            log.error("no env");
            return;
        }

        List<String> files = listSubMenus(rpmDir);

        this.setStartTime(System.currentTimeMillis());

        // 获取源码包链接
        Map<String, String> srcUrls = new HashMap<>();
        for (String filePath : files) {
            Map<String, String> osMes = parseFileName(filePath);
            Document document = parseDocument(filePath);
            if (document == null) {
                continue;
            }
            List<Element> pkgs = document.getRootElement().elements();
            Map<String, String> srcMap = initSrc(pkgs, osMes);
            srcUrls.putAll(srcMap);
        }

        // 已经在数据表中的软件包
        Set<String> existedPkgIdSet = gateway.getExistedIds();

        // 获取maintainer信息
        Map<String, BasePackageDO> maintainers = batchService.getNames();
        List<CompletableFuture<Void>> finishedList = new ArrayList<>();
        // 正式处理
        for (int fileIndex = 0; fileIndex < files.size(); fileIndex++) {
            String filePath = files.get(fileIndex);
            Map<String, String> osMes = parseFileName(filePath);

            Document document = parseDocument(filePath);
            if (document == null) {
                continue;
            }

            while (executor.getQueueSize() > 3) {
                int temp = 0;
            }

            CompletableFuture<Void> finished = threadPool.parseXml(document, osMes, fileIndex, srcUrls, maintainers,
                    count, existedPkgIdSet);
            finishedList.add(finished);
        }

        for (CompletableFuture<Void> f : finishedList) {
            try {
                f.get();
            } catch (Exception e) {
                log.error("fail-to-execute-async, e: {}", e.getMessage());
            }
        }

        log.info("finish-rpm-write");
        this.setRows(count.get());
        validData();
        log.info("fnish-rpm-validate");
    }

    /**
     * valid the stored data.
     * @return true if all the data stored.
     */
    public boolean validData() {
        long tableRow = gateway.getChangedRow(this.getStartTime());
        long row = getRows();
        if (tableRow == row) {
            log.info("no error in storing data. need to be stored: {}, stored: {}", row, tableRow);
            return true;
        } else {
            log.error("error in storing data. need to be stored: {}, stored: {}", row, tableRow);
            return false;
        }
    }

    /**
     * parse file name.
     * @param filePath filename.
     * @return map of os.
     */
    private Map<String, String> parseFileName(String filePath) {

        String[] pathSplits = filePath.split(Pattern.quote(File.separator));
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

    /**
     * assemble url.
     * @param nameSplits nameSplits.
     * @return url.
     */
    private String assembleBaseUrl(String[] nameSplits) {
        StringBuilder baseUrl = new StringBuilder();

        if (archive1.contains(nameSplits[0])) {
            baseUrl.append(arUrl1);
        } else {
            baseUrl.append(arUrl2);
        }
        for (int i = 0; i < nameSplits.length - 1; i++) {
            baseUrl.append("/");
            baseUrl.append(nameSplits[i]);
        }
        return baseUrl.toString();
    }
}
