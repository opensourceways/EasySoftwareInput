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

package com.easysoftwareinput.application.epkgpackage;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import com.easysoftwareinput.application.crawldown.EpkgCrawlDownService;
import com.easysoftwareinput.application.rpmpackage.PkgService;
import com.easysoftwareinput.common.entity.MessageCode;
import com.easysoftwareinput.common.utils.FileUtil;
import com.easysoftwareinput.domain.epkgpackage.model.EpkgConfig;

@Service
public class EPKGPackageService {
    /**
     * logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(EPKGPackageService.class);

    /**
     * crawl service.
     */
    @Autowired
    private EpkgCrawlDownService crawlService;

    /**
     * config.
     */
    @Autowired
    private EpkgConfig config;
    /**
     * pkg service.
     */
    @Autowired
    private PkgService pkgService;

    /**
     * epkg service.
     */
    @Autowired
    private EPKGAsyncService asyncService;

    /**
     * thread pool.
     */
    @Autowired
    @Qualifier("epkgasyncServiceExecutor")
    private ThreadPoolTaskExecutor executor;

    /**
     * thread pool queue capacity.
     */
    @Value("${async.executor.thread.queue_capacity}")
    private int queueCapacity;

    /**
     * xml reader.
     */
    private static SAXReader reader = new SAXReader();

    /**
     * init os.
     * @return map of os.
     */
    private Map<String, String> initMap() {
        return Map.ofEntries(
            Map.entry("osName", config.getOsName()),
            Map.entry("osVer", config.getOsVer()),
            Map.entry("osType", config.getOsType()),
            Map.entry("baseUrl", config.getBaseUrl())
        );
    }

    /**
     * parse each xml file.
     * @param filePath filepath.
     * @return document.
     */
    private Document parseDocument(String filePath) {
        Document document = null;
        try {
            document = reader.read(filePath);
        } catch (DocumentException e) {
            LOGGER.error(MessageCode.EC00016.getMsgEn());
        }
        return document;
    }

    /**
     * parse message of src pkg.
     * @param pkgs epkg.
     * @param osMes os.
     * @return map of src name and src url.
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
            String src = osMes.get("baseUrl") + href;
            map.put(name, src);
        }
        return map;
    }

    /**
     * run the program.
     */
    public void run() {
        crawlService.run();

        List<String> files = FileUtil.listSubMenus(config.getDir());
        String epkgPath = files.get(0);

        Map<String, String> osMes = initMap();
        Document document = parseDocument(epkgPath);

        List<Element> pkgs = document.getRootElement().elements();
        Map<String, String> srcMap = initSrc(pkgs, osMes);

        int batchSize = 5_000;
        for (int i = 0; i < pkgs.size(); i += batchSize) {
            int end = Math.min(i + batchSize, pkgs.size());
            List<Element> eList = pkgs.subList(i, end);
            while (executor.getQueueSize() > 2) {
                int other = 0; // wait to avoid pushing too much pkg to threadpool queue.
            }

            asyncService.executeAsync(eList, osMes, i, srcMap);
        }

        while (executor.getQueueSize() > 0 && executor.getActiveCount() > 0) {
            int other = 0; // until all the thread work finished.
        }

        LOGGER.info("finish-epkg-package");
    }
}

