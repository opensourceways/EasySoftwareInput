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

package com.easysoftwareinput.application.appver;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import com.easysoftwareinput.common.utils.HttpClientUtil;
import com.easysoftwareinput.common.utils.ObjectMapperUtil;
import com.easysoftwareinput.common.utils.YamlUtil;
import com.easysoftwareinput.domain.appver.AppVerConfig;
import com.easysoftwareinput.domain.appver.AppVersion;
import com.easysoftwareinput.domain.appver.FedoraMonitorVO;
import com.easysoftwareinput.infrastructure.appver.AppVerGatewayImpl;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

import lombok.Getter;
import lombok.Setter;

@Component
public class RpmVerService {
    /**
     * logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(RpmVerService.class);

    /**
     * env.
     */
    @Autowired
    private Environment env;

    /**
     * config.
     */
    @Autowired
    private AppVerConfig config;

    /**
     * appverservice.
     */
    @Autowired
    private AppVerService appService;

    /**
     * gateway.
     */
    @Autowired
    private AppVerGatewayImpl gateway;

    /**
     * fedora monitor service.
     */
    @Autowired
    private FedoraMonitorService fedoraMonitorService;

    /**
     * get alias.
     * @return map of alias.
     */
    public Map<String, List<String>> getRpmAlias() {
        Map<String, Object> rawAliasMap = YamlUtil.parseYaml(config.getRpmAlias());
        Map<String, List<String>> res = new HashMap<>();
        for (Map.Entry<String, Object> entry : rawAliasMap.entrySet()) {
            String value = String.valueOf(entry.getValue());
            String[] values = value.split("\\|");
            List<String> alias = Arrays.stream(values).map(StringUtils::trimToEmpty)
                    .filter(StringUtils::isNotBlank).collect(Collectors.toList());
            res.put(entry.getKey(), alias);
        }
        return res;
    }

    /**
     * run the program.
     */
    public void run() {
        List<String> pkgList = readFileByLine(config.getRpmTxt());
        Set<String> pkgs = validPkgs(pkgList);

        Map<String, List<String>> aliasMap = getRpmAlias();

        List<AppVersion> vList = new ArrayList<>();
        for (String name : pkgs) {
            AppVersion v = handleEachPkg(name, aliasMap);
            vList.add(v);
        }

        vList.addAll(addFedoraMonitorPkg());

        gateway.saveAll(appService.filter(vList));


        LOGGER.info("finish-rpm-version");
    }

    /**
     * add fedora monitor pkg.
     * @return list of AppVersion.
     */
    public List<AppVersion> addFedoraMonitorPkg() {
        List<String> pkgList = readFileByLine(config.getFedoraMonitorTxt());
        Set<String> pkgs = validPkgs(pkgList);
        List<AppVersion> verList = new ArrayList<>();
        for (String pkgName : pkgs) {
            FedoraMonitorVO upstreamPkg = fedoraMonitorService.getUpstream(pkgName);
            if (upstreamPkg == null) {
                continue;
            }
            AppVersion pkg = new AppVersion();
            pkg.setUpstreamVersion(upstreamPkg.getFirstStableVersion());
            pkg.setName(pkgName);
            pkg.setType("rpm");

            EulerRpmVerOs euler = getEulerVersion(pkgName, config.getRpmEuler());
            if (euler != null) {
                pkg.setEulerOsVersion(euler.getOs());
                setEulerVersion(pkg, euler.getVer());
                appService.setStatus(pkg);
            }
            verList.add(pkg);
        }
        return verList;

    }


    /**
     * get euler version from list.
     * @param name pkg name.
     * @param aliasMap alias map.
     * @return euler version.
     */
    public Map<String, String> getEulerVersionFromList(String name, Map<String, List<String>> aliasMap) {
        List<EulerRpmVerOs> list = new ArrayList<>();
        list.add(getEulerVersion(name, config.getRpmEuler()));

        List<String> nameList = aliasMap.get(name);
        nameList = nameList == null ? Collections.emptyList() : nameList;
        for (String pkgName : nameList) {
            EulerRpmVerOs euler = getEulerVersion(pkgName, config.getRpmEuler());
            list.add(euler);
        }
        List<EulerRpmVerOs> listPkg = list.stream().filter(pkg -> {
            return !Objects.isNull(pkg) && !StringUtils.isBlank(pkg.getOs()) && !StringUtils.isBlank(pkg.getVer());
        }).collect(Collectors.toList());

        return pickNewest(listPkg);
    }

