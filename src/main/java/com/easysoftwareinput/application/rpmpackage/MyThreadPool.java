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

package com.easysoftwareinput.application.rpmpackage;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import org.dom4j.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.easysoftwareinput.domain.rpmpackage.ability.RPMPackageConverter;
import com.easysoftwareinput.domain.rpmpackage.model.RPMPackage;
import com.easysoftwareinput.domain.rpmpackage.model.RPMPackageDO;
import com.easysoftwareinput.domain.rpmpackage.model.RpmContext;
import com.easysoftwareinput.infrastructure.mapper.RPMPackageDOMapper;
import com.easysoftwareinput.infrastructure.rpmpkg.RpmGatewayImpl;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class MyThreadPool extends ServiceImpl<RPMPackageDOMapper, RPMPackageDO> {
    /**
     * pkg service.
     */
    @Autowired
    private PkgService pkgService;

    /**
     * rpm converter.
     */
    @Autowired
    private RPMPackageConverter rpmPackageConverter;

    /**
     * env.
     */
    @Autowired
    private Environment env;

    /**
     * rpm gateway.
     */
    @Autowired
    private RpmGatewayImpl gateway;

    /**
     * parse xml by mylti threads.
     * @param context context.
     * @return CompletableFuture.
     */
    @Async("asyncServiceExecutor")
    public CompletableFuture<Void> parseXml(RpmContext context) {
        List<Element> pkgs = context.getDoc().getRootElement().elements();

        long s = System.currentTimeMillis();
        List<RPMPackage> pkgList = new ArrayList<>();
        Set<String> pkgIds = new HashSet<>();
        for (int i = 0; i < pkgs.size(); i++) {
            Element ePkg = pkgs.get(i);
            Map<String, String> res = pkgService.parsePkg(ePkg, context.getOsMes());
            RPMPackage pkg = rpmPackageConverter.toEntity(res, context.getSrcUrls(), context.getMaintainers(),
                    context.getRepoNames());

            // 如果架构是src，则不写入
            if ("src".equals(pkg.getArch())) {
                continue;
            }
            // 舍弃主键重复的数据
            if (pkgIds.add(pkg.getPkgId())) {
                pkgList.add(pkg);
            }
        }

        log.info("finish-xml-parse, thread name: {}, list.size(): {}, time used: {}ms, fileIndex: {}",
                Thread.currentThread().getName(), pkgList.size(), (System.currentTimeMillis() - s), context.getCount());

        gateway.saveAll(pkgList, context.getExistedPkgIdSet());
        context.getCount().addAndGet(pkgList.size());
        return CompletableFuture.completedFuture(null);
    }

    /**
     * store the pkgs to database.
     * @param list list of pkg.
     */
    public synchronized void saveBatch(List<RPMPackageDO> list) {
        log.info("start-save-batch; thread: {}", Thread.currentThread().getName());
        long s = System.currentTimeMillis();

        saveOrUpdateBatch(list);

        log.info("finish-mysql-batch-save; thread name: {}, list.size(): {}, time used: {}ms",
                Thread.currentThread().getName(), list.size(), (System.currentTimeMillis() - s));
    }
}
