package com.easysoftwareinput.application.rpmpackage;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.dom4j.Attribute;
import org.dom4j.Element;
import org.dom4j.Namespace;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.easysoftwareinput.common.entity.MessageCode;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class PkgService {
    @Autowired
    ObjectMapper objectMapper;

    public Map<String ,String> parsePkg(Element pkg, Map<String, String> osMes) {
        Map<String ,String> res = new HashMap<>();
        res.put("os_name", osMes.get("osName"));
        res.put("os_ver", osMes.get("osVer"));
        res.put("os_type", osMes.get("osType"));
        res.put("base_url", osMes.get("baseUrl"));


        Stream.of("name", "arch", "summary", "description", "packager", "url", "checksum").forEach(item -> {
            res.put(item, pkg.element(item).getTextTrim());
        });

        Stream.of("version", "time", "size", "location", "checksum").forEach(item -> {
            Element e = pkg.element(item);
            List<Attribute> listA = e.attributes();
            listA.stream().forEach(a -> res.put(item + "_" + a.getName(), a.getValue()));
        });

        Element format = pkg.element("format");
        Map<String ,String> forRes = parseFormat(format);
        res.putAll(forRes);

        List<Map<String, String>> provides = parseArray(format, "provides");
        List<Map<String, String>> requires = parseArray(format, "requires");
        List<Map<String, String>> conflicts = parseArray(format, "conflicts");
        List<Map<String, String>> files = parseFiles(format);

        String provideString = "";
        String requireString = "";
        String conflictString = "";
        String fileString = "";
        try {
            provideString = objectMapper.writeValueAsString(provides);
            requireString = objectMapper.writeValueAsString(requires);
            conflictString = objectMapper.writeValueAsString(conflicts);
            fileString = objectMapper.writeValueAsString(files);
        } catch (Exception e) {
            log.error(MessageCode.EC00014.getMsgEn(), e);
        }

        res.put("requires", requireString);
        res.put("provides", provideString);
        res.put("conflicts", conflictString);
        res.put("files", fileString);

        return res;
    }

    private Map<String ,String> parseFormat(Element format) {
        Map<String ,String> res = new HashMap<>();
        Stream.of ("license", "vendor", "group", "buildhost", "sourcerpm").forEach(item -> {
            Element e = format.element(item);
            if (e != null) {
                res.put("rpm_" + item, e.getTextTrim());               
            }
        });

        Stream.of("header-range").forEach(name -> {
            Element e = format.element(name);
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

    private List<Map<String, String>> parseArray(Element root, String aName) {
        List<Map<String, String>> res = new ArrayList<>();
        Element element = root.element(aName);
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

    private List<Map<String, String>> parseFiles(Element format) {
        List<Map<String, String>> res = new ArrayList<>();
        List<Element> listElement = format.elements("file");
        if (listElement == null || listElement.size() == 0) {
            return res;
        }
        for (Element e : listElement) {
            Map<String, String> map = new HashMap<>();
            map.put("fileType", e.attributeValue("type"));
            map.put("fileName", e.getTextTrim());
            res.add(map);
        }
        return res;
    }
}
