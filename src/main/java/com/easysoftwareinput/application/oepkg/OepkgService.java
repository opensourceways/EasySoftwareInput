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

package com.easysoftwareinput.application.oepkg;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.dom4j.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import com.easysoftwareinput.application.crawldown.OepkgCrawlDownService;
import com.easysoftwareinput.application.rpmpackage.BatchServiceImpl;
import com.easysoftwareinput.application.rpmpackage.PkgService;
import com.easysoftwareinput.common.utils.FileUtil;
import com.easysoftwareinput.domain.oepkg.model.OePkgEntity;
import com.easysoftwareinput.domain.oepkg.model.OsMes;
import com.easysoftwareinput.infrastructure.BasePackageDO;
import com.easysoftwareinput.infrastructure.oepkg.OepkgGatewayImpl;

import lombok.Getter;
import lombok.Setter;

@Component
@Getter
@Setter
public class OepkgService {
    /**
     * logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(OepkgService.class);

    /**
     * gateway.
     */
    @Autowired
    private OepkgGatewayImpl gateway;

    /**
     * thread pool.
     */
    @Autowired
    @Qualifier("asyncServiceExecutor")
    private ThreadPoolTaskExecutor executor;

    /**
     * env.
     */
    @Autowired
    private Environment env;

    /**
     * crawl service.
     */
    @Autowired
    private OepkgCrawlDownService crawlService;

    /**
     * service for thread task.
     */
    @Autowired
    private ThreadService threadService;

    /**
     * pkg service.
     */
    @Autowired
    private PkgService pkgService;

    /**
     * batch service.
     */
    @Autowired
    private BatchServiceImpl batchService;

    /**
     * parameters.
     */
    private OePkgEntity oePkgEntity;

    /**
     * maintianer service.
     */
    @Autowired
    private OepkgMaintainerService maintainerService;

    /**
     * run the program.
     */
    public void run() {
        String oepkgDir = env.getProperty("oepkg.dir");
        if (StringUtils.isBlank(oepkgDir)) {
            LOGGER.error("no env: oepkg.dir");
            return;
        }
        task(oepkgDir);
        LOGGER.info("finish-oepkg");
    }

    /**
     * get srcurls from xml files.
     * @param files files.
     * @return src urls.
     */
    public Map<String, String> getPkgSrcUrls(List<String> files) {
        Map<String, String> srcUrls = new HashMap<>();
        for (String file : files) {
            Map<String, String> res = getXmlSrcUrl(file);
            if (res != null && res.size() != 0) {
                srcUrls.putAll(res);
            }
        }
        return Collections.unmodifiableMap(srcUrls);
    }

    /**
     * get src urls from xml file.
     * @param file file name.
     * @return src urls.
     */
    public Map<String, String> getXmlSrcUrl(String file) {
        if (StringUtils.isBlank(file)) {
            LOGGER.error("no src url, file: {}", file);
            return Collections.emptyMap();
        }

        // 文件名中没有`source`，认为它不是源码文件
        if (!file.contains("source")) {
            return Collections.emptyMap();
        }

        OsMes osMes = oePkgEntity.getOsMesMap().get(file);
        List<Element> pkgs = pkgService.parseElements(file);

        Map<String, String> res = new HashMap<>();
        for (Element e : pkgs) {
            Map<String, String> srcMap = getElementSrcUrl(e, osMes);
            if (srcMap != null && srcMap.size() != 0) {
                res.putAll(srcMap);
            }
        }
        return res;
    }

    /**
     * get src url from element.
     * @param e element.
     * @param osMes os message.
     * @return src urls.
     */
    public Map<String, String> getElementSrcUrl(Element e, OsMes osMes) {
        Map<String, String> src = pkgService.parseSrc(e, osMes);
        String href = StringUtils.trimToEmpty(src.get("location_href"));
        if (StringUtils.isBlank(href) || !"src".equals(src.get("arch"))) {
            return Collections.emptyMap();
        }

        String[] hrefSplits = href.split("/");
        String name;
        if (hrefSplits.length >= 2) {
            name = hrefSplits[1];
            return Map.of(name, osMes.getBaseUrl() + "/" + href);
        } else {
            return Collections.emptyMap();
        }
    }

