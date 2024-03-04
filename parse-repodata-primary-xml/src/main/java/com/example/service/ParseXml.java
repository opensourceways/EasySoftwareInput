package com.example.service;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
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
import com.example.util.Base64Util;
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

    @Autowired
    ParseSrcPkg parseSrcPkg;

    @Value("${baseXmlPath}")
    private String baseXmlPath;

    @Value("${srcFileName}")
    private String srcFileName;

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
    public RPMPackage assembleInputObject(Map<String ,String> underLineMap, List<String> srcFiles) {
        Map<String, String> camelMap = new HashMap<>();
        for (String underLineKey: underLineMap.keySet()) {
            String camelKey = StringUtil.underlineToCamel(underLineKey);
            camelMap.put(camelKey, underLineMap.get(underLineKey));
        }

        RPMPackage pkg = null;
        try {
            String json = objectMapper.writeValueAsString(camelMap);
            
            pkg = objectMapper.readValue(json, RPMPackage.class);
        } catch (Exception e) {
        }
        
        pkg.setBinDownloadUrl(camelMap.get("baseUrl") + camelMap.get("locationHref"));
        pkg.setChangeLog("");

        pkg.setMaintanierId("");
        pkg.setMaintianerEmail("");
        pkg.setMaintainerGiteeId("");
        pkg.setMaintainerUpdateAt("");
        pkg.setMaintainerStatus("");

        pkg.setOs(camelMap.get("osName") + "-" + camelMap.get("osVer"));
        pkg.setOsSupport("");
        pkg.setRepo("openEuler官方仓库");
        pkg.setRepoType(camelMap.get("osType"));
        pkg.setRpmCategory("Unspecified");

        Long cTime = Long.parseLong(camelMap.get("timeFile"));
        String fTime = new SimpleDateFormat("yyyy-MM-dd").format(new Date(cTime * 1000));
        pkg.setRpmUpdateAt(fTime);
        
        double sSize = (double) Integer.parseInt(camelMap.get("sizePackage")) / 1024 / 1024;
        String fSize = String.format("%.2fMB", sSize);
        pkg.setRpmSize(fSize);

        pkg.setSecurity("");
        pkg.setSimilarPkgs("");
     
        String desired = "";
        for (String srcUrl : srcFiles) {
            String[] splits = srcUrl.split("/");
            String name = splits[splits.length - 1];
            if (name.equals(camelMap.get("rpmSourcerpm"))) {
                desired = srcUrl;
                break;
            }
        }
        pkg.setSrcDownloadUrl(desired);

        pkg.setSrcRepo(camelMap.get("url"));

        String formatS = String.format("1. 添加源\n`dnf config-manager --add-repo %s `\n2. 更新源索引\n" +
                "`dnf clean all && dnf makecache`\n3. 安装 %s 软件包\n`dnf install %s`", camelMap.get("baseUrl"),
                pkg.getName(), pkg.getName());
        
        pkg.setInstallation(formatS);
        pkg.setUpStream("");
        pkg.setVersion(camelMap.get("versionVer") + "-" + camelMap.get("versionRel"));

        return pkg;
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
        List<Map<String, String>> conflicts = parseArray(format, "conflicts");
        List<Map<String, String>> files = parseFiles(format);

        try {
            String provideString = objectMapper.writeValueAsString(provides);
            String requireString = objectMapper.writeValueAsString(requires);
            String conflictString = objectMapper.writeValueAsString(conflicts);
            String fileString = objectMapper.writeValueAsString(files);

            res.put("requires", requireString);
            res.put("provides", provideString);
            res.put("conflicts", conflictString);
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

                    log.info("current file: count: {},fileName: {}, pkgName: {}", count, filePath, pkgName);
                    count ++;

                    Map<String, String> mes = parsePkg(pkgElement, osMes);
                    List<String> srcFiles = parseSrcPkg.getSrcFile();
                    RPMPackage pkg = assembleInputObject(mes, srcFiles);
                    
                    pkg = Base64Util.encode(pkg);

                    executeService.insertRPMPackage(pkg);

                }
            } catch (DocumentException e) {
            }
        }

    }
}
