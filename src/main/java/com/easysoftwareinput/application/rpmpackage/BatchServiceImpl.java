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

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.easysoftwareinput.common.components.UpstreamService;
import com.easysoftwareinput.common.constant.MapConstant;
import com.easysoftwareinput.common.entity.MessageCode;
import com.easysoftwareinput.common.utils.HttpClientUtil;
import com.easysoftwareinput.common.utils.ObjectMapperUtil;
import com.easysoftwareinput.domain.maintainer.MaintainerConfig;
import com.easysoftwareinput.domain.rpmpackage.model.BasePackage;
import com.easysoftwareinput.infrastructure.BasePackageDO;
import com.easysoftwareinput.infrastructure.mapper.BasePackageDOMapper;
import com.easysoftwareinput.infrastructure.rpmpkg.Gateway;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;


@Service
public class BatchServiceImpl extends ServiceImpl<BasePackageDOMapper, BasePackageDO>
        implements Gateway<BasePackageDO> {
    /**
     * path of rpm file.
     */
    @Value("${rpm.dir}")
    private String rpmDir;

    /**
     * upstream service.
     */
    @Autowired
    private UpstreamService<BasePackage> upstreamService;

    /**
     * base package mapper.
     */
    @Autowired
    private BasePackageDOMapper basePackageMapper;

    /**
     * maintainer config.
     */
    @Autowired
    private MaintainerConfig maintainerConfig;

    /**
     * logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(BatchServiceImpl.class);

    /**
     * run the program.
     * @return boolean.
     */
    public boolean run() {
        Map<String, String> categoryMap = getAllSig();
        if (categoryMap.isEmpty()) {
            LOGGER.error("category should not be null");
            return false;
        }

        Map<String, Map<String, String>> maintainerMap = getAllMaintainer();
        if (maintainerMap.isEmpty()) {
            LOGGER.error("maintainer should not be null");
            return false;
        }

        return storeData(categoryMap, maintainerMap);
    }

    /**
     * store the cateogry and maintainer.
     * @param categoryMap cateogry.
     * @param maintainerMap maintainer.
     * @return boolean.
     */
    public boolean storeData(Map<String, String> categoryMap, Map<String, Map<String, String>> maintainerMap) {
        long startTime = System.currentTimeMillis();
        List<BasePackageDO> bpDoList = assembleBasePkgDo(categoryMap, maintainerMap);
        Set<String> existedNames = getExistedNames();
        Map<Boolean, List<BasePackageDO>> map = bpDoList.stream().collect(Collectors.groupingBy(
            pkg -> existedNames.contains(pkg.getName())
        ));
        saveAndUpdate(map);

        return validData(startTime, bpDoList.size());
    }

    /**
     * valid data.
     * @param startTime start time of current service.
     * @param pkgSize pkg size.
     * @return boolean.
     */
    public boolean validData(long startTime, long pkgSize) {
        long row = getChangedRow(startTime);
        if (row == pkgSize) {
            LOGGER.info("no error, changed size: {}", row);
            return true;
        } else {
            LOGGER.error("error in storing data, changed size: {}, needed to be changed: {}", row, pkgSize);
            return false;
        }
    }

    /**
     * get existed names.
     * @return set of names.
     */
    public Set<String> getExistedNames() {
        List<BasePackageDO> bpDoList = lambdaQuery().select(BasePackageDO::getName).list();
        return bpDoList.stream().map(BasePackageDO::getName).collect(Collectors.toSet());
    }

    /**
     * assemble BasePackageDO with categoryMap and maintainerMap.
     * @param categoryMap categoryMap.
     * @param maintainerMap maintainerMap.
     * @return list of BasePackageDO.
     */
    public List<BasePackageDO> assembleBasePkgDo(Map<String, String> categoryMap,
            Map<String, Map<String, String>> maintainerMap) {
        Set<String> repoSet = new HashSet<>();
        repoSet.addAll(categoryMap.keySet());
        repoSet.addAll(maintainerMap.keySet());

        List<BasePackageDO> bpDoList = new ArrayList<>();
        for (String repo : repoSet) {
            String category = categoryMap.get(repo);
            Map<String, String> maintainer = maintainerMap.get(repo);

            BasePackageDO bpDo = new BasePackageDO();
            bpDo.setName(repo);
            bpDo.setUpdateAt(new Timestamp(System.currentTimeMillis()));
            setPkgCategory(bpDo, category);
            setPkgMaintainer(bpDo, maintainer);
            bpDoList.add(bpDo);
        }
        return bpDoList;
    }

    /**
     * set maintianer.
     * @param bpDo BasePackageDO.
     * @param maintainer maintainer.
     */
    public void setPkgMaintainer(BasePackageDO bpDo, Map<String, String> maintainer) {
        if (maintainer == null) {
            bpDo.setMaintainerEmail(maintainerConfig.getEmail());
            bpDo.setMaintainerGiteeId(maintainerConfig.getGiteeId());
            bpDo.setMaintainerId(maintainerConfig.getId());
            LOGGER.info("no maintainer, pkg name: {}", bpDo.getName());
            return;
        }

        String giteeId = StringUtils.isBlank(maintainer.get("gitee_id")) ? maintainerConfig.getGiteeId()
                : maintainer.get("gitee_id");
        String id = StringUtils.isBlank(maintainer.get("name")) ? maintainerConfig.getId()
                : maintainer.get("name");
        String email = StringUtils.isBlank(maintainer.get("email")) ? maintainerConfig.getEmail()
                : maintainer.get("email");
        bpDo.setMaintainerGiteeId(giteeId);
        bpDo.setMaintainerId(id);
        bpDo.setMaintainerEmail(email);

        if (maintainerConfig.getGiteeId().equals(giteeId) || maintainerConfig.getId().equals(id)
                || maintainerConfig.getEmail().equals(email)) {
            LOGGER.info("no maintainer, pkg name: {}", bpDo.getName());
                }
    }

    /**
     * set category of pkg.
     * @param bpDo BasePackageDO.
     * @param category category.
     */
    public void setPkgCategory(BasePackageDO bpDo, String category) {
        category = StringUtils.isBlank(category) ? MapConstant.CATEGORY_MAP.get("Other") : category;
        bpDo.setCategory(category);
    }

    /**
     * get all maintainers.
     * @return maintainers.
     */
    public Map<String, Map<String, String>> getAllMaintainer() {
        JsonNode data = HttpClientUtil.getApiResponseJson(maintainerConfig.getMaintainerUrl());
        if (data == null || !(data instanceof ArrayNode)) {
            return Collections.emptyMap();
        }
        Map<String, Map<String, String>> mMap = new HashMap<>();
        for (JsonNode maintainer : data) {
            Map<String, Map<String, String>> map = ObjectMapperUtil.<Map<String, String>>jsonToMap(maintainer);
            mMap.putAll(map);
        }
        return mMap;
    }

    /**
     * get all sigs.
     * @return map of sigs.
     */
    public Map<String, String> getAllSig() {
        JsonNode data = HttpClientUtil.getApiResponseJson(maintainerConfig.getSigUrl());
        if (data == null || !(data instanceof ArrayNode)) {
            return Collections.emptyMap();
        }
        Map<String, String> sigMap = new HashMap<>();
        for (JsonNode sig : data) {
            Map<String, String> map = ObjectMapperUtil.<String>jsonToMap(sig);
            sigMap.putAll(map);
        }

        Map<String, String> categoryMap = new HashMap<>();
        for (Map.Entry<String, String> entry : sigMap.entrySet()) {
            String repoName = entry.getKey();
            String sig = entry.getValue();

            String category = MapConstant.CATEGORY_MAP.get(sig);
            category = StringUtils.isBlank(category) ? MapConstant.CATEGORY_MAP.get("Other") : category;
            categoryMap.put(repoName, category);
        }
        return categoryMap;
    }

    /**
     * get upstream.
     */
    public void upstreamInfoTask() {
        File fDir = new File(rpmDir);
        if (!fDir.isDirectory()) {
            LOGGER.error(MessageCode.EC00016.getMsgEn());
            return;
        }

        File[] xmlFiles = fDir.listFiles();
        if (xmlFiles == null || xmlFiles.length == 0) {
            LOGGER.error("no files in dir: {}", rpmDir);
            return;
        }
        Set<String> pkgSet = getPkgNameListMuti(xmlFiles);
        LOGGER.info("All pkg num: {}", pkgSet.size());
        dealByBatch(pkgSet);
        Long count = getCount();
        LOGGER.info("count: {}", count);
    }

    /**
     * get pkg name list.
     * @param xml xml file.
     * @return pkg name list.
     */
    private Set<String> getPkgNameList(Document xml) {
        Set<String> nameSet = new HashSet<>();
        for (Element ePkg : xml.getRootElement().elements()) {
            String pkgName = ePkg.element("name").getTextTrim();
            nameSet.add(pkgName);
        }
        return nameSet;
    }

    /**
     * get names of all xml.
     * @param xmlFiles xml files.
     * @return names.
     */
    private Set<String> getPkgNameListMuti(File[] xmlFiles) {
        ConcurrentLinkedQueue<String> objects = new ConcurrentLinkedQueue<>();
        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch latch = new CountDownLatch(xmlFiles.length);
        for (File file : xmlFiles) {
            executor.execute(() -> {
                try {
                    String filePath = file.getCanonicalPath();
                    SAXReader reader = new SAXReader();
                    Document document = reader.read(filePath);
                    LOGGER.info("handling doc: " + file.getName());
                    objects.addAll(getPkgNameList(document));
                } catch (IOException | DocumentException e) {
                    LOGGER.error(e.getMessage());
                } finally {
                    latch.countDown();
                }
            });
        }
        try {
            latch.await(); // 等待所有任务完成
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        Set<String> nameSet = new HashSet<>(objects);
        return nameSet;
    }

    /**
     * get upstream by batch.
     * @param pkgSet pkg names.
     */
    private void dealByBatch(Set<String> pkgSet) {
        List<String> originalList = new ArrayList<>(pkgSet);
        final int batchSize = 500;

        for (int i = 0; i < originalList.size(); i += batchSize) {
            long startTime = System.nanoTime();
            int end = Math.min(originalList.size(), i + batchSize);
            List<String> subList = originalList.subList(i, end);
            getUpstreamInfo(subList);
            long endTime = System.nanoTime();
            long duration = (endTime - startTime) / 1000000;
            LOGGER.info("cost time: " + duration + " ms, " + "pkg num: " + subList.size());
        }
    }

    /**
     * get upstream.
     * @param pkgs list of pkg name.
     */
    private void getUpstreamInfo(List<String> pkgs) {
        ConcurrentLinkedQueue<BasePackageDO> objects = new ConcurrentLinkedQueue<>();
        ExecutorService executor = Executors.newFixedThreadPool(30);
        CountDownLatch latch = new CountDownLatch(pkgs.size());
        for (String pkgName: pkgs) {
            // LOGGER.info("current thread: {}, pkg name: {}", Thread.currentThread().getName(), pkgName);
            executor.execute(() -> {
                BasePackage bp = new BasePackage();
                bp.setName(pkgName);
                bp = upstreamService.addMaintainerInfo(bp);
                if (StringUtils.isBlank(bp.getMaintainerEmail())) {
                    bp.setMaintainerEmail(maintainerConfig.getEmail());
                }
                if (StringUtils.isBlank(bp.getMaintainerId())) {
                    bp.setMaintainerId(maintainerConfig.getId());
                }
                if (StringUtils.isBlank(bp.getMaintainerGiteeId())) {
                    bp.setMaintainerGiteeId(maintainerConfig.getGiteeId());
                }
                bp = upstreamService.addRepoCategory(bp);
                // bp = upstreamService.addRepoDownload(bp);
                BasePackageDO bpDO = new BasePackageDO();
                BeanUtils.copyProperties(bp, bpDO);
                bpDO.setUpdateAt(new Timestamp(System.currentTimeMillis()));
                objects.add(bpDO);
                latch.countDown();
            });
        }
        try {
            latch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * insert one pkg to databse.
     * @param bp pkg.
     */
    public void writeToDatabase(BasePackage bp) {
        BasePackageDO bpDO = new BasePackageDO();
        BeanUtils.copyProperties(bp, bpDO);
        basePackageMapper.insert(bpDO);
    }

    /**
     * get names from databse.
     * @return list of names.
     */
    public Map<String, BasePackageDO> getNames() {
        List<BasePackageDO> list = basePackageMapper.selectList(null);
        Map<String, BasePackageDO> res = new HashMap<>();
        for (BasePackageDO base : list) {
            res.put(base.getName(), base);
        }
        return res;
    }

    /**
     * query pkg by name.
     * @param name name.
     * @return list of pkg.
     */
    public List<BasePackageDO> readFromDatabase(String name) {
        QueryWrapper<BasePackageDO> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("name", name);
        return basePackageMapper.selectList(queryWrapper);
    }

    /**
     * query count of pkgs.
     * @return count.
     */
    public Long getCount() {
        QueryWrapper<BasePackageDO> queryWrapper = new QueryWrapper<>();
        return basePackageMapper.selectCount(queryWrapper);
    }

    /**
     * execute the program.
     * @param args args.
     */
    public static void main(String[] args) {
        String url = "jdbc:sqlite:database.db";
        try (Connection conn = DriverManager.getConnection(url);
                Statement stmt = conn.createStatement()) {

            String sql = "DROP TABLE IF EXISTS base_package_info";
            stmt.execute(sql);

            sql = "CREATE TABLE IF NOT EXISTS base_package_info ("
                    + "name TEXT NOT NULL PRIMARY KEY,"
                    + "maintainer_id TEXT,"
                    + "category TEXT,"
                    + "maintainer_email TEXT,"
                    + "maintainer_gitee_id TEXT,"
                    + "maintainer_update_at TEXT,"
                    + "download_count TEXT"
                    + ")";
            stmt.execute(sql);
            LOGGER.info("Table 'base_package_info' created successfully.");
        } catch (Exception e) {
            LOGGER.error("fail to creatable table, cause: {}", e.getMessage());
        }
    }

    /**
     * get logger.
     */
    @Override
    public Logger getLogger() {
        return BatchServiceImpl.LOGGER;
    }
}
