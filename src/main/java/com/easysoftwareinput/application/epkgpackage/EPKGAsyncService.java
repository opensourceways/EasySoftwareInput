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

package com.easysoftwareinput.application.epkgpackage;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.dom4j.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.easysoftwareinput.application.rpmpackage.PkgService;
import com.easysoftwareinput.domain.epkgpackage.ability.EPKGPackageConverter;
import com.easysoftwareinput.domain.epkgpackage.model.EPKGPackage;
import com.easysoftwareinput.infrastructure.epkgpkg.EpkgGatewayImpl;

@Service
public class EPKGAsyncService {
    /**
     * logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(EPKGAsyncService.class);

    /**
     * pkgservice.
     */
    @Autowired
    private PkgService pkgService;

    /**
     * gateway.
     */
    @Autowired
    private EpkgGatewayImpl gateway;

    /**
     * epkg pkg converter.
     */
    @Autowired
    private EPKGPackageConverter epkgPackageConverter;

    /**
     * execute parse epkg xml by multi thread.
     * @param eList list of pkg.
     * @param osMes os.
     * @param i file index.
     * @param postUrl posturl.
     * @param srcMap src pkg name and url.
     */
    @Async("epkgasyncServiceExecutor")
    public void executeAsync(List<Element> eList, Map<String, String> osMes, int i, String postUrl,
            Map<String, String> srcMap) {
        List<EPKGPackage> pkgList = new ArrayList<>(eList.size());
        Set<String> pkgIds = new HashSet<>();

        for (Element e : eList) {
            Map<String, String> res = pkgService.parsePkg(e, osMes);

            EPKGPackage ePkg = epkgPackageConverter.toEntity(res, srcMap);

            // 不添加源码包
            if ("src".equals(ePkg.getArch())) {
                continue;
            }

            // 舍弃主键重复的数据
            if (pkgIds.add(ePkg.getPkgId())) {
                pkgList.add(ePkg);
            }
        }
        gateway.saveAll(pkgList);
    }
}
