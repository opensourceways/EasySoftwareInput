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
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.easysoftwareinput.common.utils.ThreadPoolUtil;
import com.easysoftwareinput.domain.repopkgnamemapper.RepoPkgNamePkg;
import com.easysoftwareinput.domain.rpmpackage.model.GitRepoBatchConfig;

@Service
public class GitRepoBatchService {
    /**
     * logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(GitRepoBatchService.class);

    /**
     * git repo service.
     */
    @Autowired
    private GitRepoService gitRepoService;

    /**
     * config.
     */
    @Autowired
    private GitRepoBatchConfig config;

    /**
     * get all branches.
     * @param orgName org.
     * @param repoSet set of repo.
     * @return list of pkg.
     */
    public List<RepoPkgNamePkg> getAllBranches(String orgName, Set<String> repoSet) {
        List<RepoPkgNamePkg> repoVoList = new ArrayList<>();
        ExecutorService executor = Executors.newFixedThreadPool(config.getThreadPoolSize());
        int maxLimit = config.getTryLimit();
        int getCount = 0;
        while (!repoSet.isEmpty() && getCount < maxLimit) {
            getCount++;
            List<String> curRepoSet = repoSet.stream().limit(config.getConcurrentSize()).collect(Collectors.toList());
            List<RepoPkgNamePkg> curRepoVoList = getBatchBranches(orgName, curRepoSet, executor);

            Set<String> includedRepo = curRepoVoList.stream()
                    .map(RepoPkgNamePkg::getRepoName).collect(Collectors.toSet());
            repoVoList.addAll(curRepoVoList);
            repoSet.removeAll(includedRepo);
        }
        executor.shutdown();
        return repoVoList;
    }

    /**
     * get batch brances.
     * @param orgName org.
     * @param repoList repo.
     * @param executor ExecutorService.
     * @return list of pkgs.
     */
    public List<RepoPkgNamePkg> getBatchBranches(String orgName, List<String> repoList, ExecutorService executor) {
        List<CompletableFuture<List<RepoPkgNamePkg>>> taskList = new ArrayList<>();
        for (int idx = 0; idx < repoList.size(); idx++) {
            String repoName = repoList.get(idx);
            taskList.add(CompletableFuture.supplyAsync(() ->
                    gitRepoService.getBranchesFromRepo(orgName, repoName), executor));
        }
       List<List<RepoPkgNamePkg>> list = ThreadPoolUtil.<List<RepoPkgNamePkg>>getResult(taskList);
       List<RepoPkgNamePkg> res = list.stream().flatMap(Collection::stream).collect(Collectors.toList());
       return res.stream().filter(pkg -> !Objects.isNull(pkg)).collect(Collectors.toList());
    }

    /**
     * get all spec name.
     * @param pkgList list of pkg.
     * @return list of pkg with spec name.
     */
    public List<RepoPkgNamePkg> getAllSpecName(List<RepoPkgNamePkg> pkgList) {
        Map<String, RepoPkgNamePkg> map = pkgList.stream().collect(Collectors.groupingBy(
                pkg -> pkg.getOrg() + pkg.getRepoName() + pkg.getBranch(),
                Collectors.collectingAndThen(Collectors.toList(), v -> v.get(0))
        ));
        Set<String> keySet = map.keySet();

        int maxLimit = config.getTryLimit();
        int getCount = 0;
        List<RepoPkgNamePkg> resList = new ArrayList<>();
        ExecutorService executor = Executors.newFixedThreadPool(config.getThreadPoolSize());
        while (!keySet.isEmpty() && getCount < maxLimit) {
            getCount++;
            List<RepoPkgNamePkg> curKeyList = keySet.stream().limit(config.getConcurrentSize())
                    .map(k -> map.get(k)).collect(Collectors.toList());

            List<RepoPkgNamePkg> curRepoVoList = getBatchSpecName(curKeyList, executor);
            List<RepoPkgNamePkg> includedPkgList = pickPkgs(curRepoVoList);
            Set<String> includedIdSet = includedPkgList.stream()
                    .map(p -> p.getOrg() + p.getRepoName() + p.getBranch()).collect(Collectors.toSet());
            resList.addAll(includedPkgList);
            keySet.removeAll(includedIdSet);
        }
        executor.shutdown();
        return resList;
    }

    /**
     * pick pkgs.
     * @param pkgList origin pkgs.
     * @return list of pkgs.
     */
    public List<RepoPkgNamePkg> pickPkgs(List<RepoPkgNamePkg> pkgList) {
        List<RepoPkgNamePkg> resList = new ArrayList<>();
        for (RepoPkgNamePkg pkg : pkgList) {
            if (pkg == null) {
                continue;
            }
            if (pkg.isHasSpec() && StringUtils.isBlank(pkg.getRawSpecContext())) {
                continue;
            }
            resList.add(pkg);
        }
        return resList;
    }

    /**
     * get batch spec name.
     * @param pkgList pkg.
     * @param executor ExecutorService.
     * @return list of pkgs.
     */
    public List<RepoPkgNamePkg> getBatchSpecName(List<RepoPkgNamePkg> pkgList, ExecutorService executor) {
        List<CompletableFuture<RepoPkgNamePkg>> taskList = new ArrayList<>();
        for (RepoPkgNamePkg pkg : pkgList) {
            taskList.add(CompletableFuture.supplyAsync(() ->
                    gitRepoService.getSpecContextFromRepo(pkg), executor)
            );
        }
        return ThreadPoolUtil.<RepoPkgNamePkg>getResult(taskList);
    }
}
