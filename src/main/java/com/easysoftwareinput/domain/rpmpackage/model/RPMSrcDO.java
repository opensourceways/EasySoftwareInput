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

package com.easysoftwareinput.domain.rpmpackage.model;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.sql.Timestamp;

@Getter
@Setter
@TableName("rpm_pkg_src")
public class RPMSrcDO {
    /**
     * Serializable class with a defined serial version UID.
     */
    @Serial
    private String id;

    /**
     * Timestamp for creation.
     */
    private Timestamp createAt;

    /**
     * Timestamp for update.
     */
    private Timestamp updateAt;

    /**
     * Name of the entity.
     */
    private String name;

    /**
     * Version information.
     */
    private String version;

    /**
     * Operating system information.
     */
    private String os;

    /**
     * Architecture information.
     */
    private String arch;

    /**
     * Category of the entity.
     */
    private String category;

    /**
     * Timestamp for RPM package update.
     */
    private String rpmUpdateAt;

    /**
     * Source repository information.
     */
    private String srcRepo;

    /**
     * Size of the RPM package.
     */
    private String rpmSize;

    /**
     * Binary download URL.
     */
    private String binDownloadUrl;

    /**
     * Source download URL.
     */
    private String srcDownloadUrl;

    /**
     * Summary of the entity.
     */
    private String summary;

    /**
     * Operating system support information.
     */
    private String osSupport;

    /**
     * Repository information.
     */
    private String repo;

    /**
     * Type of the repository.
     */
    private String repoType;

    /**
     * Installation instructions.
     */
    private String installation;

    /**
     * Description of the entity.
     */
    private String description;

    /**
     * Requirements information.
     */
    private String requires;


    /**
     * Provides information.
     */
    private String provides;

    /**
     * Conflicts information.
     */
    private String conflicts;

    /**
     * Change log information.
     */
    private String changeLog;

    /**
     * Maintainer ID.
     */
    private String maintainerId;

    /**
     * Maintainer email.
     */
    private String maintainerEmail;

    /**
     * Maintainer Gitee ID.
     */
    private String maintainerGiteeId;

    /**
     * Timestamp for maintainer update.
     */
    private String maintainerUpdateAt;

    /**
     * Maintainer status information.
     */
    private String maintainerStatus;

    /**
     * Upstream information.
     */
    private String upStream;

    /**
     * Security information.
     */
    private String security;

    /**
     * Similar packages information.
     */
    private String similarPkgs;

    /**
     * Download count information.
     */
    private String downloadCount;

    /**
     * Package ID.
     */
    private String pkgId;

    /**
     * Sub-path information.
     */
    private String subPath;

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
}
