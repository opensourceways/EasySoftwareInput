/* Copyright (c) 2024 openEuler Community
 EasySoftwareInput is licensed under the Mulan PSL v2.
 You can use this software according to the terms and conditions of the Mulan PSL v2.
 You may obtain a copy of Mulan PSL v2 at:
     http://license.coscl.org.cn/MulanPSL2
 THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
 EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
 MERCHANTABILITY OR FIT FOR A PARTICULAR PURPOSE.
 See the Mulan PSL v2 for more details.
*/

package com.easysoftwareinput.application.rpmpackage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import org.dom4j.Attribute;
import org.dom4j.Element;
import org.springframework.stereotype.Service;
import com.easysoftwareinput.common.utils.ObjectMapperUtil;

@Service
public class PkgService {

    /**
     * init the pkg.
     * @param osMes os.
     * @return map of os.
     */
    private Map<String, String> initMap(Map<String, String> osMes) {
        Map<String, String> res = new HashMap<>();
        res.put("os_name", osMes.get("osName"));
        res.put("os_ver", osMes.get("osVer"));
        res.put("os_type", osMes.get("osType"));
        res.put("base_url", osMes.get("baseUrl"));
        return res;
    }

    /**
     * parse text of pkg.
     * @param pkg pkg.
     * @param res map.
     */
    private void setText(Element pkg, Map<String, String> res) {
        Stream.of("name", "arch", "summary", "description", "packager", "url", "checksum").forEach(item -> {
            res.put(item, pkg.element(item).getTextTrim());
        });
    }

    /**
     * parse attribute of pkg.
     * @param pkg pkg.
     * @param res map.
     */
    private void setAttribute(Element pkg, Map<String, String> res) {
        Stream.of("version", "time", "size", "location", "checksum").forEach(item -> {
            Element e = pkg.element(item);
            List<Attribute> listA = e.attributes();
            listA.stream().forEach(a -> res.put(item + "_" + a.getName(), a.getValue()));
        });
    }

    /**
     * parse format tag of pkg.
     * @param format format.
     * @param res map.
     */
    private void setFormat(Element format, Map<String, String> res) {
        Map<String, String> forRes = parseFormat(format);
        res.putAll(forRes);
    }

    /**
     * parse the array of format tag.
     * @param format format.
     * @param res map.
     */
    private void setFormatArray(Element format, Map<String, String> res) {
        List<Map<String, String>> provides = parseArray(format, "provides");
        List<Map<String, String>> requires = parseArray(format, "requires");
        List<Map<String, String>> conflicts = parseArray(format, "conflicts");
        List<Map<String, String>> files = parseFiles(format);

        res.put("requires", ObjectMapperUtil.writeValueAsString(provides));
        res.put("provides", ObjectMapperUtil.writeValueAsString(requires));
        res.put("conflicts", ObjectMapperUtil.writeValueAsString(conflicts));
        res.put("files", ObjectMapperUtil.writeValueAsString(files));
    }

    /**
     * parse src pkgs.
     * @param pkg pkg.
     * @param osMes os.
     * @return map of src pkg name and url.
     */
    public Map<String, String> parseSrc(Element pkg, Map<String, String> osMes) {
        Map<String, String> res = initMap(osMes);
        Element format = pkg.element("format");
        Element s = format.element("sourcerpm");
        res.put("rpm_sourcerpm", s.getTextTrim());
        setAttribute(pkg, res);
        setText(pkg, res);
        return res;
    }

    /**
     * parse each pkg.
     * @param pkg pkg.
     * @param osMes os.
     * @return map.
     */
    public Map<String, String> parsePkg(Element pkg, Map<String, String> osMes) {
        Map<String, String> res = initMap(osMes);
        setText(pkg, res);
        setAttribute(pkg, res);

        Element format = pkg.element("format");
        setFormat(format, res);
        setFormatArray(format, res);

        return res;
    }

    /**
     * parse format tag.
     * @param format format.
     * @return map.
     */
    private Map<String, String> parseFormat(Element format) {
        Map<String, String> res = new HashMap<>();
        Stream.of("license", "vendor", "group", "buildhost", "sourcerpm").forEach(item -> {
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

    /**
     * parse attribute array of each name.
     * @param root root.
     * @param aName name.
     * @return list of atributes.
     */
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

    /**
     * parse <file> tag.
     * @param format format.
     * @return list of files.
     */
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
