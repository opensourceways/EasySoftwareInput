package com.example.service.epkgpkg;

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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.power.common.util.StringUtil;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ParseEpkgSrcPkg {
    @Value("${primaryxml.filepath}")
    private String originPath;

    @Value("${primaryxml.srcfilepath}")
    private String srcfilepath;

    public List<String> getSrcFile() {
        List<String> res = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(srcfilepath))) {
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
  
        String baseUrl = "https://repo.openeuler.org/" + nameSplits[0] + "/" + nameSplits[1] + "/" + nameSplits[2];
        Map<String, String> res = Map.ofEntries(
            Map.entry("osName", "openEuler"),
            Map.entry("osVer", osVer),
            Map.entry("osType", osType),
            Map.entry("baseUrl", baseUrl)
        );
        return res;
    }

    public void writeToFile(String srcUrl) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(srcfilepath, true))) {
            writer.write(srcUrl + "\n");
        } catch (IOException e) {
            System.err.println("An error occurred: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void run() {
        SAXReader reader = new SAXReader();
        Document document = null;
        try {
            document = reader.read(originPath);
        } catch (DocumentException e) {
            e.printStackTrace();
        }
        int count = 0;
        int srcCount = 0;

        Map osMes = Map.ofEntries(
            Map.entry("osName", "openEuler"),
            Map.entry("osVer", "22.03-LTS-SP1"),
            Map.entry("osType", "default"),
            Map.entry("baseUrl", "https://eulermaker.compass-ci.openeuler.openatom.cn/api/ems1/repositories/epkg-test/")
        );

        for (Element pkgElement : document.getRootElement().elements()) {
            String pkgName = pkgElement.element(new QName("name", new Namespace("", "http://linux.duke.edu/metadata/common"))).getTextTrim();

            log.info("count: {}, pkgName: {}", count, pkgName);
            count ++;

            Map<String, String> mes = parsePkg(pkgElement, osMes);
            System.out.println(mes.get("arch"));

            if ("src".equals(mes.get("arch"))) {
                String url = mes.get("base_url") + mes.get("location_href");
                writeToFile(url);
            }
        }
    }
}
