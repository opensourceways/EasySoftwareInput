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

package com.easysoftwareinput.domain.apppackage.ability;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import com.easysoftwareinput.application.apppackage.ObsService;
import com.easysoftwareinput.application.appver.AppVerService;
import com.easysoftwareinput.common.components.UpstreamService;
import com.easysoftwareinput.common.constant.MapConstant;
import com.easysoftwareinput.common.utils.HttpClientUtil;
import com.easysoftwareinput.common.utils.ObjectMapperUtil;
import com.easysoftwareinput.common.utils.UUidUtil;
import com.easysoftwareinput.domain.apppackage.model.AppPackage;
import com.fasterxml.jackson.databind.JsonNode;
import com.power.common.util.StringUtil;

@Component
public class AppPkgConvertor {
    /**
     * logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(AppPkgConvertor.class);
    /**
     * env.
     */
    @Autowired
    private Environment env;

    /**
     * upstream service.
     */
    @Autowired
    private UpstreamService<AppPackage> upstreamService;

    /**
     * obsservice.
     */
    @Autowired
    private ObsService obsService;

    /**
     * appver service.
     */
    @Autowired
    private AppVerService verService;

    /**
     * convert map to AppPackage object.
     * @param underLineMap map.
     * @return AppPackage object.
     */
    public AppPackage toEntity(Map<String, String> underLineMap) {
        Map<String, String> camelMap = new HashMap<>();
        for (Map.Entry<String, String> entry : underLineMap.entrySet()) {
            String camelKey = StringUtil.underlineToCamel(entry.getKey());
            camelMap.put(camelKey, entry.getValue());
        }

        String json = ObjectMapperUtil.writeValueAsString(camelMap);
        AppPackage pkg = ObjectMapperUtil.toObject(AppPackage.class, json);

        pkg = upstreamService.addMaintainerInfo(pkg);
        pkg = upstreamService.addRepoDownload(pkg);
        pkg = upstreamService.addRepoCategory(pkg);
        pkg.setIconUrl(obsService.generateUrl(pkg.getName()));
        return pkg;
    }

    /**
     * init set of AppPackage object.
     * @param map map.
     * @return AppPackage object.
     */
    private AppPackage initSet(Map<String, Object> map) {
        String json = ObjectMapperUtil.writeValueAsString(map);
        AppPackage pkg = ObjectMapperUtil.jsonToObject(json, AppPackage.class);

        pkg.setImageTags((String) map.get("tags"));
        pkg.setImageUsage((String) map.get("usage"));
        pkg.setInstallation((String) map.get("install"));
        pkg.setType("IMAGE");
        pkg.setIconUrl(obsService.generateUrl(pkg.getName()));
        pkg.setId(UUidUtil.getUUID32());
        pkg.setUpdateAt(new Timestamp(System.currentTimeMillis()));

        List<String> simi = (List<String>) map.get("similar_packages");
        pkg.setSimilarPkgs(ObjectMapperUtil.writeValueAsString(simi));

        List<String> depe = (List<String>) map.get("dependency");
        pkg.setDependencyPkgs(ObjectMapperUtil.writeValueAsString(depe));

        return pkg;
    }

    /**
     * set cateogry of AppPackage object.
     * @param map map.
     * @param pkg AppPackage object.
     */
    private void setCategory(Map<String, Object> map, AppPackage pkg) {
        String cate = StringUtils.trimToEmpty((String) map.get("category"));
        if (cate.length() == 0) {
            cate = "Other";
        }
        pkg.setCategory(MapConstant.APP_CATEGORY_MAP.get(cate));
    }

    /**
     * set maintainer of AppPackage object.
     * @param map map.
     * @param pkg AppPackage object.
     */
    private void setMaintainer(Map<String, Object> map, AppPackage pkg) {
        pkg.setMaintainerEmail(MapConstant.MAINTAINER.get("email"));
        pkg.setMaintainerGiteeId(MapConstant.MAINTAINER.get("gitee_id"));
        pkg.setMaintainerId(MapConstant.MAINTAINER.get("id"));


        String url = env.getProperty("maintainer.url");

        String res = HttpClientUtil.getHttpClient(url, null, null, null);
        if (res != null) {
            JsonNode info = ObjectMapperUtil.toJsonNode(res);
            if (info.get("code").asInt() == 200 && !info.get("data").isNull()) {
                JsonNode infoData = info.get("data");
                Map<String, Object> infoMap = ObjectMapperUtil.jsonToMap(infoData);
                pkg.setMaintainerGiteeId((String) infoMap.get("gitee_id"));
                pkg.setMaintainerId((String) infoMap.get("gitee_id"));
                pkg.setMaintainerEmail((String) infoMap.get("email"));
            }
        }
    }

    /**
     * reset the arch.
     * @param originArch the orgin arch.
     * @return the target arch.
     */
    private String convertArch(String originArch) {
        if ("amd64".equals(originArch)) {
            return "x86_64";
        } else if ("arm64".equals(originArch)) {
            return "aarch64";
        } else {
            return originArch;
        }
    }

