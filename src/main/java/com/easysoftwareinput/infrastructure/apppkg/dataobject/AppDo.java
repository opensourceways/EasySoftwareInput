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

package com.easysoftwareinput.infrastructure.apppkg.dataobject;

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
@TableName("application_package")
public class AppDo implements IDataObject {
    /**
     * Serializable class with a defined serial version UID.
     */
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * Description of the entity.
     */
    private String description;

    /**
     * Name of the entity.
     */
    private String name;

    /**
     * License information.
     */
    private String license;

    /**
     * Download link or location.
     */
    private String download;

    /**
     * Category of the entity.
     */
    private String category;

    /**
     * Environment details.
     */
    private String environment;

    /**
     * Installation instructions.
     */
    private String installation;

    /**
     * Similar packages related to this entity.
     */
    private String similarPkgs;

    /**
     * Dependency packages required.
     */
    private String dependencyPkgs;

    /**
     * ID of the entity.
     */
    private String id;

    /**
     * Timestamp for creation.
     */
    private Timestamp createAt;

    /**
     * Timestamp for last update.
     */
    private Timestamp updateAt;

    /**
     * Type of entity.
     */
    private String type;

    /**
     * URL for the icon.
     */
    private String iconUrl;

    /**
     * Version of the application.
     */
    private String appVer;

    /**
     * Supported operating systems.
     */
    private String osSupport;

    /**
     * Operating system information.
     */
    private String os;

    /**
     * Architecture details.
     */
    private String arch;

    /**
     * Maintainer's ID.
     */
    private String maintainerId;

    /**
     * Maintainer's email address.
     */
    private String maintainerEmail;

    /**
     * Maintainer's Gitee ID.
     */
    private String maintainerGiteeId;

    /**
     * Timestamp for last maintainer update.
     */
    private String maintainerUpdateAt;

    /**
     * Security level information.
     */
    private String securityLevel;

    /**
     * Safety label information.
     */
    private String safeLabel;

    /**
     * Download count information.
     */
    private String downloadCount;

    /**
     * Size of the application package.
     */
    private String appSize;

    /**
     * Source repository information.
     */
    private String srcRepo;

    /**
     * Source download URL.
     */
    private String srcDownloadUrl;

    /**
     * Binary download URL.
     */
    private String binDownloadUrl;

    /**
     * Package ID.
     */
    @TableId(value = "pkg_id")
    private String pkgId;

    /**
     * Tags associated with images.
     */
    private String imageTags;

    /**
     * Usage information for images.
     */
    private String imageUsage;

    /**
     * latestOsSupport of pkg.
     */
    private String latestOsSupport;

    /**
     * repo.
     */
    private String repo;

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
     * get created time of pkg.
     * @return created time of pkg.
     */
    public Timestamp getCreateAt() {
        if (this.createAt == null) {
            return null;
        }
        return (Timestamp) this.createAt.clone();
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
    public void setCreateAt(Timestamp stamp) {
        if (stamp != null) {
            this.createAt = (Timestamp) stamp.clone();
        }
    }

    /**
     * get version.
     */
    @Override
    public String getVersion() {
        return null;
    }
}

