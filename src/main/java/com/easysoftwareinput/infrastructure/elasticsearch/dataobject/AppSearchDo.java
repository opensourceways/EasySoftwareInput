/* Copyright (c) 2024 openEuler Community
 EasySoftwareInput is licensed under the Mulan PSL v2.
 You can use this software according to the terms and conditions of the Mulan PSL v2.
 You may obtain a copy of Mulan PSL v2 at:
     http://license.coscl.org.cn/MulanPSL2
 THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
 EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
 MERCHANTABILITY OR FIT FOR A PARTICULAR PURPOSE.
 See the Mulan PSL v2 for more details.
*/
package com.easysoftwareinput.infrastructure.elasticsearch.dataobject;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;

@Getter
@Setter
public class AppSearchDo extends BaseSearchDo {
    /**
     * Field.
     */
    private String name;
    /**
     * Field.
     */
    private String appVer;
    /**
     * Field.
     */
    private String os;
    /**
     * Field.
     */
    private String arch;
    /**
     * Field.
     */
    private String srcRepo;
    /**
     * Field.
     */
    private String category;
    /**
     * Field.
     */
    private String iconUrl;
    /**
     * Field.
     */
    private String description;
    /**
     * Field.
     */
    private String installation;
    /**
     * Field.
     */
    private String downloadCount;
    /**
     * Field.
     */
    private Timestamp epkgUpdate;
    /**
     * Field.
     */
    private Timestamp rpmUpdate;
    /**
     * Field.
     */
    @JSONField(name = "epkg_name")
    private String epkgName;
    /**
     * Field.
     */
    @JSONField(name = "rpm_name")
    private String rpmName;
    /**
     * Field.
     */
    private String tagsText;
    /**
     * Field.
     */
    @JSONField(name = "IMAGE")
    private String image;
    /**
     * Field.
     */
    @JSONField(name = "EPKG")
    private String epkg;
    /**
     * Field.
     */
    @JSONField(name = "RPM")
    private String rpm;


    /**
     * get updated time of pkg.
     *
     * @return updated time of pkg.
     */
    public Timestamp getEpkgUpdate() {
        if (this.epkgUpdate == null) {
            return null;
        }
        return (Timestamp) this.epkgUpdate.clone();
    }

    /**
     * get created time of pkg.
     *
     * @return created time of pkg.
     */
    public Timestamp getRpmUpdate() {
        if (this.rpmUpdate == null) {
            return null;
        }
        return (Timestamp) this.rpmUpdate.clone();
    }

    /**
     * set updated time of pkg.
     *
     * @param stamp updated time of pkg.
     */
    public void setEpkgUpdate(Timestamp stamp) {
        if (stamp != null) {
            this.epkgUpdate = (Timestamp) stamp.clone();
        }
    }

    /**
     * set updated time of pkg.
     *
     * @param stamp updated time of pkg.
     */
    public void setRpmUpdate(Timestamp stamp) {
        if (stamp != null) {
            this.rpmUpdate = (Timestamp) stamp.clone();
        }
    }
}