    /**
     * assemble monMap by appOp.
     * @param monMap monmap.
     * @param appOp AppPackage object.
     */
    private void setAppOp(Map<String, Object> monMap, JsonNode appOp) {
        List<String> rawVers = new ArrayList<>();
        for (JsonNode v : appOp.get("raw_versions")) {
            rawVers.add(v.asText());
        }
        monMap.put("rawVer", rawVers);

        String[] arches = appOp.get("architectures").asText().split(",");
        List<String> archList = new ArrayList<>();
        for (String arch : arches) {
            archList.add(convertArch(arch));
        }
        monMap.put("arch", archList);
    }

    /**
     * get map from monitor service.
     * @param name name.
     * @return map.
     */
    private Map<String, Object> getFromMonitor(String name) {
        String monUrl = env.getProperty("appver.monurl");
        if (monUrl == null) {
            LOGGER.error("no env: appver.monurl");
            return Collections.emptyMap();
        }
        Map<String, JsonNode> nodeMap = HttpClientUtil.getMonitor(name, monUrl);
        if (nodeMap == null || nodeMap.size() == 0) {
            LOGGER.info("no res from monitor service");
            return Collections.emptyMap();
        }

        Map<String, Object> monMap = new HashMap<>();

        JsonNode appUp = nodeMap.get("app_up");
        if (appUp != null) {
            monMap.put("appVer", appUp.get("version").asText());
        }

        JsonNode appOp = nodeMap.get("app_openeuler");
        if (appOp != null) {
            setAppOp(monMap, appOp);
        }
        return monMap;
    }

    /**
     * create AppPackage object.
     * @param originPkg origin pkg.
     * @param arch arch.
     * @param ver ver.
     * @return new AppPackage object.
     */
    private AppPackage createNewAppPkg(AppPackage originPkg, String arch, String ver) {
        if (StringUtils.isBlank(arch) && StringUtils.isBlank(ver)) {
            return null;
        }
        AppPackage pkg = new AppPackage();
        BeanUtils.copyProperties(originPkg, pkg);
        Map<String, String> osAndVer = verService.splitVer(ver);
        pkg.setArch(arch);
        pkg.setAppVer(ver);
        pkg.setOsSupport(osAndVer.get("osVer"));
        pkg.setOs(osAndVer.get("osVer"));
        pkg.setPkgId(pkg.getName() + pkg.getOs() + pkg.getArch());
        pkg.setId(UUidUtil.getUUID32());
        return pkg;
    }

    /**
     * create AppPackage object by arches and vers.
     * @param originPkg origin pkg.
     * @param monMap monmap.
     * @return list of AppPackage object.
     */
    private List<AppPackage> splitByMonMap(AppPackage originPkg, Map<String, Object> monMap) {
        if (monMap.get("arch") == null || monMap.get("rawVer") == null) {
            return List.of(originPkg);
        }
    
        List<String> arches = (List<String>) monMap.get("arch");
        if (arches == null) {
            arches = List.of("");
        }

        List<String> vers = (List<String>) monMap.get("rawVer");
        if (vers == null) {
            vers = List.of("");
        }

        List<AppPackage> appList = new ArrayList<>();

        for (String arch : arches) {
            for (String ver : vers) {
                AppPackage a = createNewAppPkg(originPkg, arch, ver);
                if (a != null) {
                    appList.add(a);
                }
            }
        }
        return appList;
    }

    /**
     * tell the latest version of each pkg.
     * @param appList list pf pkg.
     * @return the latest version.
     */
    private String getLatestOsPerName(List<AppPackage> appList) {
        if (appList.size() == 0) {
            return "";
        }

        String latest = appList.get(0).getOsSupport();
        for (AppPackage pkg : appList) {
            String curOsSupport = pkg.getOsSupport();
            if (latest.compareTo(curOsSupport) < 0) {
                latest = curOsSupport;
            }
        }
        return latest;
    }

    /**
     * set the latestossupport of pkg.
     * @param appList list of AppPackage object.
     */
    private void setLatestOsSupport(List<AppPackage> appList) {
        if (appList.size() == 0) {
            return;
        }

        String latestSupp = getLatestOsPerName(appList);
        for (AppPackage pkg : appList) {
            if (latestSupp.equals(pkg.getOsSupport())) {
                pkg.setLatestOsSupport("true");
            } else {
                pkg.setLatestOsSupport("false");
            }
        }
    }

    /**
     * create list of AppPackage object by map.
     * @param map map.
     * @return list of AppPackage object.
     */
    public List<AppPackage> mapToPkgList(Map<String, Object> map) {
        AppPackage pkg = initSet(map);
        setCategory(map, pkg);
        setMaintainer(map, pkg);
        Map<String, Object> monMap = getFromMonitor(pkg.getName());
        // 如果监控服务里容器镜像的数据，则不保存该容器镜像
        if (monMap.size() == 0) {
            return Collections.emptyList();
        }
        List<AppPackage> appList = splitByMonMap(pkg, monMap);
        setLatestOsSupport(appList);
        return appList;
    }
}
