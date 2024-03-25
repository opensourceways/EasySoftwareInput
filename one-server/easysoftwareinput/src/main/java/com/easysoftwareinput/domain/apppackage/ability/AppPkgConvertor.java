package com.easysoftwareinput.domain.apppackage.ability;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;

import com.easysoftwareinput.application.apppackage.AppHandler;
import com.easysoftwareinput.common.components.UpstreamService;
import com.easysoftwareinput.common.utils.ObjectMapperUtil;
import com.easysoftwareinput.domain.apppackage.model.AppPackage;
import com.power.common.util.StringUtil;

public class AppPkgConvertor {
    @Autowired
    UpstreamService<AppPackage> upstreamService;

    @Autowired
    AppHandler obsService;

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
}
