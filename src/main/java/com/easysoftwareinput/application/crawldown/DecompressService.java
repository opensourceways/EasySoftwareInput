package com.easysoftwareinput.application.crawldown;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.zip.GZIPInputStream;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.easysoftwareinput.common.constant.PkgConstant;
import com.easysoftwareinput.common.utils.FileUtil;
import com.easysoftwareinput.domain.crawldown.model.RpmCrawlEntity;
import com.github.luben.zstd.ZstdInputStream;

@Component
public class DecompressService {
    /**
     * logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(DecompressService.class);

    /**
     * decompress files.
     * @param compressedFiles files.
     * @param entity entity.
     * @return list of decompressed files.
     */
    public List<String> decompress(List<String> compressedFiles, RpmCrawlEntity entity) {
        List<String> uncompFiles = new ArrayList<>();
        for (String file : compressedFiles) {
            String toFile = getToFile(file, entity);
            uncompFiles.add(deCompress(file, toFile, entity));
        }
        return uncompFiles.stream().filter(StringUtils::isNotBlank).collect(Collectors.toList());
    }

    /**
     * get target file.
     * @param file origin file.
     * @param entity entity.
     * @return target file.
     */
    public String getToFile(String file, RpmCrawlEntity entity) {
        String[] fileSplits = FileUtil.extractFileNameWithoutExt(file);
        return entity.getDir() + File.separator + StringUtils.join(fileSplits, "_a_") + "_a_primary.xml";
    }

    /**
     * decompress file.
     * @param fromFile origin file.
     * @param toFile target file.
     * @param entity enitty.
     * @return decompressed file.
     */
    public String deCompress(String fromFile, String toFile, RpmCrawlEntity entity) {
        String target = entity.getTarget();
        if (StringUtils.isBlank(target)) {
            return null;
        }
        if (target.endsWith("gz")) {
            return unzip(fromFile, toFile);
        } else if (target.endsWith("zst")) {
            return unZst(fromFile, toFile);
        }
        return null;
    }

    /**
     * decompress `zst` file.
     * @param fromFile origin file.
     * @param toFile target file.
     * @return decompressed file.
     */
    public String unZst(String fromFile, String toFile) {
        try (FileInputStream in = new FileInputStream(fromFile);
                ZstdInputStream zin = new ZstdInputStream(in);
                FileOutputStream out = new FileOutputStream(toFile)) {
            byte[] buffer = new byte[4096];
            int len;
            while ((len = zin.read(buffer)) > 0) {
                out.write(buffer, 0, len);
            }
            return toFile;
        } catch (Exception e) {
            LOGGER.error("fail unzst, cause: {}", e.getMessage());
            return null;
        }
    }

    /**
     * unzip the file.
     * @param fromFile origin file.
     * @param toFile target file.
     * @return decompressed file.
     */
    public String unzip(String fromFile, String toFile) {
        try (FileInputStream fin = new FileInputStream(fromFile);
                GZIPInputStream gzin = new GZIPInputStream(fin);
                FileOutputStream fout = new FileOutputStream(toFile)) {
            byte[] buffer = new byte[PkgConstant.BUFFER_SIZE];
            int len;
            while ((len = gzin.read(buffer)) > 0) {
                fout.write(buffer, 0, len);
            }
            return toFile;
        } catch (Exception e) {
            LOGGER.error("fail unzip, filename: {}, cause: {}", fromFile, e.getMessage());
            return null;
        }
    }
}
