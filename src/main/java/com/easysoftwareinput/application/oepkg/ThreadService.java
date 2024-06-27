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

package com.easysoftwareinput.application.oepkg;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import org.dom4j.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.easysoftwareinput.application.rpmpackage.PkgService;
import com.easysoftwareinput.domain.oepkg.model.FilePkgEntity;
import com.easysoftwareinput.domain.oepkg.model.OePkg;
import com.easysoftwareinput.domain.oepkg.model.OePkgEntity;
import com.easysoftwareinput.domain.oepkg.model.OsMes;
import com.easysoftwareinput.domain.oepkg.model.ThreadPkgEntity;
import com.easysoftwareinput.infrastructure.BasePackageDO;
import com.easysoftwareinput.infrastructure.oepkg.OepkgGatewayImpl;
import com.easysoftwareinput.infrastructure.oepkg.converter.OepkgConverter;

import lombok.Getter;
import lombok.Setter;

@Component
@Getter
@Setter
public class ThreadService {
    /**
     * logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(ThreadService.class);

    /**
     * pkg service.
     */
    @Autowired
    private PkgService pkgService;

    /**
     * pkg converter.
     */
    @Autowired
    private OepkgConverter converter;

    /**
     * pkg gateway.
     */
    @Autowired
    private OepkgGatewayImpl gateway;

    /**
     * oepkg entity for each thread.
     */
    private OePkgEntity oePkgEntity;

    /**
     * get src urls.
     * @return src urls.
     */
    public Map<String, String> getSrcUrls() {
        if (oePkgEntity != null) {
            return oePkgEntity.getSrcUrls();
        }
        return null;
    }

    /**
     * get maintianers.
     * @return maintainers.
     */
    public Map<String, BasePackageDO> getMaintainers() {
        if (oePkgEntity != null) {
            return oePkgEntity.getMaintainers();
        }
        return null;
    }

    /**
     * parse files by thread.
     * @param files files.
     * @param oePkgEntity oepkg entity.
     */
    public void exeByThread(List<String> files, OePkgEntity oePkgEntity) {
        setOePkgEntity(oePkgEntity);

        // 按每个文件处理
        for (int fileIndex = 0; fileIndex < files.size(); fileIndex++) {
            String fileName = files.get(fileIndex);
            OsMes osMes = getOePkgEntity().getOsMesMap().get(fileName);

            FilePkgEntity fPkg = FilePkgEntity.of(files.get(fileIndex), fileIndex, osMes);

            List<OePkg> oList = execFile(fPkg);
            getOePkgEntity().setCount(getOePkgEntity().getCount() + oList.size());
            saveEachFile(oList, fPkg);
        }
    }

    /**
     * parse each file.
     * @param fPkg entity of file.
     * @return list of pkgs.
     */
    public List<OePkg> execFile(FilePkgEntity fPkg) {
        String filePath = fPkg.getFileName();

        List<Element> pkgs = pkgService.parseElements(filePath);
        if (pkgs == null || pkgs.size() == 0) {
            LOGGER.info("no pkgs in file: {}", filePath);
            return Collections.emptyList();
        }

        List<OePkg> oList = new ArrayList<>();
        Set<String> pkgIds = new HashSet<>();
        for (Element e : pkgs) {
            Map<String, String> res = pkgService.parsePkg(e, fPkg.getOsMes());
            OePkg pkg = converter.toEntity(res, getSrcUrls(), getMaintainers());

            // 不添加架构为src的包
            if ("src".equals(pkg.getArch())) {
                continue;
            }

            if (pkgIds.add(pkg.getPkgId())) {
                oList.add(pkg);
            }
        }
        return oList;
    }

    /**
     * save pkgs by each file.
     * @param oList pkgs.
     * @param fPkg message of file.
     */
    public void saveEachFile(List<OePkg> oList, FilePkgEntity fPkg) {
        List<CompletableFuture<Boolean>> fuList = new ArrayList<>();
        for (int start = 0; start < oList.size(); start += getOePkgEntity().getThreadElementSize()) {
            int end = Math.min(oList.size(), start + getOePkgEntity().getThreadElementSize());

            ThreadPkgEntity tPkg = ThreadPkgEntity.of(fPkg, start, end, oList.subList(start, end));
            CompletableFuture<Boolean> fu = CompletableFuture.supplyAsync(() -> {
                return gateway.saveAll(tPkg, getOePkgEntity());
            });
            fuList.add(fu);
        }

        postProcessThread(fuList);
    }

    /**
     * post process after thread.
     * @param fuList list of CompletableFuture.
     */
    public void postProcessThread(List<CompletableFuture<Boolean>> fuList) {
        if (fuList == null || fuList.size() == 0) {
            return;
        }

        for (CompletableFuture<Boolean> f : fuList) {
            try {
                f.get();
            } catch (Exception e) {
                LOGGER.error("thread failed, e: {}", e.getMessage());
            }
        }
    }
}
