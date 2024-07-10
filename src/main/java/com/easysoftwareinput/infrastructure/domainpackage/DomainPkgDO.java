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

package com.easysoftwareinput.infrastructure.domainpackage;

import java.sql.Timestamp;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@TableName("domain_package")
public class DomainPkgDO {
    /**
     * os of pkg.
     */
    private String os;

    /**
     * arch of pkg.
     */
    private String arch;

    /**
     * name of pkg.
     */
    private String name;

    /**
     * version of pkg.
     */
    private String version;

    /**
     * category of pkg.
     */
    private String category;

    /**
     * iconUrl of pkg.
     */
    private String iconUrl;

    /**
     * tags of pkg.
     */
    private String tags;

    /**
     * pkgIds of pkg.
     */
    @TableId(value = "pkg_ids")
    private String pkgIds;

    /**
     * description of pkg.
     */
    private String description;

    /**
     * maintainers.
     */
    private String maintainers;

    /**
     * update time in table.
     */
    private Timestamp updateAt;

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
     * set updated time of pkg.
     * @param stamp updated time of pkg.
     */
    public void setUpdateAt(Timestamp stamp) {
        if (stamp != null) {
            this.updateAt = (Timestamp) stamp.clone();
        }
    }
}
