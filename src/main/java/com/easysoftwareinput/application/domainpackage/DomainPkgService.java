package com.easysoftwareinput.application.domainpackage;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.repository.support.DomainClassConverter;
import org.springframework.stereotype.Service;

import com.easysoftwareinput.common.utils.ObjectMapperUtil;
import com.easysoftwareinput.domain.domainpackage.ability.DomainPackageConverter;
import com.easysoftwareinput.domain.domainpackage.model.DomainPackage;
import com.easysoftwareinput.infrastructure.domainpackage.DomainPkgGatewayImpl;
import com.easysoftwareinput.infrastructure.mapper.DomainPkgMapper;
import com.fasterxml.jackson.core.exc.StreamReadException;
import com.fasterxml.jackson.databind.DatabindException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class DomainPkgService {
    @Value("${domain.file}")
    private String path;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    DomainPackageConverter converter;

    @Autowired
    DomainPkgGatewayImpl gateway;

    public void run() {
        Map<String, Object> map = getMapFromFile(path);
        if (map.size() == 0) {
            return;
        }

        List<Map<String, Object>> list = getChildren(map);
        if (list.size() == 0) {
            return;
        }

        List<DomainPackage> pkgList = converter.toPkg(list);
        
        gateway.saveAll(pkgList);

        log.info("finish-write-domain-pkg");
    }

    private List<Map<String, Object>> getChildren(Map<String, Object> map) {
        List<Map<String, Object>> res = new ArrayList<>();
        try {
            Map<String, Object> data = (Map<String, Object>) map.get("data");
            List<Map<String, Object>> list = (List<Map<String, Object>>) data.get("list");
            for (Map<String, Object> mapL : list) {
                List<Map<String, Object>> children = (List<Map<String, Object>>) mapL.get("children");
                res.addAll(children);
            }
        } catch (Exception e) {
            log.error("can not get children", e);
        }
        return res;
    }

    private Map<String, Object> getMapFromFile(String path) {
        File file = new File(path);
        Map<String, Object> map = new HashMap<>();
        try {
            map = objectMapper.readValue(file, Map.class);
        } catch (IOException e) {
            log.error("can not parse json file", e);
        }
        return map;
    }
}
