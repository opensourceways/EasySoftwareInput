package com.easysoftwareinput.easysoftwareinput.maintainer;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.easysoftwareinput.application.rpmpackage.BatchServiceImpl;
import com.easysoftwareinput.domain.maintainer.MaintainerConfig;

@SpringBootTest
public class MaintainerExceptionTest {
    private static String localPath = "/var/local/inputeasysoftware/log/spring.log";
    private static String emptyContext = "";

    @Autowired
    private BatchServiceImpl service;

    @Autowired
    private MaintainerConfig config;

    /**
     * 1. 异常情况：无法连通sig组和maintainer的url
     */
    @Test
    public void test_without_url() throws Exception {
        // 清空日志
        FileUtils.writeStringToFile(new File(localPath), emptyContext, StandardCharsets.UTF_8);

        // 设置错误的url
        config.setMaintainerUrl(config.getMaintainerUrl() + "error");
        config.setSigUrl(config.getSigUrl() + "error");
        // 验证程序不中断运行
        boolean valid = service.run();
        assertTrue(valid);

        // 验证日志记录告警
        String log = FileUtils.readFileToString(new File(localPath), StandardCharsets.UTF_8);
        String cateogryError = log.lines().filter(
            line -> line.contains("category should not be null") && line.contains("ERROR")
        ).findAny().orElse(null);
        String maintainerError = log.lines().filter(
            line -> line.contains("category should not be null") && line.contains("ERROR")
        ).findAny().orElse(null);
        assertTrue(StringUtils.isNotBlank(maintainerError) && StringUtils.isNotBlank(cateogryError));
    }

    /**
     * 2. 异常情况: MySQL断开连接
     * @throws Exception
     */
    @Test
    public void test_without_sql() throws Exception {
        // 清空日志
        FileUtils.writeStringToFile(new File(localPath), emptyContext, StandardCharsets.UTF_8);

        // 验证程序不中断运行
        boolean valid = service.run();
        assertFalse(valid);

        // 验证日志记录MySQL告警
        String log = FileUtils.readFileToString(new File(localPath), StandardCharsets.UTF_8);
        String updateError = log.lines()
                .filter(line -> line.contains("ERROR") && line.contains("fail-to-update"))
                .findAny().orElse("");
        String getRowError = log.lines()
                .filter(line -> line.contains("ERROR") && line.contains("fail-to-getChangedRow"))
                .findAny().orElse("");
        String writeError = log.lines()
        .filter(line -> line.contains("ERROR") && line.contains("fail-to-write"))
        .findAny().orElse("");
        assertTrue(StringUtils.isNotBlank(updateError) || StringUtils.isNotBlank(getRowError)
                || StringUtils.isNotBlank(writeError));
    }

}
