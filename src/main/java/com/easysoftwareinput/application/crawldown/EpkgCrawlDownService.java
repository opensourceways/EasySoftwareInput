package com.easysoftwareinput.application.crawldown;

import java.io.File;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.easysoftwareinput.common.utils.FileUtil;
import com.easysoftwareinput.domain.crawldown.model.EpkgCrawlConfig;
import com.easysoftwareinput.domain.crawldown.model.RpmCrawlEntity;
import com.easysoftwareinput.domain.epkgpackage.model.EpkgConfig;

@Component
public class EpkgCrawlDownService {
    /**
     * config.
     */
    @Autowired
    private EpkgConfig config;

    /**
     * crawl config.
     */
    @Autowired
    private EpkgCrawlConfig crawlConfig;

    /**
     * rpm crawl service.
     */
    @Autowired
    private RpmCrawlDownService crawlService;

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

        crawlService.exec(dir, list);
        reNameFile();
        return;
    }

    /**
     * re name file.
     * @return boolean.
     */
    public boolean reNameFile() {
        List<String> files = FileUtil.listSubMenus(config.getDir());
        String file = files.get(0);
        String targetFileName = config.getOsName() + "-" + config.getOsVer() + "_a_primary.xml";
        File source = new File(file);
        File target = new File(config.getDir() + File.separator + targetFileName);
        return source.renameTo(target);

    }
}
