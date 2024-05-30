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

    private String imageTags;

    private String imageUsage;

    private String dockerfileLink;

    private String pullStr;

    private String latestOsSupport;

    public AppPackage(AppPackage other) {  
        super(other);
        // 深拷贝AppPackage的字段  
        this.id = other.id;  
        this.createAt = other.createAt;  
        this.updateAt = other.updateAt;  
        this.description = other.description;  
        this.license = other.license;  
        this.download = other.download;  
        this.environment = other.environment;  
        this.installation = other.installation;  
        this.similarPkgs = other.similarPkgs;  
        this.dependencyPkgs = other.dependencyPkgs;  
        this.type = other.type;  
        this.iconUrl = other.iconUrl;  
        this.appVer = other.appVer;  
        this.osSupport = other.osSupport;  
        this.os = other.os;  
        this.arch = other.arch;  
        this.securityLevel = other.securityLevel;  
        this.safeLabel = other.safeLabel;  
        this.appSize = other.appSize;  
        this.srcRepo = other.srcRepo;  
        this.srcDownloadUrl = other.srcDownloadUrl;  
        this.binDownloadUrl = other.binDownloadUrl;  
        this.pkgId = other.pkgId;  
        this.imageTags = other.imageTags;  
        this.imageUsage = other.imageUsage;
        this.dockerfileLink = other.dockerfileLink;
        this.pullStr = other.pullStr;
    }
}
