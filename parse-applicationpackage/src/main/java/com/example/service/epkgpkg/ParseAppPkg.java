package com.example.service.epkgpkg;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
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

import org.apache.commons.lang3.StringUtils;
import org.commonmark.Extension;
import org.commonmark.ext.gfm.tables.TablesExtension;
import org.commonmark.node.Node;
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

import com.example.entity.po.AppPkg;
import com.example.util.Base64Util;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.power.common.util.StringUtil;
import org.yaml.snakeyaml.Yaml;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class ParseAppPkg {
    // @Value("${imageinfo.filepath}")
    // private String originPath;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    ExecuteService executeService;

    

    private static Map<String, String> categoryChEn = Map.ofEntries(
        Map.entry("database", "数据库"),
        Map.entry("bigdata", "大数据"),
        Map.entry("ai", "AI"),
        Map.entry("storage", "分布式存储"),
        Map.entry("cloud", "云服务"),
        Map.entry("hpc", "HPC"),
        Map.entry("other", "其他")
    );


    public void run(String imagePath) {
        // 解析yaml
        Yaml yaml = new Yaml();

        InputStream inputStream = null;
        try {
            inputStream = new FileInputStream(imagePath);
        } catch (FileNotFoundException e) {
            log.error("", e);
        }
        Map<String, Object> map = yaml.load(inputStream);
        String envi = (String) map.get("environment");

        
        AppPkg pkg = null;
        try {
            String json = objectMapper.writeValueAsString(map);
            pkg = objectMapper.readValue(json, AppPkg.class);
        } catch (Exception e) {
        }

        String cate = StringUtils.trimToEmpty((String) map.get("category"));
        if (0 == cate.length()) {
            pkg.setAppCategory("其他");
        } else if (categoryChEn.keySet().contains(cate)) {
            pkg.setAppCategory(categoryChEn.get(cate));
        } else {
        }

        pkg.setInstallation((String) map.get("install"));

        List<String> simi = (List<String>) map.get("similar_packages");
        try {
			pkg.setSimilarPkgs(objectMapper.writeValueAsString(simi));
		} catch (JsonProcessingException e) {
            log.error("", e);
		}
        List<String> depe = (List<String>) map.get("dependency");
        try {
			pkg.setDependencyPkgs(objectMapper.writeValueAsString(depe));
		} catch (JsonProcessingException e) {
            log.error("", e);
		}

        
        

        executeService.insertRPMPackage(pkg);

        // try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("output.txt"), "UTF-8"))) {
        //     writer.write(envi);
        //     System.out.println("Content has been written to the file successfully with UTF-8 encoding.");
        // } catch (IOException e) {
        //     System.err.println("An error occurred while writing to the file: " + e.getMessage());
        // }
        
        // 验证markdown字符串是否能够被正确解析
        // List<Extension> extensions = Arrays.asList(TablesExtension.create());
        // org.commonmark.parser.Parser parser = org.commonmark.parser.Parser.builder().extensions(extensions).build();
        // Node document = parser.parse(envi);
        // org.commonmark.renderer.html.HtmlRenderer renderer = org.commonmark.renderer.html.HtmlRenderer.builder().extensions(extensions).build();
        // System.out.println(renderer.render(document));
        // System.out.println();
    }
}
