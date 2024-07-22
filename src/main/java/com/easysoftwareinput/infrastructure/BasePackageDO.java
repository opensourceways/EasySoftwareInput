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

package com.easysoftwareinput.infrastructure;

import java.sql.Timestamp;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;

@Data
@TableName("base_package_info")
public class BasePackageDO {
    /**
     * name of pkg.
     */
    @TableId
    private String name;

    /**
     * category of pkg.
     */
    private String category;

    /**
     * maintianer of pkg.
     */
    private String maintainerId;

    /**
     * maintainer email of pkg.
     */
    private String maintainerEmail;

    /**
     * maintainer gitee id of pkg.
     */
    private String maintainerGiteeId;

    /**
     * maintianer update at of pkg.
     */
    private String maintainerUpdateAt;

    /**
     * download count of pkg.
     */
    private String downloadCount;

    /**
     * updateat.
     */
    private Timestamp updateAt;

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
}
