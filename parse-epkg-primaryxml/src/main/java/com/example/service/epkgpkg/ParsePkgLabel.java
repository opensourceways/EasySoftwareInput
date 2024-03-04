package com.example.service.epkgpkg;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.dom4j.Attribute;
import org.dom4j.Element;
import org.dom4j.Namespace;
import org.dom4j.QName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class ParsePkgLabel {
    @Autowired
    ObjectMapper objectMapper;

    // 解析<file>标签
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

    // 解析<package>标签
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

    // 解析<rpm:format>标签
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

    // 解析<rpm:entity>标签
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
}
