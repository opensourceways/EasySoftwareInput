package com.easysoftwareinput.repopkgname;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.easysoftwareinput.application.repopkgnamemapper.RepoPkgNameMapperService;
import com.easysoftwareinput.domain.rpmpackage.model.GitRepoConfig;
import com.easysoftwareinput.easysoftwareinput.EasysoftwareinputApplication;

@SpringBootTest(classes = {EasysoftwareinputApplication.class})
public class RepoPkgNameExceptionTest {
    private static String localPath = "/var/local/inputeasysoftware/log/spring.log";
    private static String emptyContext = "";

    @Autowired
    private GitRepoConfig config;

    @Autowired
    private RepoPkgNameMapperService service;

    /**
     * 异常情况1： 无法获取组织下仓库。
     * @throws Exception excepiton.
     */
    @Test
    public void test_without_url() throws Exception {
        // 清空日志
        FileUtils.writeStringToFile(new File(localPath), emptyContext, StandardCharsets.UTF_8);

        // 设置错误的url
        String template = config.getOrgTemplate().replace("v5", "vv");
        config.setOrgTemplate(template);
        // 验证程序不中断运行
        assertFalse(service.run());

        // 验证日志记录告警
        String log = FileUtils.readFileToString(new File(localPath), StandardCharsets.UTF_8);
        String alert = log.lines().filter(line -> line.contains("fail to get gitee repos"))
                .findAny().orElse(null);
        assertTrue(StringUtils.isNotBlank(alert));
    }

    /**
     * 异常情况2： 无法获取仓库的分支。
     * @throws Exception excepiton.
     */
    @Test
    public void test_without_branch() throws Exception {
        // 清空日志
        FileUtils.writeStringToFile(new File(localPath), emptyContext, StandardCharsets.UTF_8);

        // 设置错误的url
        String template = config.getRepoBranchTemplate().replace("v5", "vv");
        config.setRepoBranchTemplate(template);
        // 验证程序不中断运行
        assertFalse(service.run());

        // 验证日志记录告警
        String log = FileUtils.readFileToString(new File(localPath), StandardCharsets.UTF_8);
        String alert = log.lines().filter(line -> line.contains("fail to get branches"))
                .findAny().orElse(null);
        assertTrue(StringUtils.isNotBlank(alert));
    }

    /**
     * 异常情况3： 无法获取分支的目录树。
     * @throws Exception excepiton.
     */
    @Test
    public void test_without_tree() throws Exception {
        // 清空日志
        FileUtils.writeStringToFile(new File(localPath), emptyContext, StandardCharsets.UTF_8);

        // 设置错误的url
        String template = config.getTreeTemplate().replace("v5", "vv");
        config.setTreeTemplate(template);
        // 验证程序不中断运行
        assertFalse(service.run());

        // 验证日志记录告警
        String log = FileUtils.readFileToString(new File(localPath), StandardCharsets.UTF_8);
        String alert = log.lines().filter(line -> line.contains("fail to get spec"))
                .findAny().orElse(null);
        assertTrue(StringUtils.isNotBlank(alert));
    }

    /**
     * 异常情况4： 无法获取原始文本文件。
     * @throws Exception excepiton.
     */
    @Test
    public void test_without_raw() throws Exception {
        // 清空日志
        FileUtils.writeStringToFile(new File(localPath), emptyContext, StandardCharsets.UTF_8);

        // 设置错误的url
        String template = config.getBlobTextTemplate().replace("v5", "vv");
        config.setBlobTextTemplate(template);
        // 验证程序不中断运行
        assertFalse(service.run());

        // 验证日志记录告警
        String log = FileUtils.readFileToString(new File(localPath), StandardCharsets.UTF_8);
        String alert = log.lines().filter(line -> line.contains("fail to get spec"))
                .findAny().orElse(null);
        assertTrue(StringUtils.isNotBlank(alert));
    }

    /**
     * 异常情况5： 无法连接数据库。
     * @throws Exception excepiton.
     */
    @Test
    public void test_without_sql() throws Exception {
        // 清空日志
        FileUtils.writeStringToFile(new File(localPath), emptyContext, StandardCharsets.UTF_8);

        // 验证程序不中断运行
        assertFalse(service.run());

        // 验证日志记录告警
        String log = FileUtils.readFileToString(new File(localPath), StandardCharsets.UTF_8);
        String updateError = log.lines()
                .filter(line -> line.contains("ERROR") && line.contains("fail-to-update"))
                .findAny().orElse(null);
        String getRowError = log.lines()
                .filter(line -> line.contains("ERROR") && line.contains("fail-to-getChangedRow"))
                .findAny().orElse(null);
        String writeError = log.lines()
                .filter(line -> line.contains("ERROR") && line.contains("fail-to-write"))
                .findAny().orElse(null);
        assertTrue(StringUtils.isNotBlank(updateError) || StringUtils.isNotBlank(getRowError)
        || StringUtils.isNotBlank(writeError));
    }
}
