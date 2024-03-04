package com.example.entity.po;

import java.io.Serial;
import java.sql.Timestamp;

import com.baomidou.mybatisplus.annotation.TableName;
import com.gitee.sunchenbin.mybatis.actable.annotation.Column;
import com.gitee.sunchenbin.mybatis.actable.constants.MySqlTypeConstant;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
// @Table(isSimple = true)
@TableName("epkg_pkg")
public class EPKGPackage {
    @Serial
    private String id;

    private Timestamp createAt;

    private Timestamp updateAt;

    
    private String name;

   
    private String version;

    
    private String os;

    
    private String arch;

    // 软件包分类、领域
    
    private String epkgCategory;

    
    private String epkgUpdateAt;

    
    private String srcRepo;

    
    private String epkgSize;

    
    private String binDownloadUrl;

    private String srcDownloadUrl;

    // detail page
    
    private String summary;

    // 版本支持情况
    
    private String osSupport;

    // 所属仓库
   
    private String repo;

    // repo源
    
    private String repoType;

    // 安装指引
    
    private String installation;

    
    private String description;

    @Column(type = MySqlTypeConstant.LONGTEXT)
    private String requires;
    
    @Column(type = MySqlTypeConstant.LONGTEXT)
    private String provides;

    @Column(type = MySqlTypeConstant.LONGTEXT)
    private String conflicts;

    @Column(type = MySqlTypeConstant.LONGTEXT)
    private String files;

    @Column(length = 1000)
    private String changeLog;

    private String maintanierId;

    private String maintianerEmail;

    private String maintainerGiteeId;

    private String maintainerUpdateAt;

    private String maintainerStatus;

    // 上游
    
    private String upStream;

    // 安全风险
    
    private String security;

    // rpm近似应用

    private String similarPkgs;
}
