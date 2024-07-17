package com.easysoftwareinput.application.crawldown;

import java.io.File;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.easysoftwareinput.common.utils.FileUtil;
import com.easysoftwareinput.domain.crawldown.model.RpmCrawlConfig;
import com.easysoftwareinput.domain.crawldown.model.RpmCrawlEntity;
import com.easysoftwareinput.domain.rpmpackage.model.RpmConfig;

import lombok.Getter;
import lombok.Setter;

@Component
@Getter
@Setter
public class RpmCrawlDownService {
    /**
     * crawl config.
     */
    @Autowired
    private RpmCrawlConfig config;

    /**
     * crawl service.
     */
    @Autowired
    private CrawlPkgService crawlService;

    /**
     * download service.
     */
    @Autowired
    private DownloadPkgService downService;

    /**
     * rpm config.
     */
    @Autowired
    private RpmConfig rpmConfig;

    /**
     * decompress service.
     */
    @Autowired
    private DecompressService decompService;

    /**
     * run the program.
     */
    public void run() {
        List<RpmCrawlEntity> list = config.getList();
        if (list == null || list.isEmpty()) {
            return;
        }

        String dir = rpmConfig.getDir();
        FileUtil.mkdirIfUnexist(new File(dir));
        for (RpmCrawlEntity entity : list) {
            entity.setDir(dir);
            List<String> urls = crawlService.crawl(entity);
            List<String> compFiles = downService.downloadList(urls, entity);
            List<String> unCompFiles = decompService.decompress(compFiles, entity);
            FileUtil.deleteFiles(compFiles);
        }
        return;
    }
}
