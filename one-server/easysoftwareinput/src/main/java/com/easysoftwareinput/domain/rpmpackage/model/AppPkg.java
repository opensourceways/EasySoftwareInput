package com.easysoftwareinput.domain.rpmpackage.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AppPkg extends BasePackage {
    private String iconUrl;

    private String appVer;

    private String os;

    private String arch;

    private String appSize;

    private String srcRepo;

    private String srcDownloadUrl;

    private String binDownloadUrl;
}
