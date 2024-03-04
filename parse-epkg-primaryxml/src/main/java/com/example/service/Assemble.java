package com.example.service;

import java.lang.reflect.Field;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.example.entity.po.EPKGPackage;
import com.example.mapper.EPKGPackageMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.power.common.util.StringUtil;

@Component
public class Assemble {
    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    EPKGPackageMapper epkgPackageMapper;

    @Autowired
    ExecuteService executeService;

    public EPKGPackage assembleEpkgpkg(Map<String, String> underLineMap, List<String> srcFiles) {

        Map<String, String> camelMap = new HashMap<>();
        for (String underLineKey: underLineMap.keySet()) {
            String camelKey = StringUtil.underlineToCamel(underLineKey);
            camelMap.put(camelKey, underLineMap.get(underLineKey));
        }

        EPKGPackage pkg = null;
        try {
            String json = objectMapper.writeValueAsString(camelMap);
            pkg = objectMapper.readValue(json, EPKGPackage.class);
        } catch (Exception e) {
        }

        pkg.setBinDownloadUrl(camelMap.get("baseUrl") + camelMap.get("locationHref"));
        // BeanUtils.copyProperties(mes, pkg);
        // TODO  我要获取changlog
        pkg.setChangeLog("");
        
        Timestamp currentTime = new Timestamp(System.currentTimeMillis());
        String id = UUID.randomUUID().toString().replace("-", "");
        pkg.setUpdateAt(currentTime);
        pkg.setCreateAt(currentTime);
        // 直接往数据库存，需要设置id，但是调用easysoftware接口，不能设置id
        // pkg.setId(id);
        
        pkg.setMaintanierId("");
        pkg.setMaintianerEmail("");
        pkg.setMaintainerGiteeId("");
        pkg.setMaintainerUpdateAt("");
        pkg.setMaintainerStatus("");

        pkg.setOs(camelMap.get("osName") + "-" + camelMap.get("osVer"));
        pkg.setOsSupport("");
        pkg.setRepo("openeuler official repo");
        pkg.setRepoType(camelMap.get("osType"));
        pkg.setEpkgCategory("Unspecified");
        
        Long cTime = Long.parseLong(camelMap.get("timeFile"));
        String fTime = new SimpleDateFormat("yyyy-MM-dd").format(new Date(cTime * 1000));
        pkg.setEpkgUpdateAt(fTime);
        
        double sSize = (double) Integer.parseInt(camelMap.get("sizePackage")) / 1024 / 1024;
        String fSize = String.format("%.2fMB", sSize);
        pkg.setEpkgSize(fSize);

        pkg.setSecurity("");
        pkg.setSimilarPkgs("");

        String desired = "";
        for (String srcUrl : srcFiles) {
            String[] splits = srcUrl.split("/");
            String name = splits[splits.length - 1];
            if (name.equals(camelMap.get("rpmSourcerpm"))) {
                desired = srcUrl;
                break;
            }
        }
        pkg.setSrcDownloadUrl(desired);

        pkg.setSrcRepo(camelMap.get("url"));

        String formatS = String.format("1. 添加源\n`dnf config-manager --add-repo %s `\n2. 更新源索引\n" +
                "`dnf clean all && dnf makecache`\n3. 安装 %s 软件包\n`dnf install %s`", camelMap.get("baseUrl"),
                pkg.getName(), pkg.getName());
        pkg.setInstallation(formatS);

        pkg.setUpStream("");
        pkg.setVersion(camelMap.get("versionVer") + "-" + camelMap.get("versionRel"));
    
        return pkg;
    }
}
