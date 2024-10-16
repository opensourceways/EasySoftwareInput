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

package com.easysoftwareinput.infrastructure.fieldpkg.dataobject;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.easysoftwareinput.infrastructure.rpmpkg.IDataObject;

import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.sql.Timestamp;

@Getter
@Setter
@TableName("field_package")
public class FieldDo implements IDataObject {
    /**
     * Serializable class with a defined serial version UID.
     */
    @Serial
    private String os;

    /**
     * Architecture information.
     */
    private String arch;

    /**
     * Name of the entity.
     */
    private String name;

    /**
     * Version information.
     */
    private String version;

    /**
     * Category of the entity.
     */
    private String category;

    /**
     * URL for the icon.
     */
    private String iconUrl;

    /**
     * Tags associated with the entity.
     */
    private String tags;

    /**
     * The Length of tags to the entity.
     */
    private int tagsLength;

    /**
     * Package IDs related to the entity.
     */
    @TableId(value = "pkg_ids")
    private String pkgIds;

    /**
     * Description of the entity.
     */
    private String description;

    /**
     * updateAt of pkg.
     */
    private Timestamp updateAt;

    /**
     * maintainers.
     */
    private String maintainers;

    /**
     * number of selected pkgs.
     */
    @TableField(value = "count(*)", insertStrategy = FieldStrategy.NEVER, updateStrategy = FieldStrategy.NEVER)
    private Integer count;

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

    /**
     * get appver.
     */
    @Override
    public String getAppVer() {
        return null;
    }

    /**
     * get pkg id.
     */
    @Override
    public String getPkgId() {
        return null;
    }

    /**
     * get maintianerid.
     */
    @Override
    public String getMaintainerId() {
        return this.maintainers;
    }
}
