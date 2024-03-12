package com.example.entity.po;

import java.io.Serial;
import java.sql.Timestamp;

import com.baomidou.mybatisplus.annotation.TableName;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
// @Table(isSimple = true)
@TableName("application_package")
public class AppPkg {
    @Serial
    private String id;

    private Timestamp createAt;

    private Timestamp updateAt;

    public String description;

    public String name;

    public String license;

    private String download;
    
    private String environment;
    
    private String installation;

    private String similarPkgs;

    private String dependencyPkgs;

    private String appCategory;

    private String type;

    private String iconUrl;

    private String appVer;

    private String osSupport;

    private String os;

    private String arch;

    private String maintainerId;

    private String maintainerEmail;

    private String maintainerGiteeId;

    private String maintainerUpdateAt;

    private String securityLevel;

    private String safeLabel;

    private String downloadCount;

    private String appSize;

    private String srcRepo;

    private String srcDownloadUrl;

    private String binDownloadUrl;
}
