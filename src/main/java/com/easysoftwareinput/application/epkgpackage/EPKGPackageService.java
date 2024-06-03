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
import org.springframework.core.env.Environment;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import com.easysoftwareinput.application.rpmpackage.PkgService;
import com.easysoftwareinput.common.entity.MessageCode;

@Service
public class EPKGPackageService {
    /**
     * logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(EPKGPackageService.class);

    /**
     * file path of epkg.
     */
    @Value("${epkg.path}")
    private String epkgPath;

    /**
     * post url.
     */
    @Value("${epkg.post.url}")
    private String postUrl;

    /**
     * env.
     */
    @Autowired
    private Environment env;

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
            Map.entry("osName", env.getProperty("epkg.os-name")),
            Map.entry("osVer", env.getProperty("epkg.os-ver")),
            Map.entry("osType", env.getProperty("epkg.os-type")),
            Map.entry("baseUrl", env.getProperty("epkg.base-url"))
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
        Map<String, String> osMes = initMap();
        Document document = parseDocument(epkgPath);

        List<Element> pkgs = document.getRootElement().elements();
        Map<String, String> srcMap = initSrc(pkgs, osMes);

        for (int i = 0; i < pkgs.size(); i++) {
            while (executor.getQueueSize() > 2) {
                int other = 0; // wait to avoid pushing too much pkg to threadpool queue.
            }

            Element pkg = pkgs.get(i);
            asyncService.executeAsync(pkg, osMes, i, postUrl, srcMap);
        }

        while (executor.getQueueSize() > 0 && executor.getActiveCount() > 0) {
            int other = 0; // until all the thread work finished.
        }

        LOGGER.info("finish-epkg-package");
    }
}

