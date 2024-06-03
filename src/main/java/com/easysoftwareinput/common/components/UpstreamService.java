package com.easysoftwareinput.common.components;

import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.easysoftwareinput.common.constant.MapConstant;
import com.easysoftwareinput.common.utils.HttpClientUtil;
import com.easysoftwareinput.domain.apppackage.model.AppPackage;
import com.easysoftwareinput.domain.rpmpackage.model.BasePackage;

@Component
public class UpstreamService<T extends BasePackage> {
    /**
     * maintainer api.
     */
    @Value("${api.repoMaintainer}")
    private String repoMaintainerApi;

    /**
     * repo api.
     */
    @Value("${api.repoSig}")
    private String repoSigApi;

    /**
     * repodownload api.
     */
    @Value("${api.repoDownload}")
    private String repoDownloadApi;

    /**
     * repo info api.
     */
    @Value("${api.repoInfo}")
    private String repoInfoApi;

    /**
     * add maintainer.
     * @param pkg pkg
     * @return pkg.
     */
    public T addMaintainerInfo(T pkg) {
        Map<String, String> maintainer = HttpClientUtil.getApiResponseMaintainer(
                String.format(repoMaintainerApi, pkg.getName()));
        pkg.setMaintainerGiteeId(maintainer.get("gitee_id"));
        pkg.setMaintainerId(maintainer.get("id"));
        pkg.setMaintainerEmail(maintainer.get("email"));
        pkg.setMaintainerUpdateAt("");
        return pkg;
    }

    /**
     * add category.
     * @param pkg pkg.
     * @return pkg.
     */
    public T addRepoCategory(T pkg) {
        String resp = HttpClientUtil.getApiResponseData(String.format(repoSigApi, pkg.getName()));
        if (resp != null && MapConstant.CATEGORY_MAP.containsKey(resp)) {
            pkg.setCategory(MapConstant.CATEGORY_MAP.get(resp));
        } else {
            pkg.setCategory(MapConstant.CATEGORY_MAP.get("Other"));
        }
        return pkg;
    }

    /**
     * add download.
     * @param pkg pkg.
     * @return pkg.
     */
    public T addRepoDownload(T pkg) {
        String resp = HttpClientUtil.getApiResponseData(String.format(repoDownloadApi, pkg.getName()));
        pkg.setDownloadCount(resp);
        return pkg;
    }

    /**
     * add pkginfo.
     * @param appPkg pkg.
     * @return pkg.
     */
    public AppPackage addAppPkgInfo(AppPackage appPkg) {
        Map<String, String> info = HttpClientUtil.getApiResponseMap(String.format(repoInfoApi, appPkg.getName(),
                "app_openeuler"));
        appPkg.setOs(info.get("os"));
        appPkg.setAppVer(info.get("latest_version") + "-" + info.get("os_version"));
        appPkg.setArch(info.get("arch"));
        appPkg.setAppSize(info.get("appSize"));
        appPkg.setBinDownloadUrl(info.get("binDownloadUrl"));
        appPkg.setSrcDownloadUrl(info.get("srcDownloadUrl"));
        appPkg.setSrcRepo(info.get("srcRepo"));
        return appPkg;
    }
}
