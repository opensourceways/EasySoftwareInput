package com.easysoftwareinput.application.rpmpackage;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
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
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.easysoftwareinput.common.components.UpstreamService;
import com.easysoftwareinput.common.entity.MessageCode;
import com.easysoftwareinput.domain.rpmpackage.model.BasePackage;
import com.easysoftwareinput.infrastructure.BasePackageDO;
import com.easysoftwareinput.infrastructure.mapper.BasePackageDOMapper;

@Service
public class BatchServiceImpl extends ServiceImpl<BasePackageDOMapper, BasePackageDO> implements BatchService {
    @Value("${rpm.dir}")
    private String rpmDir;

    @Autowired
    UpstreamService<BasePackage> upstreamService;

    @Autowired
    BasePackageDOMapper basePackageMapper;

    private static final Logger logger = LoggerFactory.getLogger(BatchServiceImpl.class);

    public void upstreamInfoTask() {
        File fDir = new File(rpmDir);
        if (!fDir.isDirectory()) {
            logger.error(MessageCode.EC00016.getMsgEn());
            return;
        }

        File[] xmlFiles = fDir.listFiles();
        Set<String> pkgSet = getPkgNameListMuti(xmlFiles);
        logger.info("All pkg num: {}", pkgSet.size());
        dealByBatch(pkgSet);
        Long count = getCount();
        logger.info("count: {}", count);
    }

    private Set<String> getPkgNameList(Document xml) {
        Set<String> nameSet = new HashSet<>();
        for (Element ePkg : xml.getRootElement().elements()) {
            String pkgName = ePkg.element("name").getTextTrim();
            nameSet.add(pkgName);
        }
        return nameSet;
    }

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
                    logger.info("handling doc: " + file.getName());
                    synchronized (objects) {
                        objects.addAll(getPkgNameList(document));
                    }
                } catch (IOException | DocumentException e) {
                    logger.error(e.getMessage());
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
            logger.info("cost time: " + duration + " ms, " + "pkg num: " + subList.size());
        }
    }

    private void getUpstreamInfo(List<String> pkgs) {
        ConcurrentLinkedQueue<BasePackageDO> objects = new ConcurrentLinkedQueue<>();
        ExecutorService executor = Executors.newFixedThreadPool(30);
        CountDownLatch latch = new CountDownLatch(pkgs.size());
        for (String pkgName: pkgs) {
            // logger.info("current thread: {}, pkg name: {}", Thread.currentThread().getName(), pkgName);
            executor.execute(() -> {
                BasePackage bp = new BasePackage();
                bp.setName(pkgName);
                bp = upstreamService.addMaintainerInfo(bp);
                bp = upstreamService.addRepoCategory(bp);
                bp = upstreamService.addRepoDownload(bp);
                BasePackageDO bpDO = new BasePackageDO();
                BeanUtils.copyProperties(bp, bpDO);

                synchronized (objects) {
                    objects.add(bpDO);
                }
                latch.countDown();

            });
        }

        try {
            latch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        saveOrUpdateBatchData(new ArrayList<>(objects));
    }

    @Override
    @Transactional
    public void saveOrUpdateBatchData(List<BasePackageDO> dataObjects) {
        saveOrUpdateBatch(dataObjects);
    }

    public void writeToDatabase(BasePackage bp) {
        BasePackageDO bpDO = new BasePackageDO();
        BeanUtils.copyProperties(bp, bpDO);
        basePackageMapper.insert(bpDO);

    }

    public synchronized List<BasePackageDO> readFromDatabase(String name) {
        QueryWrapper<BasePackageDO> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("name", name);
        return basePackageMapper.selectList(queryWrapper);
    }

    public Long getCount() {
        QueryWrapper<BasePackageDO> queryWrapper = new QueryWrapper<>();
        return basePackageMapper.selectCount(queryWrapper);
    }

    public static void main(String[] args) {
        String url = "jdbc:sqlite:database.db";
        try (Connection conn = DriverManager.getConnection(url);
                Statement stmt = conn.createStatement()) {

            String sql = "DROP TABLE IF EXISTS base_package_info";
            stmt.execute(sql);

            sql = "CREATE TABLE IF NOT EXISTS base_package_info (" +
                    "name TEXT NOT NULL PRIMARY KEY," +
                    "maintainer_id TEXT," +
                    "category TEXT," +
                    "maintainer_email TEXT," +
                    "maintainer_gitee_id TEXT," +
                    "maintainer_update_at TEXT," +
                    "download_count TEXT" +
                    ")";
            stmt.execute(sql);
            logger.info("Table 'base_package_info' created successfully.");
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
    }
}
