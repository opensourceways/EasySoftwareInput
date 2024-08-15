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

package com.easysoftwareinput.application.repopkgnamemapper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.easysoftwareinput.application.rpmpackage.GitRepoBatchService;
import com.easysoftwareinput.application.rpmpackage.GitRepoService;
import com.easysoftwareinput.domain.repopkgnamemapper.RepoPkgNameMapperConfig;
import com.easysoftwareinput.domain.repopkgnamemapper.RepoPkgNamePkg;
import com.easysoftwareinput.infrastructure.repopkgnamemapper.RepoPkgNameMapperGatewayImpl;
import com.easysoftwareinput.infrastructure.repopkgnamemapper.dataobject.RepoPkgNameDO;
import com.power.common.util.Base64Util;

@Component
public class RepoPkgNameMapperService {
    /**
     *  start marker of name.
     */
    private static final String START_MARKER = "%{";
    /**
     * end marker of name.
     */
    private static final String END_MARKER = "}";

    /**
     * logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(RepoPkgNameMapperService.class);

    /**
     * git repo service.
     */
    @Autowired
    private GitRepoService gitRepoService;

    /**
     * repo config.
     */
    @Autowired
    private RepoPkgNameMapperConfig config;

    /**
     * git service.
     */
    @Autowired
    private GitRepoBatchService gitRepoBatchService;

    /**
     * repo gateway.
     */
    @Autowired
    private RepoPkgNameMapperGatewayImpl gateway;

    /**
     * run the program.
     * @return boolean.
     */
    public boolean run() {
        long start = System.currentTimeMillis();
        Set<String> repoNames = gitRepoService.getOrgRepos(config.getOrg());
        if (repoNames == null || repoNames.isEmpty()) {
            LOGGER.error("fail to get gitee repos");
            return false;
        }

        AtomicLong pkgSize = new AtomicLong(0);
        while (!repoNames.isEmpty()) {
            List<RepoPkgNameDO> existedPkgs = gateway.getExistedIds("pkg_id");
            List<String> existedPkgIds = existedPkgs.stream().map(RepoPkgNameDO::getPkgId).collect(Collectors.toList());
            Set<String> curRepoNames = repoNames.stream().limit(100).collect(Collectors.toSet());
            Set<String> copy = new HashSet<>(curRepoNames);
            handleRepo(curRepoNames, existedPkgIds, pkgSize);
            copy.removeAll(curRepoNames);
            repoNames.removeAll(copy);
        }
        return validData(start, pkgSize.get());
    }

    /**
     * handle part of repos.
     * @param repoNames set of repos.
     * @param existedPkgIds existedPkgIds.
     * @param pkgSize pkgSize.
     */
    public void handleRepo(Set<String> repoNames, List<String> existedPkgIds, AtomicLong pkgSize) {
        long start = System.currentTimeMillis();
        List<RepoPkgNamePkg> repoList = gitRepoBatchService.getAllBranches(config.getOrg(), repoNames);
        if (repoList == null || repoList.isEmpty()) {
            LOGGER.error("fail to get branches");
            return;
        }

        List<RepoPkgNamePkg> pkgList = gitRepoBatchService.getAllSpecName(repoList);
        if (pkgList == null || pkgList.isEmpty()) {
            LOGGER.error("fail to get spec");
            return;
        }

        List<RepoPkgNamePkg> updatedList = new ArrayList<>();
        for (RepoPkgNamePkg pkg : pkgList) {
            String raw = pkg.getRawSpecContext();
            if (StringUtils.isBlank(raw)) {
                continue;
            }
            String context = Base64Util.decryptToString(pkg.getRawSpecContext());
            String pkgName = getSpecficName(context.lines().collect(Collectors.toList()), pkg.getRepoName());
            pkg.setPkgName(pkgName);
            updatedList.add(pkg);
        }
        gateway.saveAll(updatedList, existedPkgIds);
        long end = System.currentTimeMillis();
        LOGGER.info("size: {}, time: {}, pkgsize: {}", pkgList.size(), end - start, updatedList.size());
        pkgSize.addAndGet(updatedList.size());
    }

