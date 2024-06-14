package com.easysoftwareinput.easysoftwareinput;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.sql.SQLNonTransientConnectionException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.easysoftwareinput.application.rpmpackage.RPMPackageService;
import com.easysoftwareinput.common.CommonUtil;
import com.easysoftwareinput.common.utils.ObjectMapperUtil;
import com.easysoftwareinput.domain.rpmpackage.model.RPMPackageDO;
import com.easysoftwareinput.infrastructure.mapper.RPMPackageDOMapper;
import com.easysoftwareinput.infrastructure.rpmpkg.RpmGatewayImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
public class RPMPackageServiceTest {
    @Autowired
    RPMPackageDOMapper mapper;

    @Autowired
    RPMPackageService service;

    @Autowired
    RpmGatewayImpl gateway;

    @Test
    public void test_insert() {
        mapper.delete(null);
        service.run();
        assertTrue(service.validData());
        
        List<RPMPackageDO> d = mapper.selectList(null);
        System.out.println(d.size());
        assertPkg(d.get(0));
    }

    public void assertPkg(RPMPackageDO pkg) {
        assertEquals("Catch2", pkg.getName());
        assertEquals("3.3.2-1.oe2403", pkg.getVersion());
        assertEquals("openEuler-24.03-LTS", pkg.getOs());
        assertEquals("x86_64", pkg.getArch());
        assertEquals("其他", pkg.getCategory());
        assertEquals("2024-05-29 09:07:08", pkg.getRpmUpdateAt());
        assertEquals("https://github.com/catchorg/Catch2", pkg.getSrcRepo());
        assertEquals("0.34MB", pkg.getRpmSize());
        assertEquals("https://repo.openeuler.org/openEuler-24.03-LTS/EPOL/main/x86_64/repodata/Packages/Catch2-3.3.2-1.oe2403.x86_64.rpm", pkg.getBinDownloadUrl());
        assertEquals(null, pkg.getSrcDownloadUrl());
        assertEquals("Modern, C++-native, header-only, framework for unit-tests, TDD and BDD", pkg.getSummary());
        assertEquals("openEuler-24.03-LTS", pkg.getOsSupport());
        assertEquals("1. 添加源\n```\ndnf config-manager --add-repo https://repo.openeuler.org/openEuler-24.03-LTS/EPOL/main/x86_64/repodata\n```\n2. 更新源索引\n```\ndnf clean all && dnf makecache\n```\n3. 安装 Catch2 软件包\n```\ndnf install Catch2\n```", pkg.getInstallation());
        assertEquals("Catch stands for C++ Automated Test Cases in Headers and is a multi-paradigm automated test framework for C++ and Objective-C (and, maybe, C). It is implemented entirely in a set of header files, but is packaged up as a single header for extra convenience.", pkg.getDescription());
        assertEquals("[{\"name\":\"libgcc_s.so.1()(64bit)\"},{\"name\":\"libgcc_s.so.1(GCC_3.0)(64bit)\"},{\"name\":\"libm.so.6()(64bit)\"},{\"name\":\"libm.so.6(GLIBC_2.2.5)(64bit)\"},{\"name\":\"libm.so.6(GLIBC_2.29)(64bit)\"},{\"name\":\"libstdc++.so.6()(64bit)\"},{\"name\":\"libstdc++.so.6(CXXABI_1.3)(64bit)\"},{\"name\":\"libstdc++.so.6(CXXABI_1.3.13)(64bit)\"},{\"name\":\"libstdc++.so.6(CXXABI_1.3.3)(64bit)\"},{\"name\":\"libstdc++.so.6(CXXABI_1.3.9)(64bit)\"},{\"name\":\"libstdc++.so.6(GLIBCXX_3.4)(64bit)\"},{\"name\":\"libstdc++.so.6(GLIBCXX_3.4.11)(64bit)\"},{\"name\":\"libstdc++.so.6(GLIBCXX_3.4.14)(64bit)\"},{\"name\":\"libstdc++.so.6(GLIBCXX_3.4.15)(64bit)\"},{\"name\":\"libstdc++.so.6(GLIBCXX_3.4.18)(64bit)\"},{\"name\":\"libstdc++.so.6(GLIBCXX_3.4.19)(64bit)\"},{\"name\":\"libstdc++.so.6(GLIBCXX_3.4.20)(64bit)\"},{\"name\":\"libstdc++.so.6(GLIBCXX_3.4.21)(64bit)\"},{\"name\":\"libstdc++.so.6(GLIBCXX_3.4.22)(64bit)\"},{\"name\":\"libstdc++.so.6(GLIBCXX_3.4.29)(64bit)\"},{\"name\":\"libstdc++.so.6(GLIBCXX_3.4.30)(64bit)\"},{\"name\":\"libstdc++.so.6(GLIBCXX_3.4.9)(64bit)\"},{\"name\":\"rtld(GNU_HASH)\"},{\"name\":\"libc.so.6(GLIBC_2.38)(64bit)\"}]", pkg.getRequires());
        assertEquals("[{\"name\":\"Catch2\",\"flags\":\"EQ\",\"rel\":\"1.oe2403\",\"ver\":\"3.3.2\",\"epoch\":\"0\"},{\"name\":\"Catch2(x86-64)\",\"flags\":\"EQ\",\"rel\":\"1.oe2403\",\"ver\":\"3.3.2\",\"epoch\":\"0\"},{\"name\":\"libCatch2.so.3.3.2()(64bit)\"},{\"name\":\"libCatch2Main.so.3.3.2()(64bit)\"}]", pkg.getProvides());
        assertEquals("[]", pkg.getConflicts());
        assertEquals("", pkg.getChangeLog());
        assertEquals("davidhan008", pkg.getMaintainerId());
        assertEquals(null, pkg.getMaintainerEmail());
        assertEquals("davidhan008", pkg.getMaintainerGiteeId());
        assertEquals("", pkg.getMaintainerUpdateAt());
        assertEquals(null, pkg.getMaintainerStatus());
        assertEquals("", pkg.getUpStream());
        assertEquals("", pkg.getSimilarPkgs());
        assertEquals("openEuler-24.03-LTSEPOLmainx86_64repodataCatch23.3.2-1.oe2403x86_64", pkg.getPkgId());
        assertEquals("EPOLmainx86_64repodata", pkg.getSubPath());
        assertEquals("BSL-1.0", pkg.getLicense());

        // map: repo
        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, String> repo = ObjectMapperUtil.<String>strToMap(pkg.getRepo());
        Map<String, String> realRepo = Map.of("url", "https://gitee.com/src-openEuler/", "type", "openEuler官方仓库");
        assertTrue(CommonUtil.assertEqualMap(repo, realRepo));

        // map: repoType
        Map<String, String> repoType = ObjectMapperUtil.<String>strToMap(pkg.getRepoType());
        Map<String, String> realRepoType = Map.of("url", "https://gitee.com/src-openEuler/Catch2", "type", "EPOL");
        assertTrue(CommonUtil.assertEqualMap(repoType, realRepoType));

    }

}
