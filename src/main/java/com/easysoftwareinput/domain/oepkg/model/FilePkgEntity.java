package com.easysoftwareinput.domain.oepkg.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FilePkgEntity {
    /**
     * file name.
     */
    private String fileName;

    /**
     * file index.
     */
    private int fileIndex;

    /**
     * os message.
     */
    private OsMes osMes;

    /**
     * create FilePkgEntity.
     * @param filePath file path.
     * @param fileIndex file index.
     * @param osMes os message.
     * @return FilePkgEntity.
     */
    public static FilePkgEntity of(String filePath, int fileIndex, OsMes osMes) {
        FilePkgEntity fPkg = new FilePkgEntity();
        fPkg.setOsMes(osMes);
        fPkg.setFileIndex(fileIndex);
        fPkg.setFileName(filePath);
        return fPkg;
    }
}
