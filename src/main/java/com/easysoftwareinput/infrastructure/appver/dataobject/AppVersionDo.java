package com.easysoftwareinput.infrastructure.appver.dataobject;

import java.sql.Timestamp;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;

@Data
@TableName("application_version")
public class AppVersionDo {
    /**
     * name of app.
     */
    private String name;

    /**
     * upstream homepage of app.
     */
    private String upHomepage;

    /**
     * openEuller homepage of app.
     */
    private String eulerHomepage;

    /**
     * backend of app.
     */
    private String backend;

    /**
     * version of upstream app.
     */
    private String upstreamVersion;

    /**
     * version of openEuler app.
     */
    private String openeulerVersion;

    /**
     * version of ci app.
     */
    private String ciVersion;

    /**
     * status of openEuler pkg.
     */
    private String status;

    /**
     * version of openEuler os.
     */
    private String eulerOsVersion;

    /**
     * update time of data.
     */
    private Timestamp updateAt;

    /**
     * create time of data.
     */
    private Timestamp createdAt;

    /**
     * id of data.
     */
    @TableId("id")
    private String id;

    /**
     * type.
     */
    private String type;
}