    /**
     * valid data.
     * @param start start time.
     * @param size size of pkgs.
     * @return boolean.
     */
    public boolean validData(long start, long size) {
        long row = gateway.getChangedRow(start);
        if (row == size) {
            LOGGER.info("no error, changed size: {}", row);
            return true;
        } else {
            LOGGER.error("error in sotring data, changed size: {}, needed to be changed: {}", row, size);
            return false;
        }
    }

    /**
     * get name from .spec.
     * @param lines list of String from .spec.
     * @param file file name.
     * @return name.
     */
    public String getSpecficName(List<String> lines, String file) {
        String fullName = lines.stream().filter(
            line -> StringUtils.startsWithIgnoreCase(line, "name:")
        ).findFirst().orElse("");
        String name = StringUtils.trimToNull(fullName.substring(5));

        while (true) {
            String[] virtualNames = StringUtils.substringsBetween(name, START_MARKER, END_MARKER);
            name = name.replace(START_MARKER, "");
            name = name.replace(END_MARKER, "");
            if (virtualNames == null || virtualNames.length == 0) {
                break;
            }
            for (String virtualName : virtualNames) {
                String actualName = getActualName(virtualName, lines, file);
                name = name.replace(virtualName, actualName);
            }
        }
        return name;
    }

    /**
     * get actual name from lines.
     * @param virtualName virtual name.
     * @param lines lines.
     * @param repo repo.
     * @return actual name.
     */
    public String getActualName(String virtualName, List<String> lines, String repo) {
        if (virtualName.startsWith("?") && virtualName.contains(":")) {
            return getNameOrDefault(virtualName, lines, repo);
        } else if (virtualName.startsWith("?") && !virtualName.contains(":")) {
            virtualName = virtualName.substring(1);
            return getSimpleName(virtualName, lines, repo);
        } else if (!virtualName.startsWith("?") && !virtualName.contains(":")) {
            return getSimpleName(virtualName, lines, repo);
        } else {
            LOGGER.info("unrecognized macro: {}, repo: {}", virtualName, repo);
            return "";
        }
    }

    /**
     * Name: ?_with_debug:-debug.
     * The value is empty string if the _with_debug macro is not defined.
     * If the _with_debug macro is defined, the value would be binutils-debug.
     * @param virtualName virtualName.
     * @param lines lines.
     * @param repo repo.
     * @return name.
     */
    public String getNameOrDefault(String virtualName, List<String> lines, String repo) {
        virtualName = virtualName.substring(1);
        String[] splits = virtualName.split(":");
        virtualName = splits[0];
        String defaultValue = splits[1];
        String value = getSimpleName(virtualName, lines, repo);
        if (value == null) {
            return "";
        }
        return defaultValue;
    }

    /**
     * get simple name.
     * @param virtualName virtualName.
     * @param lines lines.
     * @param repo repo.
     * @return name.
     */
    public String getSimpleName(String virtualName, List<String> lines, String repo) {
        String actualLine = lines.stream().filter(line ->
                (line.contains("%global") || line.contains("%define")) && line.contains(virtualName)
        ).findFirst().orElse(null);
        if (StringUtils.isBlank(actualLine)) {
            return null;
        }

        String[] splits = actualLine.split(" ");
        String res = Arrays.stream(splits).filter(s -> !StringUtils.isBlank(StringUtils.trimToNull(s))).filter(s ->
            !s.equals(virtualName) && !s.equals("%global") && !s.equals("%define")
        ).findFirst().orElse(null);
        if (res == null) {
            LOGGER.info("the {} micro is not defined, repo: {}", virtualName, repo);
        }
        return res;
    }
}
