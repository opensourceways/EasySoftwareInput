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

package com.easysoftwareinput.easysoftwareinput;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.easysoftwareinput.application.rpmpackage.BatchServiceImpl;
import com.easysoftwareinput.domain.maintainer.MaintainerConfig;
import com.easysoftwareinput.domain.rpmpackage.model.RPMPackageDO;
import com.easysoftwareinput.infrastructure.BasePackageDO;
import com.easysoftwareinput.infrastructure.mapper.RPMPackageDOMapper;

@SpringBootTest
public class MaintainerTest {
    @Autowired
    private RPMPackageDOMapper mapper;

    @Autowired
    private BatchServiceImpl batchService;

    @Autowired
    private MaintainerConfig maintainerConfig;

    @Test
    public void test() {
        Map<String, BasePackageDO> maintainers = getMaintainer();
        List<RPMPackageDO> list = getList();
        for (RPMPackageDO pkg : list) {
            boolean check = checkPkg(pkg, maintainers);
            assertTrue(check);
        }
    }

    public boolean checkPkg(RPMPackageDO pkg, Map<String, BasePackageDO> maintainers) {
        if (StringUtils.isBlank(pkg.getSrcDownloadUrl())) {
            return true;
        }

        String srcName = getNameFromUrl(pkg.getSrcDownloadUrl());

        BasePackageDO maintainer = maintainers.get(srcName);

        String email = maintainer == null ? maintainerConfig.getEmail() : maintainer.getMaintainerEmail();
        String id = maintainer == null ? maintainerConfig.getId() : maintainer.getMaintainerId();
        String giteeId = maintainer == null ? maintainerConfig.getGiteeId() : maintainer.getMaintainerGiteeId();

        boolean b1 = email.equals(pkg.getMaintainerEmail());
        boolean b2 = giteeId.equals(pkg.getMaintainerGiteeId());
        boolean b3 = id.equals(pkg.getMaintainerId());

        return b1 && b2 && b3;
    }

    public String getNameFromUrl(String url) {
        if (StringUtils.isBlank(url)) {
            return null;
        }
        String[] splits = url.split("/");
        String srcPkg = splits[splits.length - 1];
        splits = srcPkg.split("-");
        String[] srcNames = Arrays.copyOfRange(splits, 0, splits.length - 2);
        return StringUtils.join(srcNames, "-");
    }

    public Map<String, BasePackageDO> getMaintainer() {
        Map<String, BasePackageDO> maintainers = batchService.getNames();
        return maintainers;
    }

    public List<RPMPackageDO> getList() {
        QueryWrapper<RPMPackageDO> wrapper = new QueryWrapper<>();
        wrapper.select("name", "src_download_url", "maintainer_id", "maintainer_email",
                "maintainer_gitee_id");
        return mapper.selectList(wrapper);
    }
}
