package com.easysoftwareinput.application.rpmpackage;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
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

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class BatchServiceImpl extends ServiceImpl<BasePackageDOMapper, BasePackageDO> implements BatchService {
    @Value("${rpm.dir}")
    private String rpmDir;

    @Autowired
    UpstreamService<BasePackage> upstreamService;

    @Autowired
    BasePackageDOMapper basePackageMapper;

    public void run() {
        SAXReader reader = new SAXReader();
        Document document = null;

        File fDir = new File(rpmDir);
        if (!fDir.isDirectory()) {
            log.error(MessageCode.EC00016.getMsgEn());
            return;
        }

        File[] xmlFiles = fDir.listFiles();
        for (File file : xmlFiles) {
            String filePath = "";
            try {
                filePath = file.getCanonicalPath();
            } catch (IOException e) {
                log.error(MessageCode.EC00016.getMsgEn());
                return;
            }
            try {
                document = reader.read(filePath);
                log.info("handling doc: " + file.getName());
            } catch (DocumentException e) {
                log.error(MessageCode.EC00016.getMsgEn());
            }

            List<BasePackageDO> objects = parseXml(document);
            long endTime = System.nanoTime();
            saveOrUpdateBatchData(objects);
            long endTime2 = System.nanoTime();
            long duration = (endTime2 - endTime) / 1000000;
            log.info("save cost time: " + duration + " ms, " + "pkg num: " + objects.size());
            break;
        }
    }

    private List<BasePackageDO> parseXml(Document xml) {
        ConcurrentLinkedQueue<BasePackageDO> objects = new ConcurrentLinkedQueue<>();
        ExecutorService executor = Executors.newFixedThreadPool(20);
        AtomicInteger taskCount = new AtomicInteger(xml.getRootElement().elements().size());
        log.info("task num: {}", taskCount);

        for (Element ePkg : xml.getRootElement().elements()) {
            String pkgName = ePkg.element("name").getTextTrim();
            log.info("current thread: {}, pkg name: {}", Thread.currentThread().getName(), pkgName);

            executor.execute(() -> {
                BasePackage bp = new BasePackage();
                bp.setName(pkgName);
                bp = upstreamService.addMaintainerInfo(bp);
                bp = upstreamService.addRepoCategory(bp);
                bp = upstreamService.addRepoDownload(bp);
                BasePackageDO bpDO = new BasePackageDO();
                BeanUtils.copyProperties(bp, bpDO);

                objects.add(bpDO);
                if (taskCount.decrementAndGet() == 0) {
                    synchronized (objects) {
                        objects.notify(); // 唤醒等待的线程
                    }
                }

            });
        }
        synchronized (objects) {
            try {
                objects.wait(); // 等待所有任务完成
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        return new ArrayList<>(objects);
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

    public List<BasePackageDO> readFromDatabase(String name) {
        QueryWrapper<BasePackageDO> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("name", name);
        return basePackageMapper.selectList(queryWrapper);
    }

    public static void main(String[] args) {
        String url = "jdbc:sqlite:C:\\database.db";
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
            log.info("Table 'base_package_info' created successfully.");
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }
}
