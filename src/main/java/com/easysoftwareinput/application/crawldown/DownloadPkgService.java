package com.easysoftwareinput.application.crawldown;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.easysoftwareinput.common.constant.PkgConstant;
import com.easysoftwareinput.domain.crawldown.model.RpmCrawlEntity;
import lombok.Getter;
import lombok.Setter;

@Component
@Getter
@Setter
public class DownloadPkgService {
    /**
     * logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(DownloadPkgService.class);

    /**
     * download urls.
     * @param urls urls.
     * @param entity entity.
     * @return list of file paths.
     */
    public List<String> downloadList(List<String> urls, RpmCrawlEntity entity) {
        Map<String, String> fileMap = specifyFilePath(urls, entity);
        return asnycDownload(fileMap);
    }

    /**
     * download files.
     * @param fileMap filemap.
     * @return list of file paths.
     */
    public List<String> asnycDownload(Map<String, String> fileMap) {
        List<CompletableFuture<String>> fuList = new ArrayList<>();
        for (Map.Entry<String, String> entry : fileMap.entrySet()) {
            String url = entry.getKey();
            String filePath = entry.getValue();

            CompletableFuture<String> fu = CompletableFuture.supplyAsync(
                () -> downloadFile(url, filePath)
            ).exceptionally(ex -> {
                LOGGER.error("err download, url: {}, filePath: {}", url, filePath);
                return null;
            });
            fuList.add(fu);
        }

        List<String> res = new ArrayList<>();
        for (CompletableFuture<String> fu : fuList) {
            try {
                res.add(fu.get(20, TimeUnit.SECONDS));
            } catch (Exception e) {
                LOGGER.error("err get CompletableFuture, cause: {}", e.getMessage());
            }
        }
        return res;
    }

    /**
     * get local paths for urls.
     * @param urls urls.
     * @param entity entity.
     * @return map.
     */
    public Map<String, String> specifyFilePath(List<String> urls, RpmCrawlEntity entity) {
        if (urls == null || urls.isEmpty()) {
            return Collections.emptyMap();
        }

        Map<String, String> res = new HashMap<>();
        for (String url : urls) {
            URL urlObj = getUrl(url);
            String filePath = assembleFilePath(urlObj.getPath(), entity);
            res.put(url, filePath);
        }
        return res;
    }

    /**
     * get URL from String.
     * @param url url.
     * @return URL.
     */
    public URL getUrl(String url) {
        URL urlObj;
        try {
            urlObj = new URL(url);
        } catch (MalformedURLException e) {
            LOGGER.error("err url, url: {}, message: {}", url, e.getMessage());
            return null;
        }
        return urlObj;
    }

    /**
     * download file.
     * @param urlS url.
     * @param filePath local path.
     * @return file path.
     */
    public String downloadFile(String urlS, String filePath) {
        if (StringUtils.isBlank(urlS) || StringUtils.isBlank(filePath)) {
            return null;
        }
        URL url = getUrl(urlS);
        try (BufferedInputStream in = new BufferedInputStream(url.openStream());
                FileOutputStream out = new FileOutputStream(filePath)) {
            byte[] buffer = new byte[PkgConstant.BUFFER_SIZE];
            int byteRead;
            while ((byteRead = in.read(buffer, 0, PkgConstant.BUFFER_SIZE)) != -1) {
                out.write(buffer, 0, byteRead);
            }
            return filePath;
        } catch (Exception e) {
            LOGGER.error("fail to download url, url: {}, filePath: {}, cause: {}",
                    url, filePath, e.getMessage());
            return null;
        }
    }

    /**
     * assemble file path.
     * @param file file name.
     * @param entity entity.
     * @return file path.
     */
    public String assembleFilePath(String file, RpmCrawlEntity entity) {
        if (StringUtils.isBlank(file)) {
            return null;
        }

        List<String> paths = Arrays.stream(file.split("/")).filter(s -> {
            return filterPath(s, entity);
        }).collect(Collectors.toList());
        String fileName = StringUtils.join(paths, "_a_") + "_a_" + entity.getTarget();

        return entity.getDir() + File.separator + fileName;
    }

    /**
     * file path piece.
     * @param path piece.
     * @param entity entity.
     * @return boolean.
     */
    public boolean filterPath(String path, RpmCrawlEntity entity) {
        if (StringUtils.isBlank(path)) {
            return false;
        }
        if ("repodata".equals(path)) {
            return false;
        }

        if (path.endsWith(entity.getTarget())) {
            return false;
        }
        return true;
    }


}
