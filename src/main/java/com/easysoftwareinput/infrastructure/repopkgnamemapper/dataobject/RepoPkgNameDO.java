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

package com.easysoftwareinput.infrastructure.repopkgnamemapper.dataobject;

import java.sql.Timestamp;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Getter;
import lombok.Setter;

@TableName("repo_pkg_name")
@Getter
@Setter
public class RepoPkgNameDO {
    /**
     * name.
     */
    private String name;

    /**
     * os.
     */
    private String os;

    /**
     * repo url.
     */
    private String repoUrl;

    /**
     * pkg id.
     */
    @TableId(value = "pkg_id")
    private String pkgId;

    /**
     * updateat.
     */
    private Timestamp updateAt;

    /**
     * create at.
     */
    private Timestamp createAt;

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
    public void setCreateAt(Timestamp stamp) {
        if (stamp != null) {
            this.createAt = (Timestamp) stamp.clone();
        }
    }

    /**
     * get updated time of pkg.
     * @return updated time of pkg.
     */
    public Timestamp getCreateAt() {
        if (this.createAt == null) {
            return null;
        }
        return (Timestamp) this.createAt.clone();
    }
}
