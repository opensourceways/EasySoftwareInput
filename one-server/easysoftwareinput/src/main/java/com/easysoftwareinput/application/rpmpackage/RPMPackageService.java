package com.easysoftwareinput.application.rpmpackage;

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
import org.dom4j.io.SAXReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import com.easysoftwareinput.common.entity.MessageCode;
import com.easysoftwareinput.domain.rpmpackage.ability.RPMPackageConverter;
import com.easysoftwareinput.domain.rpmpackage.model.RPMPackage;

import co.elastic.clients.elasticsearch._types.Time;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class RPMPackageService {
    @Value("${rpm.dir}")
    private String rpmDir;

    @Autowired
    Environment env;

    @Autowired
    PkgService pkgService;

    @Autowired
    RPMPackageConverter rpmPackageConverter;

    @Autowired
    HttpService httpService;

    @Autowired
    ThreadPoolTaskExecutor executor;

    @Autowired
    AsyncService asyncService;

    public void run() {
        SAXReader reader = new SAXReader();
        Document document = null;

        Instant start = Instant.now();
        File fDir = new File(rpmDir);
        if (! fDir.isDirectory()) {
            log.error(MessageCode.EC00016.getMsgEn());
            return;
        }
        Instant current = Instant.now();
        log.info(Duration.between(current, start).toMillis() + "");
        File[] xmlFiles = fDir.listFiles();
        int count = 0;
        for (File file : xmlFiles) {
            count += 1;
            String filePath = "";
            try {
                filePath = file.getCanonicalPath();
            } catch (IOException e) {
                log.error(MessageCode.EC00016.getMsgEn());
                return;
            }

            Map<String, String> osMes = parseFileName(filePath);

            try {
                document = reader.read(filePath);
            } catch (DocumentException e) {
                log.error(MessageCode.EC00016.getMsgEn());
            }

            parseXml(document, osMes);

            current = Instant.now();
            log.info(Duration.between(current, start).toSeconds() + " seconds each file, count: " + count);
        }
        current = Instant.now();
        log.info(Duration.between(current, start).toMillis() + "");
    }

    private void parseXml(Document xml, Map<String, String> osMes) {
        List<Element> pkgs = xml.getRootElement().elements();
        for (int i = 0; i < pkgs.size(); i++) {
            if (executor.getQueueSize() > 300) {
                try {
                    Thread.sleep(300);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            Element ePkg = pkgs.get(i);
            asyncService.executeAsync(ePkg, osMes, i);
        }
    }

    private Map<String, String> parseFileName(String filePath) {
        String[] pathSplits = filePath.split("\\\\");
        String filename = pathSplits[pathSplits.length - 1];
        String[] nameSplits = filename.split("_a_");

        String osVer = nameSplits[0].replace("openEuler-", "");
        String osType = nameSplits[1];
  
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
        String baseUrlS = baseUrl.toString();

        Map<String, String> res = Map.ofEntries(
            Map.entry("osName", "openEuler"),
            Map.entry("osVer", osVer),
            Map.entry("osType", osType),
            Map.entry("baseUrl", baseUrlS)
        );

        return res;
    }
}