    /**
     * get os message.
     * @param files xml files.
     * @return os message.
     */
    public Map<String, OsMes> getPkgOsMesMap(List<String> files) {
        Map<String, OsMes> osMesMap = new HashMap<>();
        for (String file : files) {
            String[] nameSplits = FileUtil.parseFileName(file);
            OsMes osMes = getOsMesFromName(nameSplits);
            if (osMes != null) {
                osMesMap.put(file, osMes);
            }
        }
        return Collections.unmodifiableMap(osMesMap);
    }

    /**
     * get os message from xml file name.
     * @param nameSplits string array.
     * @return os message.
     */
    public OsMes getOsMesFromName(String[] nameSplits) {
        if (nameSplits == null || nameSplits.length == 0) {
            return null;
        }

        OsMes osMes = new OsMes();
        osMes.setOsName("openEuler");
        osMes.setOsVer(nameSplits[0].replace("openEuler-", ""));
        if (nameSplits.length >= 2) {
            osMes.setOsType(nameSplits[1]);
        } else {
            LOGGER.error("no osType, nameSplits: {}", Arrays.toString(nameSplits));
        }
        osMes.setBaseUrl(getBaseUrl(nameSplits));
        return osMes;
    }

    /**
     * get baseurl.
     * @param nameSplits string array of file name.
     * @return baseurl.
     */
    public String getBaseUrl(String[] nameSplits) {
        String url = env.getProperty("oepkg.url");
        if (StringUtils.isBlank(url)) {
            LOGGER.error("no env: oepkg.url");
        }

        String sub;
        if (nameSplits.length >= 2) {
            sub = StringUtils.join(nameSplits, "/", 0, nameSplits.length - 1);
        } else {
            sub = "";
        }

        String res = url + sub;
        res = res.replaceAll("%3A", ":");
        return res;
    }

    /**
     * get existed ids.
     * @return existed ids.
     */
    public Set<String> getPkgExisted() {
        Set<String> res = gateway.getExistedIds();
        if (res == null) {
            return Collections.emptySet();
        }
        return res;
    }

    /**
     * get maintianers.
     * @return maintianers.
     */
    public Map<String, BasePackageDO> getPkgMaintainers() {
        return Collections.unmodifiableMap(batchService.getNames());
    }

    /**
     * init the fields of OePkgEntity.
     * @param files xml files.
     * @param oePkgEntity OePkgEntity.
     */
    public void initFields(List<String> files, OePkgEntity oePkgEntity) {
        oePkgEntity.setOsMesMap(getPkgOsMesMap(files));
        oePkgEntity.setSrcUrls(getPkgSrcUrls(files));
        oePkgEntity.setExistedPkgIds(getPkgExisted());
        oePkgEntity.setMaintainers(getPkgMaintainers());
        oePkgEntity.setStartTime(System.currentTimeMillis());
        oePkgEntity.setThreadElementSize(3_000);
        oePkgEntity.setElementSize(30_000);
        oePkgEntity.setCount(0);
        oePkgEntity.setExecutor(this.getExecutor());
    }

    /**
     * whether the data be stored.
     * @return boolean.
     */
    public boolean validData() {
        long tableRow = gateway.getChangedRow(getOePkgEntity().getStartTime());
        long updatedRow = getOePkgEntity().getCount();
        if (tableRow == updatedRow) {
            LOGGER.info("no error in storing data. need to be stored: {}, stored: {}", updatedRow, tableRow);
            return true;
        } else {
            LOGGER.error("error in storing data. need to be stored: {}, stored: {}", updatedRow, tableRow);
            return false;
        }
    }


    /**
     * start the task.
     * @param dir dir of files.
     */
    public void task(String dir) {
        crawlService.run();

        List<String> files = FileUtil.listSubMenus(dir);
        if (files == null || files.size() == 0) {
            return;
        }

        setOePkgEntity(new OePkgEntity());
        initFields(files, getOePkgEntity());

        threadService.exeByThread(files, getOePkgEntity());

        maintainerService.updateMaintainerList();

        validData();
    }
}
