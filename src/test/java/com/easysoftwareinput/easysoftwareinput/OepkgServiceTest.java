package com.easysoftwareinput.easysoftwareinput;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.easysoftwareinput.application.oepkg.OepkgService;
import com.easysoftwareinput.common.CommonUtil;
import com.easysoftwareinput.common.utils.ObjectMapperUtil;
import com.easysoftwareinput.infrastructure.mapper.OepkgDOMapper;
import com.easysoftwareinput.infrastructure.oepkg.OepkgGatewayImpl;
import com.easysoftwareinput.infrastructure.oepkg.dataobject.OepkgDO;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
public class OepkgServiceTest {
    @Autowired
    OepkgDOMapper mapper;

    @Autowired
    OepkgService service;

    @Autowired
    OepkgGatewayImpl gateway;

    @Test
    public void test_insert() {
        mapper.delete(null);
        service.run();
        assertTrue(service.validData());
        
        List<OepkgDO> d = mapper.selectList(null);
        assertPkg(d.get(0));
        assertTrue(service.validData());
    }

    public void assertPkg(OepkgDO pkg) {
        assertEquals("xorg-x11-xbitmaps", pkg.getName());
        assertEquals("1.0.1-9.1", pkg.getVersion());
        assertEquals("openEuler-20.03-LTS-SP1", pkg.getOs());
        assertEquals("x86_64", pkg.getArch());
        assertEquals("其他", pkg.getCategory());
        assertEquals("2021-10-17 06:19:09", pkg.getRpmUpdateAt());
        assertEquals("http://www.x.org", pkg.getSrcRepo());
        assertEquals("0.03MB", pkg.getRpmSize());
        assertEquals("https://repo.oepkgs.net/openeuler/rpm/openEuler-20.03-LTS-SP1/compat-centos6/standard_x86_64/x86_64/Packages/xorg-x11-xbitmaps-1.0.1-9.1.x86_64.rpm", pkg.getBinDownloadUrl());
        assertEquals(null, pkg.getSrcDownloadUrl());
        assertEquals("X.Org X11 application bitmaps", pkg.getSummary());
        assertEquals("openEuler-20.03-LTS-SP1", pkg.getOsSupport());
        assertEquals("1. 添加源\n```\ndnf config-manager --add-repo https://repo.oepkgs.net/openeuler/rpm/openEuler-20.03-LTS-SP1/compat-centos6/standard_x86_64/x86_64\n```\n2. 更新源索引\n```\ndnf clean all && dnf makecache\n```\n3. 安装 xorg-x11-xbitmaps 软件包\n```\ndnf install xorg-x11-xbitmaps\n```",
                pkg.getInstallation());
        assertEquals("X.Org X11 application bitmaps", pkg.getDescription());
        assertEquals("[{\"name\":\"/usr/bin/pkg-config\"}]", pkg.getRequires());
        assertEquals("[{\"name\":\"pkgconfig(xbitmaps)\",\"flags\":\"EQ\",\"ver\":\"1.0.1\",\"epoch\":\"0\"},{\"name\":\"xbitmaps\"},{\"name\":\"xbitmaps-devel\"},{\"name\":\"xorg-x11-xbitmaps\",\"flags\":\"EQ\",\"rel\":\"9.1\",\"ver\":\"1.0.1\",\"epoch\":\"0\"},{\"name\":\"xorg-x11-xbitmaps(x86-64)\",\"flags\":\"EQ\",\"rel\":\"9.1\",\"ver\":\"1.0.1\",\"epoch\":\"0\"}]", pkg.getProvides());
        assertEquals("[]", pkg.getConflicts());
        assertEquals("", pkg.getChangeLog());
        assertEquals("t_feng", pkg.getMaintainerId());
        assertEquals("fengtao40@huawei.com", pkg.getMaintainerEmail());
        assertEquals("t_feng", pkg.getMaintainerGiteeId());
        assertEquals("", pkg.getMaintainerUpdateAt());
        assertEquals(null, pkg.getMaintainerStatus());
        assertEquals("", pkg.getUpStream());
        assertEquals("", pkg.getSimilarPkgs());
        assertEquals("openEuler-20.03-LTS-SP1openEuler-20.03-LTS-SP1compat-centos6standard_x86_64x86_64xorg-x11-xbitmaps1.0.1-9.1x86_64", pkg.getPkgId());
        assertEquals("openEuler-20.03-LTS-SP1compat-centos6standard_x86_64x86_64", pkg.getSubPath());
        assertEquals("MIT", pkg.getLicense());

        // map: repo
        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, String> repo = ObjectMapperUtil.<String>strToMap(pkg.getRepo());
        Map<String, String> realRepo = Map.of("url", "", "type", "oepkg官方仓库");
        assertTrue(CommonUtil.assertEqualMap(repo, realRepo));

        // map: repoType
        Map<String, String> repoType = ObjectMapperUtil.<String>strToMap(pkg.getRepoType());
        Map<String, String> realRepoType = Map.of("url", "", "type", "");
        assertTrue(CommonUtil.assertEqualMap(repoType, realRepoType));
    }
}
