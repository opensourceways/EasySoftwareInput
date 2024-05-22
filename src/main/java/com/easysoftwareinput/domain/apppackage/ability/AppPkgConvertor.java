package com.easysoftwareinput.domain.apppackage.ability;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
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
    @Autowired
    Environment env;

    @Autowired
    UpstreamService<AppPackage> upstreamService;

    @Autowired
    ObsService obsService;

    @Autowired
    AppVerService verService;

    public AppPackage toEntity(Map<String, String> underLineMap) {
        Map<String, String> camelMap = new HashMap<>();
        for (String underLineKey: underLineMap.keySet()) {
            String camelKey = StringUtil.underlineToCamel(underLineKey);
            camelMap.put(camelKey, underLineMap.get(underLineKey));
        }

        String json = ObjectMapperUtil.writeValueAsString(camelMap);
        AppPackage pkg = ObjectMapperUtil.toObject(AppPackage.class, json);

        pkg = upstreamService.addMaintainerInfo(pkg);
        pkg = upstreamService.addRepoDownload(pkg);
        pkg = upstreamService.addRepoCategory(pkg);
        pkg.setIconUrl(obsService.generateUrl(pkg.getName()));
        
        return pkg;
    }

    private AppPackage initSet(Map<String, Object> map) {
        String json = ObjectMapperUtil.writeValueAsString(map);
        AppPackage pkg = ObjectMapperUtil.jsonToObject(json, AppPackage.class);

        pkg.setImageTags((String) map.get("tags"));
        pkg.setImageUsage((String) map.get("usage"));
        pkg.setInstallation((String) map.get("install"));
        pkg.setType("IMAGE");
        pkg.setIconUrl(obsService.generateUrl(pkg.getName()));

        List<String> simi = (List<String>) map.get("similar_packages");
        pkg.setSimilarPkgs(ObjectMapperUtil.writeValueAsString(simi));

        List<String> depe = (List<String>) map.get("dependency");
        pkg.setDependencyPkgs(ObjectMapperUtil.writeValueAsString(depe));

        return pkg;
    }

    private void setCategory(Map<String, Object> map, AppPackage pkg) {
        String cate = StringUtils.trimToEmpty((String) map.get("category"));
        if (cate.length() == 0) {
            cate = "Other";
        }
        pkg.setCategory(MapConstant.APP_CATEGORY_MAP.get(cate));
    }

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

    private String convertArch(String originArch) {
        if ("amd64".equals(originArch)) {
            return "x86_64";
        } else if ("arm64".equals(originArch)) {
            return "aarch64";
        } else {
            return originArch;
        }
    }

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

    private Map<String, Object> getFromMonitor(String name) {
        Map<String, JsonNode> nodeMap = HttpClientUtil.getMonitor(name, env.getProperty("appver.monurl"));
        if (nodeMap.size() == 0) {
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

    private AppPackage createNewAppPkg(AppPackage originPkg, String arch, String ver) {
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

    private List<AppPackage> splitByMonMap(AppPackage originPkg, Map<String, Object> monMap) {
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
                appList.add(createNewAppPkg(originPkg, arch, ver));
            }
        }
        return appList;
    }

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
