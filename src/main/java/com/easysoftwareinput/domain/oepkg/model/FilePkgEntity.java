/* Copyright (c) 2024 openEuler Community
 EasySoftwareInput is licensed under the Mulan PSL v2.
 You can use this software according to the terms and conditions of the Mulan PSL v2.
 You may obtain a copy of Mulan PSL v2 at:
     http://license.coscl.org.cn/MulanPSL2
 THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
 EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
 MERCHANTABILITY OR FIT FOR A PARTICULAR PURPOSE.
 See the Mulan PSL v2 for more details.
*/

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
