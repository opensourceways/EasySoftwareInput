package com.easysoftwareinput.infrastructure.appver.dataobject;

import java.sql.Timestamp;

import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;

@Data
@TableName("application_version")
public class AppVersionDo {
    public String name;
    public String upHomepage;
    public String eulerHomepage;
    public String backend;
    public String upstreamVersion;
    public String openeulerVersion;
    public String ciVersion;
    public String status;
    public String eulerOsVersion;
    private Timestamp createdAt;
    private Timestamp updateAt;
    private String id;
}
