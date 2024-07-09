package com.easysoftwareinput.infrastructure.appver.dataobject;

import java.sql.Timestamp;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
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

    /**
     * get updated time of pkg.
     * @return updated time of pkg.
     */
    public Timestamp getUpdateAt() {
        if (this.updateAt == null) {
            return null;
        }
        return (Timestamp) this.updateAt.clone();
    }

    /**
     * get created time of pkg.
     * @return created time of pkg.
     */
    public Timestamp getCreatedAt() {
        if (this.createdAt == null) {
            return null;
        }
        return (Timestamp) this.createdAt.clone();
    }

    /**
     * set updated time of pkg.
     * @param stamp updated time of pkg.
     */
    public void setUpdateAt(Timestamp stamp) {
        if (stamp != null) {
            this.updateAt = (Timestamp) stamp.clone();
        }
    }

    /**
     * set updated time of pkg.
     * @param stamp updated time of pkg.
     */
    public void setCreatedAt(Timestamp stamp) {
        if (stamp != null) {
            this.createdAt = (Timestamp) stamp.clone();
        }
    }
}
