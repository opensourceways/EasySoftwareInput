package com.example.service;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.Namespace;
import org.dom4j.QName;
import org.dom4j.io.SAXReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import com.example.entity.po.RPMPackage;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.power.common.util.StringUtil;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ParseSrcPkg {
    @Value("${baseXmlPath}")
    private String baseXmlPath;

    @Value("${srcFileName}")
    private String srcFileName;

    public List<String> getSrcFile() {
        List<String> res = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(srcFileName))) {
            String line;
            while ((line = reader.readLine()) != null) {
                res.add(line);
            }
        } catch (IOException e) {
        }
        return res;
    }

    public Map<String ,String> parsePkg(Element pkg, Map<String, String> osMes) {
        Map<String ,String> res = new HashMap<>();
        res.put("os_name", osMes.get("osName"));
        res.put("os_ver", osMes.get("osVer"));
        res.put("os_type", osMes.get("osType"));
        res.put("base_url", osMes.get("baseUrl"));

        Stream.of("name", "arch", "summary", "description", "packager", "url", "checksum").forEach(item -> {
            res.put(item, pkg.element(new QName(item, new Namespace("", "http://linux.duke.edu/metadata/common"))).getTextTrim());
        });

        Stream.of("version", "time", "size", "location", "checksum").forEach(item -> {
            Element e = pkg.element(new QName(item, new Namespace("", "http://linux.duke.edu/metadata/common")));
            List<Attribute> listA = e.attributes();
            listA.stream().forEach(a -> res.put(item + "_" + a.getName(), a.getValue()));
        });
        return res;
    }

    // 解析primary.xml文件名
    public Map<String, String> parseFilename(String filePath) {
        String[] pathSplits = filePath.split("\\\\");
        String filename = pathSplits[pathSplits.length - 1];
        String[] nameSplits = filename.split("_a_");

        String osVer = nameSplits[0].replace("openEuler-", "");
        String osType = nameSplits[1];
        
        StringBuilder baseUrl = new StringBuilder();
        if ("openEuler-20.09".equals(nameSplits[0]) || "openEuler-21.03".equals(nameSplits[0]) || 
                "openEuler-21.09".equals(nameSplits[0]) || "openEuler-22.09".equals(nameSplits[0])) {
            baseUrl.append("https://archives.openeuler.openatom.cn");
        } else {
            baseUrl.append("https://repo.openeuler.org");
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

    public void writeToFile(String srcUrl) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(srcFileName, true))) {
            writer.write(srcUrl + "\n");
        } catch (IOException e) {
            System.err.println("An error occurred: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void run() {
        SAXReader reader = new SAXReader();

        File base = new File(baseXmlPath);
        File[] xmlFiles = base.listFiles();
        Document document = null;
        int count = 0;
        // 解析每个xml文件
        for (File file : xmlFiles) {
            try {
                String filePath = file.getAbsolutePath();
                
                Map<String, String> osMes = parseFilename(filePath);

                document = reader.read(filePath);
                
                // 每个xml文件包含多个软件包
                for (Element pkgElement : document.getRootElement().elements()) {
                    String pkgName = pkgElement.element(new QName("name", new Namespace("", "http://linux.duke.edu/metadata/common"))).getTextTrim();

                    log.info("count: {}fileName: {}, pkgName: {}", count, filePath, pkgName);
                    count ++;
                    Map<String, String> mes = parsePkg(pkgElement, osMes);
                    System.out.println(mes);
                    if ("src".equals(mes.get("arch"))) {
                        String srcUrl = mes.get("base_url") + "/" + mes.get("location_href");
                        writeToFile(srcUrl);
                    }
                }

            } catch (Exception e) {
                
            }
        }
    }
}
