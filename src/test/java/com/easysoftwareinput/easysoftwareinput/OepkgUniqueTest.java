package com.easysoftwareinput.easysoftwareinput;

import static org.junit.jupiter.api.Assertions.assertFalse;

import java.io.File;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

import com.easysoftwareinput.application.oepkg.OepkgService;
import com.easysoftwareinput.application.oepkg.ThreadService;
import com.easysoftwareinput.common.utils.FileUtil;
import com.easysoftwareinput.domain.oepkg.model.FilePkgEntity;
import com.easysoftwareinput.domain.oepkg.model.OePkg;
import com.easysoftwareinput.domain.oepkg.model.OePkgEntity;
import com.easysoftwareinput.domain.oepkg.model.OsMes;

import lombok.Getter;

@SpringBootTest
public class OepkgUniqueTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(OepkgUniqueTest.class);

    @Value("${oepkg.dir}")
    private String dir;

    @Autowired
    private OepkgService service;

    @Autowired
    private ThreadService threadService;

    /**
     * oepkg官网，同一版本架构的primary.xml既存在于`/repodata`目录下，也存在于`/repodata.old.xxx`目录下。验证：
     *     1. `/repodata.old.xxx`目录的`primary.xml`中的软件包都在·/repodata/primary.xml`中。
     *     2. 如果不在，那么这些软件包也不存在于oepkg官网。
     */
    @Test
    public void test_unique_pkg() {
        OePkgEntity oeEntity = new OePkgEntity();
        threadService.setOePkgEntity(oeEntity);

        List<String> fList = FileUtil.listSubMenus(dir);

        Map<Boolean, List<String>> fMap = fList.stream().collect(Collectors.partitioningBy(
            path -> path.contains("old")
        ));
        List<String> noOldList = fMap.get(false);
        List<String> oldList = fMap.get(true);

        Map<String, List<String>> dMap = oldList.stream().collect(Collectors.groupingBy(this::getTypeEachFile));

        for (String file : noOldList) {
            String type = getTypeEachFile(file);
            List<String> deprList = dMap.get(type);
            analyseDeprecated(file, deprList);
        }
    }

    public void analyseDeprecated(String file, List<String> deprList) {
        if (file == null || deprList == null || deprList.size() == 0) {
            return;
        }

        List<OepkgTemp> noOldPkgs = calculatePkg(List.of(file));
        List<OepkgTemp> oldPkgs = calculatePkg(deprList);

        Set<String> noOldIds = noOldPkgs.stream().map(OepkgTemp::getId).collect(Collectors.toSet());
        for (OepkgTemp pkg: oldPkgs) {
            if (noOldIds.contains(pkg.getId())) {
                continue;
            }

            String url = pkg.getDownloadUrl();
            assertFalse(validUrl(url));
        }
    }

    public boolean validUrl(String oriUrl) {
        String urlFormated = Arrays.stream(oriUrl.split("/")).filter(p -> !p.contains("repodata"))
                .collect(Collectors.joining("/"));
        int state = -1;
        try {
            URL url = new URL(urlFormated);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            state = con.getResponseCode();
        } catch (Exception e) {
            return false;
        }
        if (state == 200) {
            return true;
        } else {
            return false;
        }

    }

    public Map<String, String> getPkgTypeMap(List<String> fList) {
        Map<String, String> res = new HashMap<>();
        for (String file : fList) {
            String type = getTypeEachFile(file);
            res.put(type, file);
        }
        return res;
    }

    public String getTypeEachFile(String file) {
        if (file == null) {
            return null;
        }
        String[] paths = file.split(Pattern.quote(File.separator));
        String fileName = paths[paths.length - 1];

        if (fileName.contains("old")) {
            String[] names = fileName.split("_a_repodata.old");
            return names[0];
        } else {
            String[] names = fileName.split("_a_primary");
            return names[0];
        }
    }

    public List<OepkgTemp> calculatePkg(List<String> files) {
        if (files == null || files.size() == 0) {
            LOGGER.error("no files");
            return Collections.emptyList();
        }

        Map<String, OsMes> osMesMap = service.getPkgOsMesMap(files);
        List<OepkgTemp> pkgIdsAllFiles = new ArrayList<>();
        for (int i = 0; i < files.size(); i++) {
            String file = files.get(i);
            List<OepkgTemp> pkgIdSetEachFile = getPkgIdsEachFile(file, i, osMesMap);
            pkgIdsAllFiles.addAll(pkgIdSetEachFile);
        }
        return pkgIdsAllFiles;
    }

    public List<OepkgTemp> getPkgIdsEachFile(String file, int index, Map<String, OsMes> osMesMap) {
        OsMes osMes = osMesMap.get(file);
        FilePkgEntity fEntity = FilePkgEntity.of(file, index, osMes);
        List<OePkg> oList = threadService.execFile(fEntity);
        return oList.stream().map(pkg -> {
            String id = pkg.getOs() + pkg.getName() + pkg.getVersion() + pkg.getArch();
            String url = pkg.getBinDownloadUrl();
            return new OepkgTemp(id, url);
        }
        ).collect(Collectors.toList());
    }

    @Getter
    static class OepkgTemp {
        private String id;
        private String downloadUrl;
        public OepkgTemp(String id, String downloadUrl) {
            this.id = id;
            this.downloadUrl = downloadUrl;
        }
    }
}
