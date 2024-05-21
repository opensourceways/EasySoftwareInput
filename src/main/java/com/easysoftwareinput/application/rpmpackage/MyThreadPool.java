package com.easysoftwareinput.application.rpmpackage;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.session.SqlSessionFactory;
import org.dom4j.Document;
import org.dom4j.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.easysoftwareinput.common.utils.ObjectMapperUtil;
import com.easysoftwareinput.domain.rpmpackage.ability.RPMPackageConverter;
import com.easysoftwareinput.domain.rpmpackage.model.RPMPackage;
import com.easysoftwareinput.domain.rpmpackage.model.RPMPackageDO;
import com.easysoftwareinput.infrastructure.BasePackageDO;
import com.easysoftwareinput.infrastructure.mapper.RPMPackageDOMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class MyThreadPool extends ServiceImpl<RPMPackageDOMapper, RPMPackageDO> {
    @Autowired
    PkgService pkgService;

    @Autowired
    RPMPackageConverter rpmPackageConverter;

    @Autowired
    HttpService httpService;

    @Autowired
    Environment env;

    // ThreadLocal<MyServiceImpl> batchLocal = ThreadLocal.withInitial(() -> {return new MyServiceImpl();});


    @Async("asyncServiceExecutor")
    public void parseXml(Document xml, Map<String, String> osMes, int count, Map<String, String> srcUrls,
            Map<String, BasePackageDO> maintainers) {
        List<Element> pkgs = xml.getRootElement().elements();

        long s = System.currentTimeMillis();
        List<RPMPackage> pkgList = new ArrayList<>();
        Set<String> pkgIds = new HashSet<>();
        for (int i = 0; i < pkgs.size(); i++) {
            Element ePkg = pkgs.get(i);
            Map<String, String> res = pkgService.parsePkg(ePkg, osMes);
            RPMPackage pkg = rpmPackageConverter.toEntity(res, srcUrls, maintainers);

            // 如果架构是src，则不写入
            if ("src".equals(pkg.getArch())) {
                continue;
            }
            // 舍弃主键重复的数据
            if (pkgIds.add(pkg.getPkgId())) {
                pkgList.add(pkg);
            }
        }

        log.info("finish-xml-parse, thread name: {}, list.size(): {}, time used: {}ms, fileIndex: {}", Thread.currentThread()
                .getName(), pkgList.size(), (System.currentTimeMillis() - s), count);

        // List<RPMPackageDO> doList = rpmPackageConverter.toDO(pkgList);
        // saveBatch(doList);

        List<String> bodyList = filterPkg(pkgList);
        post(bodyList, env.getProperty("rpm.post-url"));
    }

    private void post(List<String> bodyList, String postUrl) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        RestTemplate rest = new RestTemplate();
        long s = System.currentTimeMillis();
        // int count = 0;
        for (String body : bodyList) {
            // count ++;
            HttpEntity<String> request = new HttpEntity<>(body, headers);
            try {
            String res = rest.postForObject(postUrl, request, String.class);
            } catch (Exception e) {
                log.info("fail-to-mysql,res: {}, body: {}", e.getMessage(), body);
            }
        }
        log.info("post time(ms): {}", System.currentTimeMillis() - s);
    }


    private List<String> filterPkg(List<RPMPackage> pkgList) {
        ObjectMapper objectMapper = new ObjectMapper();
        List<String> bodyList = new ArrayList<>();
        for (RPMPackage pkg : pkgList) {
            
            // fitlerPkg(pkg, forBiddenedFields);
            String body = "";
            try {
                body = objectMapper.writeValueAsString(pkg);
                
            } catch (JsonProcessingException e) {
                log.info("can not transfer: {}", e);
            }
            bodyList.add(body);
        }
        return bodyList;
    }

 
    public synchronized void saveBatch(List<RPMPackageDO> list) {
        log.info("start-save-batch; thread: {}", Thread.currentThread().getName());
        long s = System.currentTimeMillis();

        saveOrUpdateBatch(list);

        log.info("finish-mysql-batch-save; thread name: {}, list.size(): {}, time used: {}ms", Thread.currentThread().getName(), list.size(), (System.currentTimeMillis() - s));

    }
    
}

// class MyServiceImpl extends ServiceImpl<RPMPackageDOMapper, RPMPackageDO> {
//     @Override
//     public SqlSessionFactory getSqlSessionFactory() {
//         return super.getSqlSessionFactory();
//     }
// }