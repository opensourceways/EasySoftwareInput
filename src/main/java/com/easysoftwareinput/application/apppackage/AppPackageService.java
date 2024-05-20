package com.easysoftwareinput.application.apppackage;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jgit.api.Git;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import com.easysoftwareinput.common.utils.HttpClientUtil;
import com.easysoftwareinput.common.utils.ObjectMapperUtil;
import com.easysoftwareinput.common.utils.YamlUtil;
import com.easysoftwareinput.domain.apppackage.ability.AppPkgConvertor;
import com.easysoftwareinput.domain.apppackage.model.AppPackage;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Service
public class AppPackageService {
    private static final Logger logger = LoggerFactory.getLogger(AppPackageService.class);

    @Autowired
    Environment env;

    @Autowired
    ObsService obsService;

    @Autowired
    AppPkgConvertor converter;

    public void gitPull(String repoPath) {
        try {
            Git git = Git.open(new File(repoPath));
            git.pull().call();
            git.close();
        } catch (Exception e) {
            logger.error("git pull exception", e);
        }
    }

    private List<File> getSubMenus(String repoPath) {
        File folder = new File(repoPath);
        if (!folder.exists() || (!folder.isDirectory())) {
            return Collections.emptyList();
        }

        List<File> pkgList = new ArrayList<>();
        File[] pkgs = folder.listFiles();
        for (File pkg : pkgs) {
            pkgList.add(pkg);
        }
        return pkgList;
    }

    private boolean existInfoFile(String infoPath) {
        File file = new File(infoPath);
        if (file.exists() && file.isFile()) {
            return true;
        }
        return false;
    }

    private Map<String, Object> getInfoMap(File pkg) {
        String infoPath = Paths.get(pkg.getAbsolutePath(), new String[]{"doc", "image-info.yml"}).toString();

        if (! existInfoFile(infoPath)) {
            return Collections.emptyMap();
        }

        Map<String, Object> yamlMap = YamlUtil.parseYaml(infoPath);

        String name = (String) yamlMap.get("name");
        if (StringUtils.isBlank(name)) {
            logger.info("no yaml name, file: {}", pkg.toString());
            return Collections.emptyMap();
        }
        return yamlMap;
    }

    private String getPicPath(File pkg) {
        File picMenu = new File(Paths.get(pkg.getAbsolutePath(), new String[]{"doc", "picture"}).toString());
        if (! picMenu.exists() || ! picMenu.isDirectory()) {
            return "";
        }

        File[] picFiles = picMenu.listFiles(new ImageFilenameFilter());
        if (picFiles == null || picFiles.length == 0) {
            return "";
        }
        return picFiles[0].getAbsolutePath();
    }

    private void pushPic(File pkg, Object nameO) {
        String name = (String) nameO;
        String picPath = getPicPath(pkg);
        obsService.putData(name, picPath);
    }

    private void postInfo(Map<String, Object> map, String posturl) {
        List<AppPackage> pkgList = converter.mapToPkgList(map);
        for (AppPackage pkg : pkgList) {
            String body = ObjectMapperUtil.writeValueAsString(pkg);
            String res = HttpClientUtil.postApp(posturl, body);
            logger.info("finish-post,name: {}, res: {}, body: {}, posturl: {}", pkg.getName(), res, body, posturl);
        }
    }

    private void handleEachApp(String repoPath, String posturl) {
        List<File> pkgs = getSubMenus(repoPath);
        for (File pkg : pkgs) {
            Map<String, Object> map = getInfoMap(pkg);
            if (map.size() == 0) {
                continue;
            }

            // 如果show-on-appstore设为false，则跳过当前容器镜像
            Object show = map.get("show-on-appstore");
            if (show != null && "false".equals(show)) {
                continue;
            }

            pushPic(pkg, map.get("name"));

            postInfo(map, posturl);
        }
       
    }

    public void run() {
        String repoPath = env.getProperty("app.path");
        String posturl = env.getProperty("app.posturl");
        gitPull(repoPath);
        handleEachApp(repoPath, posturl);
        logger.info("finish-update-application");
        System.exit(0);
    }

    static class ImageFilenameFilter implements FilenameFilter {
        @Override
        public boolean accept(File dir, String name) {
            return name.toLowerCase().endsWith(".jpg") ||
                   name.toLowerCase().endsWith(".jpeg") ||
                   name.toLowerCase().endsWith(".png") ||
                   name.toLowerCase().endsWith(".gif") ||
                   name.toLowerCase().endsWith(".bmp");
        }
    }
}
