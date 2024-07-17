package com.easysoftwareinput.application.crawldown;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.easysoftwareinput.common.utils.FileUtil;
import com.easysoftwareinput.domain.crawldown.model.OepkgCrawlConfig;
import com.easysoftwareinput.domain.crawldown.model.RpmCrawlEntity;
import com.easysoftwareinput.domain.oepkg.model.OepkgConfig;

@Component
public class OepkgCrawlDownService {
    /**
     * crawl config.
     */
    @Autowired
    private OepkgCrawlConfig crawlConfig;

    /**
     * config.
     */
    @Autowired
    private OepkgConfig config;

    /**
     * rpm crawl service.
     */
    @Autowired
    private RpmCrawlDownService rpmCrawlService;

    /**
     * run the program.
     */
    public void run() {
        List<RpmCrawlEntity> list = crawlConfig.getList();
        if (list == null || list.isEmpty()) {
            return;
        }

        String dir = config.getDir();
        if (StringUtils.isBlank(dir)) {
            return;
        }

        rpmCrawlService.exec(dir, list);
        reNameDir(dir);
        return;
    }

    /**
     * rename files in dir.
     * @param dir dir.
     * @return boolean.
     */
    public boolean reNameDir(String dir) {
        List<String> files = FileUtil.listSubMenus(dir);
        return files.stream().map(file -> reNameFile(file, dir)).allMatch(b -> Boolean.TRUE.equals(b));
    }

    /**
     * rename file.
     * @param file file.
     * @param dir dir.
     * @return boolean.
     */
    public boolean reNameFile(String file, String dir) {
        String[] splits = FileUtil.parseFileName(file);
        if (splits == null || splits.length < 3) {
            return false;
        }

        String[] filterSplits = Arrays.copyOfRange(splits, 2, splits.length);
        String desFile = StringUtils.join(filterSplits, "_a_");

        File source = new File(file);
        File target = new File(dir + File.separator + desFile);
        return source.renameTo(target);
    }
}
