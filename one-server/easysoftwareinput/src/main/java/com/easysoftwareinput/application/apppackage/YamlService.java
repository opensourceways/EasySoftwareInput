package com.easysoftwareinput.application.apppackage;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.yaml.snakeyaml.Yaml;

import com.easysoftwareinput.common.constant.MapConstant;
import com.easysoftwareinput.common.entity.MessageCode;
import com.easysoftwareinput.common.utils.HttpClientUtil;
import com.easysoftwareinput.domain.apppackage.model.AppPackage;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class YamlService {
    private static final Logger logger = LoggerFactory.getLogger(YamlService.class);

    @Value("${app.post.url}")
    String postUrl;

    @Autowired
    ObjectMapper objectMapper;

    public String run(String imagePath) {
        Yaml yaml = new Yaml();

        InputStream inputStream = null;
        try {
            inputStream = new FileInputStream(imagePath);
        } catch (FileNotFoundException e) {
            logger.error(MessageCode.EC0009.getMsgEn(), imagePath);
        }
        Map<String, Object> map = yaml.load(inputStream);

        AppPackage pkg = null;
        try {
            String json = objectMapper.writeValueAsString(map);
            pkg = objectMapper.readValue(json, AppPackage.class);
        } catch (Exception e) {
            logger.error(MessageCode.EC00014.getMsgEn(), e);
        }

        // if (! "nginx".equals(pkg.getName())) {
        //     return "";
        // }

        String cate = StringUtils.trimToEmpty((String) map.get("category"));
        if (0 == cate.length()) {
            pkg.setCategory(MapConstant.APP_CATEGORY_MAP.get("Other"));
        } else if (MapConstant.APP_CATEGORY_MAP.keySet().contains(cate)) {
            pkg.setCategory(MapConstant.APP_CATEGORY_MAP.get(cate));
        } else {
        }

        pkg.setInstallation((String) map.get("install"));

        List<String> simi = (List<String>) map.get("similar_packages");
        try {
			pkg.setSimilarPkgs(objectMapper.writeValueAsString(simi));
		} catch (JsonProcessingException e) {
            logger.error(MessageCode.EC00014.getMsgEn(), e);
		}
        List<String> depe = (List<String>) map.get("dependency");
        try {
			pkg.setDependencyPkgs(objectMapper.writeValueAsString(depe));
		} catch (JsonProcessingException e) {
            logger.error(MessageCode.EC00014.getMsgEn(), e);
		}

        pkg.setType("IMAGE");

        pkg.setAppVer("");

        String body = "";
        try {
			body = objectMapper.writeValueAsString(pkg);
        } catch (JsonProcessingException e) {
            logger.error(MessageCode.EC00014.getMsgEn(), e);
        }

        HttpClientUtil.postApp(postUrl, body);

        return pkg.getName();
    }
}
