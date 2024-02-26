package com.example.service;

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

import com.example.entity.po.RPMPackage;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.power.common.util.StringUtil;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class ParseXml {
    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    ExecuteService executeService;

    @Value("${baseXmlPath}")
    private String baseXmlPath;

    private Map<String, Integer> countMap = new HashMap<>();

    public Map<String ,String> parseFormat(Element format) {
        Map<String ,String> res = new HashMap<>();
        Stream.of ("license", "vendor", "group", "buildhost", "sourcerpm").forEach(item -> {
            Element e = format.element(new QName(item, new Namespace("", "http://linux.duke.edu/metadata/rpm")));
            if (e != null) {
                res.put("rpm_" + item, e.getTextTrim());               
            }
        });

        Stream.of("header-range").forEach(name -> {
            Element e = format.element(new QName(name, new Namespace("", "http://linux.duke.edu/metadata/rpm")));
            if (e != null) {
                List<Attribute> listA = e.attributes();
                listA.stream().forEach(attribute -> {
                    if ("header-range".equals(name)) {
                        res.put("header_" + attribute.getName(), attribute.getValue());
                    } else {
                        res.put(e.getName() + "_" + attribute.getName(), attribute.getValue());
                    }
                    
                });
            }
        });
        return res;
    }

    public List<Map<String, String>> parseArray(Element root, String aName) {
        List<Map<String, String>> res = new ArrayList<>();
        Element element = root.element(new QName(aName, new Namespace("", "http://linux.duke.edu/metadata/rpm")));
        if (element == null) {
            return res;
        }
        Map<String, String> map = new HashMap<>();
        for (Element e : element.elements()) {
            map.put("name", e.attributeValue("name"));
            map.put("flags", e.attributeValue("flags"));
            map.put("epoch", e.attributeValue("epoch"));
            map.put("ver", e.attributeValue("ver"));
            map.put("rel", e.attributeValue("rel"));
            res.add(new HashMap<>(map));
        }
        return res;
        
    }


    @Async
    public RPMPackage assembleInputObject(Map<String ,String> underLineMap) {
        Map<String, String> camelMap = new HashMap<>();
        for (String underLineKey: underLineMap.keySet()) {
            String camelKey = StringUtil.underlineToCamel(underLineKey);
            camelMap.put(camelKey, underLineMap.get(underLineKey));
        }

        RPMPackage pAll = null;
        try {
            String json = objectMapper.writeValueAsString(camelMap);
            
            pAll = objectMapper.readValue(json, RPMPackage.class);
        } catch (Exception e) {
        }
        

        Timestamp currentTime = new Timestamp(System.currentTimeMillis());
        String id = UUID.randomUUID().toString().replace("-", "");

        return pAll;
    }

    public List<Map<String, String>> parseFiles(Element format) {
        List<Map<String, String>> res = new ArrayList<>();
        List<Element> listElement = format.elements(new QName("file", new Namespace("", "http://linux.duke.edu/metadata/common")));
        if (listElement == null || listElement.size() == 0) {
            return res;
        }
        Map<String, String> map = new HashMap<>();
        for (Element e : listElement) {
            map.put("fileType", e.attributeValue("type"));
            map.put("fileName", e.getTextTrim());
            res.add(new HashMap<>(map));
        }
        return res;
    }

    // 解析每个pkg
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

        Element format = pkg.element(new QName("format", new Namespace("", "http://linux.duke.edu/metadata/common")));
        Map<String ,String> forRes = parseFormat(format);
        res.putAll(forRes);

        List<Map<String, String>> provides = parseArray(format, "provides");
        List<Map<String, String>> requires = parseArray(format, "requires");

        List<Map<String, String>> files = parseFiles(format);

        try {
            String provideString = objectMapper.writeValueAsString(provides);
            String requireString = objectMapper.writeValueAsString(requires);
            String fileString = objectMapper.writeValueAsString(files);

            res.put("requires", requireString);
            res.put("provides", provideString);
            res.put("files", fileString);
        } catch (JsonProcessingException e) {
        } catch (IOException e) {
        }
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

                    // 只存10万条数据，仅供演示
                    // if (count > 10_0000) {
                    //     System.exit(0);
                    // }

                    Map<String, String> mes = parsePkg(pkgElement, osMes);
                    RPMPackage pkg = assembleInputObject(mes);
                    executeService.insertRPMPackage(pkg);

                }
            } catch (DocumentException e) {
            }
        }

    }
}
