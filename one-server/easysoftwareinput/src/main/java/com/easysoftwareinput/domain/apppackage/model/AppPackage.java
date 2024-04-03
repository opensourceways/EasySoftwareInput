package com.easysoftwareinput.domain.apppackage.model;

import java.io.Serial;
import java.sql.Timestamp;

import com.easysoftwareinput.domain.rpmpackage.model.BasePackage;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AppPackage extends BasePackage {
    @Serial
    private String id;

    private Timestamp createAt;

    private Timestamp updateAt;

    public String description;

    public String license;

    private String download;
    
    private String environment;
    
    private String installation;

    private String similarPkgs;

    private String dependencyPkgs;

    private String type;

    private String iconUrl;

    private String appVer;

    private String osSupport;

    private String os;

    private String arch;

    private String securityLevel;

    private String safeLabel;

    private String appSize;

    private String srcRepo;

    private String srcDownloadUrl;

    private String binDownloadUrl;

    private String pkgId;
}
