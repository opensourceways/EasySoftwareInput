package com.easysoftwareinput.application.apppackage;

import java.io.File;
import java.io.FilenameFilter;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jgit.api.Git;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import com.easysoftwareinput.common.utils.YamlUtil;
import com.easysoftwareinput.domain.apppackage.ability.AppPkgConvertor;
import com.easysoftwareinput.domain.apppackage.model.AppPackage;
import com.easysoftwareinput.infrastructure.apppkg.AppGatewayImpl;

@Service
public class AppPackageService {
    /**
     * logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(AppPackageService.class);

    /**
     * env.
     */
    @Autowired
    private Environment env;

    /**
     * obs.
     */
    @Autowired
    private ObsService obsService;

    /**
     * converter.
     */
    @Autowired
    private AppPkgConvertor converter;

    /**
     * appgatewayimpl.
     */
    @Autowired
    private AppGatewayImpl appGateway;

    /**
     * git pull.
     * @param repoPath repoPath.
     */
    public void gitPull(String repoPath) {
        try {
            Git git = Git.open(new File(repoPath));
            git.pull().call();
            git.close();
        } catch (Exception e) {
            LOGGER.error("git pull exception", e);
        }
    }

    /**
     * get sub menus of repoPath.
     * @param repoPath repoPath
     * @return sub menus.
     */
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

    /**
     * whether the file exists.
     * @param infoPath infoPath.
     * @return whether the file exists.
     */
    private boolean existInfoFile(String infoPath) {
        File file = new File(infoPath);
        if (file.exists() && file.isFile()) {
            return true;
        }
        return false;
    }

    /**
     * get map from file.
     * @param pkg file.
     * @return a map.
     */
    private Map<String, Object> getInfoMap(File pkg) {
        String infoPath = Paths.get(pkg.getAbsolutePath(), new String[]{"doc", "image-info.yml"}).toString();

        if (!existInfoFile(infoPath)) {
            return Collections.emptyMap();
        }

        Map<String, Object> yamlMap = YamlUtil.parseYaml(infoPath);

        String name = (String) yamlMap.get("name");
        if (StringUtils.isBlank(name)) {
            LOGGER.info("no yaml name, file: {}", pkg.toString());
            return Collections.emptyMap();
        }
        return yamlMap;
    }

    /**
     * get path of pic.
     * @param pkg
     * @return return "" if the path of pic does not exist.
     */
    private String getPicPath(File pkg) {
        File picMenu = new File(Paths.get(pkg.getAbsolutePath(), new String[]{"doc", "picture"}).toString());
        if (!picMenu.exists() || !picMenu.isDirectory()) {
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

    /**
     * save the data.
     * @param map data.
     */
    private void postInfo(Map<String, Object> map) {
        List<AppPackage> pkgList = converter.mapToPkgList(map);
        if (pkgList.size() == 0) {
            return;
        }
        appGateway.saveAll(pkgList);
    }

    /**
     * handle each app pkg.
     * @param repoPath repoPath.
     */
    private void handleEachApp(String repoPath) {
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

            postInfo(map);
        }
    }

    /**
     * run the program.
     */
    public void run() {
        String repoPath = env.getProperty("app.path");
        gitPull(repoPath);
        handleEachApp(repoPath);
        LOGGER.info("finish-update-application");
    }

    /**
     * filter the image by ext.
     */
    static class ImageFilenameFilter implements FilenameFilter {
        @Override
        public boolean accept(File dir, String name) {
            return name.toLowerCase().endsWith(".jpg")
                    || name.toLowerCase().endsWith(".jpeg")
                    || name.toLowerCase().endsWith(".png")
                    || name.toLowerCase().endsWith(".gif")
                    || name.toLowerCase().endsWith(".bmp");
        }
    }
}
