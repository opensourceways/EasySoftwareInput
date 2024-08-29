package com.easysoftwareinput.easysoftwareinput.srcrepo;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.easysoftwareinput.application.repopkgnamemapper.RepoPkgNameMapperService;
import com.easysoftwareinput.common.constant.MapConstant;
import com.easysoftwareinput.domain.repopkgnamemapper.RepoPkgNameMapperConfig;
import com.easysoftwareinput.infrastructure.repopkgnamemapper.RepoPkgNameMapperGatewayImpl;
import com.easysoftwareinput.infrastructure.repopkgnamemapper.dataobject.RepoPkgNameDO;

@SpringBootTest
public class AddPkgManuallyTest {
    @Autowired
    private RepoPkgNameMapperService service;

    @Autowired
    private RepoPkgNameMapperGatewayImpl gateway;

    @Autowired
    private RepoPkgNameMapperConfig config;

    @Test
    public void test() {
        Map<String, Object> deleteMap = Map.of("1", "1");
        gateway.getBaseMapper().deleteByMap(deleteMap);
        AtomicLong atL = new AtomicLong(0);
        service.addPkgManually(atL);

        List<RepoPkgNameDO> list = gateway.getBaseMapper().selectList(null);

        Map<String, String> pkgRepo = MapConstant.PKG_REPO_MAP;
        for (Map.Entry<String, String> entry : pkgRepo.entrySet()) {
            String name = entry.getKey();
            String repoUrl = String.format(config.getRepoUrlTemplate(), config.getOrg(), entry.getValue());
            assertTrue(containsEntry(list, name, repoUrl));
        }
    }

    public boolean containsEntry(List<RepoPkgNameDO> list, String name, String repoUrl) {
        for (RepoPkgNameDO pkg : list) {
            if (name.equals(pkg.getName()) && repoUrl.equals(pkg.getRepoUrl())) {
                return true;
            }
        }
        return false;
    }
}
