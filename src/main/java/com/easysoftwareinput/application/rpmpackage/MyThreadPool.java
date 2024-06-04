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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.dom4j.Document;
import org.dom4j.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
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
    /**
     * pkg service.
     */
    @Autowired
    private PkgService pkgService;

    /**
     * rpm converter.
     */
    @Autowired
    private RPMPackageConverter rpmPackageConverter;

    /**
     * env.
     */
    @Autowired
    private Environment env;

    /**
     * parse xml by mylti threads.
     * @param xml xml.
     * @param osMes os.
     * @param count file index.
     * @param srcUrls map of src pkgs.
     * @param maintainers maintainers.
     */
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

        log.info("finish-xml-parse, thread name: {}, list.size(): {}, time used: {}ms, fileIndex: {}",
                Thread.currentThread().getName(), pkgList.size(), (System.currentTimeMillis() - s), count);

        List<String> bodyList = filterPkg(pkgList);
        post(bodyList, env.getProperty("rpm.post-url"));
    }

    /**
     * post.
     * @param bodyList pkg to be posted.
     * @param postUrl url.
     */
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

    /**
     * filter the pkgs.
     * @param pkgList pkgs.
     * @return list of string.
     */
    private List<String> filterPkg(List<RPMPackage> pkgList) {
        ObjectMapper objectMapper = new ObjectMapper();
        List<String> bodyList = new ArrayList<>();
        for (RPMPackage pkg : pkgList) {

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

    /**
     * store the pkgs to database.
     * @param list list of pkg.
     */
    public synchronized void saveBatch(List<RPMPackageDO> list) {
        log.info("start-save-batch; thread: {}", Thread.currentThread().getName());
        long s = System.currentTimeMillis();

        saveOrUpdateBatch(list);

        log.info("finish-mysql-batch-save; thread name: {}, list.size(): {}, time used: {}ms",
                Thread.currentThread().getName(), list.size(), (System.currentTimeMillis() - s));
    }
}
