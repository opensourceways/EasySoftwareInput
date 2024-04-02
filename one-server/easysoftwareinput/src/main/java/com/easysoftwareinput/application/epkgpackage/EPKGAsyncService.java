package com.easysoftwareinput.application.epkgpackage;

import java.util.Map;

import org.dom4j.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.easysoftwareinput.application.rpmpackage.HttpService;
import com.easysoftwareinput.application.rpmpackage.PkgService;
import com.easysoftwareinput.common.entity.MessageCode;
import com.easysoftwareinput.domain.epkgpackage.ability.EPKGPackageConverter;
import com.easysoftwareinput.domain.epkgpackage.model.EPKGPackage;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class EPKGAsyncService {
    private static final Logger logger = LoggerFactory.getLogger(EPKGAsyncService.class);

    @Autowired
    PkgService pkgService;

   
    @Autowired
    HttpService httpService;

    @Autowired
    EPKGPackageConverter epkgPackageConverter;

    @Autowired
    ObjectMapper objectMapper;

    @Async("epkgasyncServiceExecutor")
    public void executeAsync(Element e, Map<String, String> osMes, int i, String postUrl) {
        logger.info("thread name: {},  index: {}", Thread.currentThread().getName(), i);
        Map<String, String> res = pkgService.parsePkg(e, osMes);
        if ("sdbus-cpp-debugsource".equals(res.get("name"))) {
            return;
        }
        EPKGPackage ePkg = epkgPackageConverter.toEntity(res);

        String jsonPkg = "";
        try {
            jsonPkg= objectMapper.writeValueAsString(ePkg);
        } catch (JsonProcessingException ex) {
            logger.error(MessageCode.EC00014.getMsgEn(), ex);
        }
        httpService.postPkg(jsonPkg, postUrl);
    }
}
