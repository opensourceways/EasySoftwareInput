package com.easysoftwareinput.domain.apppkg.ability;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;

import com.easysoftwareinput.common.components.ObsService;
import com.easysoftwareinput.common.components.UpstreamService;
import com.easysoftwareinput.common.utils.ObjectMapperUtil;
import com.easysoftwareinput.domain.rpmpackage.model.AppPkg;
import com.power.common.util.StringUtil;

public class AppPkgConvertor {
    @Autowired
    UpstreamService<AppPkg> upstreamService;

    @Autowired
    ObsService obsService;

    public AppPkg toEntity(Map<String, String> underLineMap) {
        Map<String, String> camelMap = new HashMap<>();
        for (String underLineKey: underLineMap.keySet()) {
            String camelKey = StringUtil.underlineToCamel(underLineKey);
            camelMap.put(camelKey, underLineMap.get(underLineKey));
        }

        String json = ObjectMapperUtil.writeValueAsString(camelMap);
        AppPkg pkg = ObjectMapperUtil.toObject(AppPkg.class, json);

        pkg = upstreamService.addMaintainerInfo(pkg);
        pkg = upstreamService.addRepoDownload(pkg);
        pkg = upstreamService.addRepoCategory(pkg);
        pkg.setIconUrl(obsService.generateUrl(pkg.getName()));
        
        return pkg;
    }
}
