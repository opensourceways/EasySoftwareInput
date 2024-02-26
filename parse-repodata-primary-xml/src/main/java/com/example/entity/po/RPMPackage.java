package com.example.entity.po;

import java.sql.Timestamp;

import org.apache.tomcat.util.bcel.Const;

import com.baomidou.mybatisplus.annotation.TableName;
import com.gitee.sunchenbin.mybatis.actable.annotation.Column;
import com.gitee.sunchenbin.mybatis.actable.annotation.IsKey;
import com.gitee.sunchenbin.mybatis.actable.annotation.Table;
import com.gitee.sunchenbin.mybatis.actable.constants.MySqlTypeConstant;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
// @Table(isSimple = true)
// @TableName("rpm_pkg_base")
public class RPMPackage {
    // @IsKey
    // @Column(length = 50)
    // private String id;

    // private Timestamp createAt;

    // private Timestamp updateAt; 
    
    @Column(length = 10)
    private String headerEnd;

    @Column(length = 20)
    private String sizeInstalled;
    
    @Column(length = 20)
    private String timeFile;
    
    @Column(length = 20)
    private String sizePackage;

    @Column(length = 6000)
    private String description;
    
    @Column(length = 5)
    private String checksumPkgid;
    
    @Column(length = 100)
    private String locationHref;
    
    @Column(length = 100)
    private String rpmBuildhost;
    
    @Column(length = 10)
    private String checksumType;
    
    @Column(length = 20)
    private String sizeArchive;
    
    @Column(length = 40)
    private String rpmVendor;
    
    @Column(length = 70)
    private String checksum;
    
    @Column(length = 50)
    private String rpmGroup;

    @Column(length = 5)
    private String headerStart;
    
    @Column(length = 200)
    private String summary;
    
    @Column(length = 100)
    private String versionRel;
    
    @Column(length = 30)
    private String versionVer;
    
    @Column(length = 30)
    private String packager;
    
    @Column(length = 150)
    private String url;
    
    @Column(length = 5)
    private String versionEpoch;
    
    @Column(length = 100)
    private String rpmSourcerpm;

    @Column(length = 1000)
    private String rpmLicense;

    @Column(length = 50)
    private String name;
    
    @Column(length = 20)
    private String timeBuild;
    
    @Column(length = 10)
    private String arch;

    @Column(length = 20)
    private String osName;
    
    @Column(length = 20)
    private String osVer;
    
    @Column(length = 20)
    private String osType;

    @Column(type = MySqlTypeConstant.LONGTEXT)
    private String requires;
    
    @Column(type = MySqlTypeConstant.LONGTEXT)
    private String provides;

    @Column(type = MySqlTypeConstant.LONGTEXT)
    private String files;

    @Column(length = 100)
    private String baseUrl;

    @Column(length = 50)
    private String rpmCategory;
}
