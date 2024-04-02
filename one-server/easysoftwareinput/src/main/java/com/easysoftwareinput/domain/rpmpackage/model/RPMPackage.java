package com.easysoftwareinput.domain.rpmpackage.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RPMPackage extends BasePackage {
    // private String name;
    
    private String id;
    
    private String version;

    private String os;

    private String arch;

    private String rpmUpdateAt;

    private String srcRepo;

    private String rpmSize;

    private String binDownloadUrl;

    private String srcDownloadUrl;

    private String summary;

    private String osSupport;

    private String repo;

    private String repoType;

    private String installation;

    private String description;

    private String requires;
    
    private String provides;

    private String conflicts;

    private String changeLog;

    private String upStream;

    private String security;

    private String similarPkgs;

    private String pkgId;

    private String subPath;
}
