package com.easysoftwareinput.domain.epkgpackage.model;

import com.easysoftwareinput.domain.rpmpackage.model.BasePackage;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EPKGPackage extends BasePackage{
    private String id;
    
    private String version;

    private String os;

    private String arch;

    private String epkgUpdateAt;

    private String srcRepo;

    private String epkgSize;

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
	
	private String files;

    private String license;
}
