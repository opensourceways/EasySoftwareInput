package com.example.service.epkgpkg;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
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

import com.example.entity.po.EPKGPackage;
import com.example.mapper.EPKGPackageMapper;
import com.example.service.Assemble;
import com.example.service.ExecuteService;
import com.example.util.Base64Util;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.power.common.util.StringUtil;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class ParseEpkg {
    @Value("${primaryxml.filepath}")
    private String originPath;

    @Value("${primaryxml.srcfilepath}")
    private String srcfilepath;

    @Value("${epkg.post.url}")
    private String postUrl;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    Assemble assemble;

    @Autowired
    ParseEpkgSrcPkg parseSrcPkg;

    @Autowired
    ParsePkgLabel parsePkgLabel;

    @Autowired
    ExecuteService executeService;

    @Autowired
    EPKGPackageMapper epkgPackageMapper;

    @Async
    public EPKGPackage assembleInputObject(Map<String ,String> underLineMap) {
        Map<String, String> camelMap = new HashMap<>();
        for (String underLineKey: underLineMap.keySet()) {
            String camelKey = StringUtil.underlineToCamel(underLineKey);
            camelMap.put(camelKey, underLineMap.get(underLineKey));
        }

        EPKGPackage pAll = null;
        try {
            String json = objectMapper.writeValueAsString(camelMap);
            
            pAll = objectMapper.readValue(json, EPKGPackage.class);
        } catch (Exception e) {
        }
        

        Timestamp currentTime = new Timestamp(System.currentTimeMillis());
        String id = UUID.randomUUID().toString().replace("-", "");

        return pAll;
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


    public void run() throws UnsupportedEncodingException {

        SAXReader reader = new SAXReader();
        Document document = null;
        try {
            document = reader.read(originPath);
        } catch (DocumentException e) {
            e.printStackTrace();
        }
        int count = 0;

        Map osMes = Map.ofEntries(
            Map.entry("osName", "openEuler"),
            Map.entry("osVer", "22.03-LTS-SP1"),
            Map.entry("osType", "default"),
            Map.entry("baseUrl", "https://eulermaker.compass-ci.openeuler.openatom.cn/api/ems1/repositories/epkg-test/")
        );

        // srcFiles存储：源码包url
        List<String> srcFiles = parseSrcPkg.getSrcFile();

        for (Element pkgElement : document.getRootElement().elements()) {
            String pkgName = pkgElement.element(new QName("name", new Namespace("", "http://linux.duke.edu/metadata/common"))).getTextTrim();

            log.info("count: {}, pkgName: {}", count, pkgName);
            count ++;

            // Map<String, String> mes = parsePkg(pkgElement, osMes);
            Map<String, String> mes = parsePkgLabel.parsePkg(pkgElement, osMes);

            EPKGPackage pkg = assemble.assembleEpkgpkg(mes, srcFiles);
            
            // EPKGPackage pkgBase64 = null;
            // try {
            //     pkgBase64 = Base64Util.encode(pkg);
            // } catch (Exception e) {
            //     // TODO Auto-generated catch block
            //     e.printStackTrace();
            // }
            
            // epkgPackageMapper.insert(pkgBase64);

            // 调用easysoftwareservice服务的接口
            executeService.insertEPKGPackage(pkg, postUrl);
        }
    }
}
