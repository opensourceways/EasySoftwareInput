package com.easysoftwareinput.application.epkgpackage;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.dom4j.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.easysoftwareinput.application.rpmpackage.HttpService;
import com.easysoftwareinput.application.rpmpackage.PkgService;
import com.easysoftwareinput.domain.epkgpackage.ability.EPKGPackageConverter;
import com.easysoftwareinput.domain.epkgpackage.model.EPKGPackage;
import com.easysoftwareinput.infrastructure.epkgpkg.EpkgGatewayImpl;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class EPKGAsyncService {
    private static final Logger logger = LoggerFactory.getLogger(EPKGAsyncService.class);

    @Autowired
    PkgService pkgService;

    @Autowired
    EpkgGatewayImpl gateway;


    @Autowired
    EPKGPackageConverter epkgPackageConverter;

  

    @Async("epkgasyncServiceExecutor")
    public void executeAsync(List<Element> eList, Map<String, String> osMes, int i, String postUrl, Map<String, String> srcMap) {
        log.info("start: {}", Thread.currentThread().getName());
        long s = System.currentTimeMillis();
        List<EPKGPackage> pkgList = new ArrayList<>(eList.size());
        Set<String> pkgIds = new HashSet<>();
        
        try {
            for (Element e : eList) {
                Map<String, String> res = pkgService.parsePkg(e, osMes);
    
                EPKGPackage ePkg = epkgPackageConverter.toEntity(res, srcMap);
        
                // 不添加源码包
                if ("src".equals(ePkg.getArch())) {
                    continue;
                }
    
                // 舍弃主键重复的数据
                if (pkgIds.add(ePkg.getPkgId())) {
                    pkgList.add(ePkg);
                }
            }
        } catch (Exception e) {
            log.error("e: {}", e);
        }

        log.info("start cthread: {}, start i: {}, name: {}", Thread.currentThread().getName(), i, pkgList.get(0).getName());

        long s1 = System.currentTimeMillis();
        log.info("finish-xml-parse, thread name: {}, list.size(): {}, time used: {}ms", Thread.currentThread().getName(), pkgList.size(), (s1 - s));

        gateway.saveAll(pkgList);
        log.info("finish-mysql, thread name: {}, list.size(): {}, time used: {}ms", Thread.currentThread().getName(), pkgList.size(), (s1 - s));

    }
}
