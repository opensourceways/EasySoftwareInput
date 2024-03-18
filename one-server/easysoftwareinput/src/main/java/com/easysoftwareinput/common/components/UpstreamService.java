package com.easysoftwareinput.common.components;

import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.easysoftwareinput.common.constant.MapConstant;
import com.easysoftwareinput.common.utils.HttpClientUtil;
import com.easysoftwareinput.domain.rpmpackage.model.AppPkg;
import com.easysoftwareinput.domain.rpmpackage.model.BasePackage;

@Component
public class UpstreamService<T extends BasePackage> {
    @Value("${api.repoMaintainer}")
    String repoMaintainerApi;

    @Value("${api.repoSig}")
    String repoSigApi;

    @Value("${api.repoDownload}")
    String repoDownloadApi;

    @Value("${api.repoInfo}")
    String repoInfoApi;

    public T addMaintainerInfo(T pkg) {
        
        Map<String, String> maintainer = HttpClientUtil.getApiResponseMaintainer(String.format(repoMaintainerApi, pkg.getName()));
        pkg.setMaintainerGiteeId(maintainer.get("gitee_id"));
        pkg.setMaintainerId(maintainer.get("id"));
        pkg.setMaintainerEmail(maintainer.get("email"));
        pkg.setMaintainerUpdateAt("");
        return pkg;
    }

    public T addRepoCategory(T pkg) {
        String resp = HttpClientUtil.getApiResponseData(String.format(repoSigApi, pkg.getName()));
        if (resp != null && MapConstant.CATEGORY_MAP.containsKey(resp)) {
            pkg.setCategory(MapConstant.CATEGORY_MAP.get(resp));
        } else {
            pkg.setCategory(MapConstant.CATEGORY_MAP.get("Other"));
        }
        return pkg;
    }

    public T addRepoDownload(T pkg) {
        String resp = HttpClientUtil.getApiResponseData(String.format(repoDownloadApi, pkg.getName()));
        pkg.setDownloadCount(resp);
        return pkg;
    }

    public AppPkg addAppPkgInfo(AppPkg appPkg) {
        Map<String, String> info = HttpClientUtil.getApiResponseMap(String.format(repoInfoApi, appPkg.getName(), "app_openeuler"));
        appPkg.setOs(info.get("os"));
        appPkg.setAppVer(info.get("latest_version") + "-" + info.get("os_version"));
        appPkg.setArch(info.get("arch"));
        appPkg.setAppSize(info.get("appSize"));
        appPkg.setBinDownloadUrl(info.get("binDownloadUrl"));
        appPkg.setSrcDownloadUrl(info.get("srcDownloadUrl"));
        appPkg.setSrcRepo(info.get("srcRepo"));
        return appPkg;
    }

    public static void main(String[] args) {
        String repoSigApi = "https://dsapi.osinfra.cn/query/repo/sig?community=openeuler&repo=%s";
        BasePackage pkg = new BasePackage();
        pkg.setName("opencv");
        String resp = HttpClientUtil.getApiResponseData(String.format(repoSigApi, pkg.getName()));
        if (resp != null && MapConstant.CATEGORY_MAP.containsKey(resp)) {
            pkg.setCategory(MapConstant.CATEGORY_MAP.get(resp));
        } else {
            pkg.setCategory(MapConstant.CATEGORY_MAP.get("Other"));
        }
        System.out.println(pkg);
    }
}
