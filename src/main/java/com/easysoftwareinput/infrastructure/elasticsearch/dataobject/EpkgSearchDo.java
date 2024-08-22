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

import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;

@Getter
@Setter
public class EpkgSearchDo extends BaseSearchDo {
    /**
     * originPkg.
     */
    private String originPkg;

    /**
     * Timestamp for last update.
     */
    private Timestamp updateTime;

    /**
     * Timestamp for creation.
     */
    private Timestamp createAt;

    /**
     * Name of the entity.
     */
    private String name;

    /**
     * Version information.
     */
    private String version;

    /**
     * Operating system details.
     */
    private String os;

    /**
     * Architecture details.
     */
    private String arch;

    /**
     * Category of the entity.
     */
    private String category;


    /**
     * Source repository information.
     */
    private String srcRepo;

    /**
     * Size of the EPKG package.
     */
    private String epkgSize;

    /**
     * Binary download URL.
     */
    private String binDownloadUrl;

    /**
     * Source download URL.
     */
    private String srcDownloadUrl;

    /**
     * Summary or brief description.
     */
    private String summary;

    /**
     * Supported operating systems.
     */
    private String osSupport;

    /**
     * Repository information.
     */
    private String repo;

    /**
     * Type of repository.
     */
    private String repoType;

    /**
     * Installation instructions.
     */
    private String installation;

    /**
     * Detailed description of the entity.
     */
    private String description;

    /**
     * Required dependencies.
     */
    private String requiresText;

    /**
     * Provided functionality.
     */
    private String providesText;

    /**
     * Conflicting packages.
     */
    private String conflicts;

    /**
     * Change log information.
     */
    private String changeLog;

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
     * Maintainer's status.
     */
    private String maintainerStatus;

    /**
     * Upstream source details.
     */
    private String upStream;

    /**
     * Security information.
     */
    private String security;

    /**
     * Similar packages related to this entity.
     */
    private String similarPkgs;

    /**
     * Files associated with the entity.
     */
    private String files;

    /**
     * Download count information.
     */
    private String downloadCount;

    /**
     * Package ID.
     */
    @TableId
    private String pkgId;

    /**
     * subPath.
     */
    private String subPath;

    /**
     * License.
     */
    private String license;
    /**
     * size.
     */
    private String size;

    /**
     * get updated time of pkg.
     * @return updated time of pkg.
     */
    public Timestamp getUpdateTime() {
        if (this.updateTime == null) {
            return null;
        }
        return (Timestamp) this.updateTime.clone();
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
    public void setUpdateTime(Timestamp stamp) {
        if (stamp != null) {
            this.updateTime = (Timestamp) stamp.clone();
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
}
