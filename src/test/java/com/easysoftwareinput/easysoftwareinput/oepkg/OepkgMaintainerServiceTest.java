package com.easysoftwareinput.easysoftwareinput.oepkg;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.easysoftwareinput.application.oepkg.OepkgMaintainerService;
import com.easysoftwareinput.domain.oepkg.model.OepkgMaintainer;
import com.easysoftwareinput.domain.oepkg.model.OepkgMaintainerConfig;
import com.easysoftwareinput.infrastructure.epkgpkg.dataobject.EpkgDo;
import com.easysoftwareinput.infrastructure.oepkg.OepkgGatewayImpl;
import com.easysoftwareinput.infrastructure.oepkg.dataobject.OepkgDO;

@SpringBootTest
public class OepkgMaintainerServiceTest {
    @Autowired
    private OepkgGatewayImpl gateway;

    @Autowired
    private OepkgMaintainerService service;

    @Autowired
    private OepkgMaintainerConfig maintianerConfig;
    /**
     * 正常情况。
     */
    @Test
    public void test() {
        // 初始化数据库
        initDB();
        // 执行业务
        service.updateMaintainerList();
        // 验证数据
        validData();
    }

    /**
     * 异常情况1：断开MySQL服务器
     */
    @Test
    public void test_without_sql() throws Exception {
        // 删除日志
        String logPath = "/var/local/inputeasysoftware/log/spring.log";
        FileUtils.writeStringToFile(new File(logPath), "", StandardCharsets.UTF_8);
        // 初始化数据库
        initDB();
        // 执行业务
        service.updateMaintainerList();
        // 验证数据

        validLog(logPath, "fail update, name: ");
    }
    /**
     * 异常情况2：配置文件中没有配置名称
     */
    @Test
    public void test_without_maintainer() throws Exception {
        String logPath = "/var/local/inputeasysoftware/log/spring.log";
        FileUtils.writeStringToFile(new File(logPath), "", StandardCharsets.UTF_8);
        // 初始化数据库
        initDB();
        // 执行业务
        service.updateMaintainerList();
        // 验证数据
        validLog(logPath, "undefined pkg name");
    }

    public void validLog(String logPath, String keyLog) throws Exception {
        String log = FileUtils.readFileToString(new File(logPath), StandardCharsets.UTF_8);
        long num = log.lines().filter(line -> line.contains(keyLog)).count();
        assertTrue(num != 0);
    }

    public void validData() {
        QueryWrapper<OepkgDO> wrapper = new QueryWrapper<>();
        wrapper.select("name", "maintainer_id", "maintainer_gitee_id", "maintainer_email", "category");
        List<OepkgDO> list = gateway.getBaseMapper().selectList(wrapper);
        List<OepkgMaintainer> maintainerList = maintianerConfig.getMaintainerList();
        Map<String, OepkgMaintainer> map = maintainerList.stream().collect(
            Collectors.toMap(OepkgMaintainer::getPkgName, Function.identity(), (oldObj, newObj) -> oldObj)
        );

        for (OepkgDO pkg : list) {
            OepkgMaintainer maintainer = map.get(pkg.getName());
            assertEquals(maintainer.getCategory(), pkg.getCategory());
            assertEquals(maintainer.getEmail(), pkg.getMaintainerEmail());
            assertEquals(maintainer.getGiteeId(), pkg.getMaintainerGiteeId());
            assertEquals(maintainer.getId(), pkg.getMaintainerId());
        }
    }

    public void initDB() {
        Map<String, Object> deleteMap = Map.of("1", "1");
        gateway.getBaseMapper().deleteByMap(deleteMap);
        List<String> names = List.of("epkg", "VSCode", "eulercopilot-cli", "x2openEuler");
        for (int i = 0; i < names.size(); i++) {
            OepkgDO oepkg = new OepkgDO();
            oepkg.setId(String.valueOf(i));
            oepkg.setPkgId(String.valueOf(i));
            oepkg.setOs("openEuler-22.09");
            oepkg.setName(names.get(i));
            gateway.getBaseMapper().insert(oepkg);
        }
    }
}