    /**
     * pick newest EulerRpmVerOs from list.
     * @param list list of EulerRpmVerOs.
     * @return map.
     */
    public Map<String, String> pickNewest(List<EulerRpmVerOs> list) {
        if (list == null || list.isEmpty()) {
            return Collections.emptyMap();
        }

        list = list.stream().sorted(Comparator.comparing(EulerRpmVerOs::getOs).thenComparing(EulerRpmVerOs::getVer))
                .collect(Collectors.toList());
        EulerRpmVerOs winner = list.get(list.size() - 1);
        return Map.of("ver", winner.getVer(), "os", winner.getOs());
    }

    /**
     * Generate AppVersion for each pkg.
     * @param name pkg name.
     * @param aliasMap alias map.
     * @return AppVersion.
     */
    private AppVersion handleEachPkg(String name, Map<String, List<String>> aliasMap) {
        Map<String, JsonNode> items = appService.getItems(name, config.getMonurl());

        Map<String, String> euler = getEulerVersionFromList(name, aliasMap);

        AppVersion v = new AppVersion();
        v.setName(name);
        v.setType("rpm");
        fillWithEuler(v, euler);
        fillWithUpCi(v, items);
        return v;
    }

    /**
     * fill AppVersion with upstream.
     * @param v AppVersion pkg.
     * @param items upstream message.
     */
    public void fillWithUpCi(AppVersion v, Map<String, JsonNode> items) {
        appService.fillWithUp(v, items.get("app_up"));
        appService.fillWithCi(v, items.get("app_ci_openeuler"));
        appService.setStatus(v);
    }

    /**
     * fill AppVersion with euler.
     * @param v AppVersion pkg.
     * @param euler euler message.
     */
    public void fillWithEuler(AppVersion v, Map<String, String> euler) {
        v.setEulerOsVersion(euler.get("os"));
        v.setOpeneulerVersion(euler.get("ver"));
        setEulerVersion(v, euler.get("ver"));
        v.setEulerHomepage(""); // none
    }

    /**
     * set OpeneulerVersion of pkg.
     * @param v pkg.
     * @param ver openEulerverison.
     */
    public void setEulerVersion(AppVersion v, String ver) {
        if (StringUtils.isBlank(ver)) {
            return;
        }

        String[] splits = ver.split("-");
        if (splits == null || splits.length == 0) {
            return;
        }

        v.setOpeneulerVersion(splits[0]);
    }

    /**
     * get euler version of each pkg.
     * @param name pgk name.
     * @param monUrl monitor url.
     * @return euler version.
     */
    public EulerRpmVerOs getEulerVersion(String name, String monUrl) {
        if (StringUtils.isBlank(monUrl) || StringUtils.isBlank(name)) {
            return null;
        }
        name = name.replaceAll(" ", "%20");
        String url = String.format(monUrl, name);
        try {
            String response = HttpClientUtil.getHttpClient(url, null, null, null);
            JsonNode info = ObjectMapperUtil.toJsonNode(response);
            JsonNode data = info.get("data");
            ArrayNode list = (ArrayNode) data.get("list");
            JsonNode l = list.get(0).get(0);
            String ver = l.get("newestVersion").asText();
            String os = l.get("os").asText();

            EulerRpmVerOs eulerRpmVerOs = new EulerRpmVerOs();
            eulerRpmVerOs.setOs(StringUtils.trimToEmpty(os));
            eulerRpmVerOs.setVer(StringUtils.trimToEmpty(ver));
            return eulerRpmVerOs;
        } catch (Exception e) {
            LOGGER.error("fail-to-get-euler-verison, url: {}", url);
            return null;
        }
    }

    /**
     * filter the pkgs which is empty.
     * @param pkgs origin pkgs.
     * @return filtered pkgs.
     */
    public Set<String> validPkgs(List<String> pkgs) {
        Set<String> res = new HashSet<>(pkgs);
        return res.stream().filter(p -> !StringUtils.isBlank(p)).collect(Collectors.toSet());
    }

    /**
     * read data from file by line.
     * @param fileName filename.
     * @return list of string.
     */
    public List<String> readFileByLine(String fileName) {
        List<String> res = new ArrayList<>();
        try (BufferedReader r = new BufferedReader(new FileReader(fileName, StandardCharsets.UTF_8))) {
            String line;
            while ((line = r.readLine()) != null) {
                res.add(StringUtils.trimToEmpty(line));
            }
        } catch (IOException e) {
            LOGGER.error("error-reading-file: {}", fileName);
        }
        return res;
    }

    @Getter
    @Setter
    public static class EulerRpmVerOs {
        /**
         * os.
         */
        private String os;

        /**
         * ver.
         */
        private String ver;
    }
}
