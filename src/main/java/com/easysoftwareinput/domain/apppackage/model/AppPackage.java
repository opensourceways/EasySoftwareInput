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

package com.easysoftwareinput.domain.apppackage.model;

import java.io.Serial;
import java.sql.Timestamp;
import java.util.Map;

import com.easysoftwareinput.domain.rpmpackage.model.BasePackage;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@SuppressFBWarnings("EQ_DOESNT_OVERRIDE_EQUALS")
public class AppPackage extends BasePackage {
    /**
     * uuid.
     */
    @Serial
    private String id;

    /**
     * time of pkg created.
     */
    private Timestamp createAt;

    /**
     * time of pkg updated.
     */
    private Timestamp updateAt;

    /**
     * description of pkg.
     */
    private String description;

    /**
     * license of pkg.
     */
    private String license;

    /**
     * download of pkg.
     */
    private String download;

    /**
     * environment of pkg.
     */
    private String environment;

    /**
     * installation  of pkg.
     */
    private String installation;

    /**
     * similarPkgs of pkg.
     */
    private String similarPkgs;

    /**
     * dependencyPkgs of pkg.
     */
    private String dependencyPkgs;

    /**
     * type of pkg.
     */
    private String type;

    /**
     * iconUrl of pkg.
     */
    private String iconUrl;

    /**
     * appVer of pkg.
     */
    private String appVer;

    /**
     * osSupport of pkg.
     */
    private String osSupport;

    /**
     * os of pkg.
     */
    private String os;

    /**
     * arch of pkg.
     */
    private String arch;

    /**
     * securityLevel of pkg.
     */
    private String securityLevel;

    /**
     * safeLabel of pkg.
     */
    private String safeLabel;

    /**
     * appSize of pkg.
     */
    private String appSize;

    /**
     * srcRepo of pkg.
     */
    private String srcRepo;

    /**
     * srcDownloadUrl of pkg.
     */
    private String srcDownloadUrl;

    /**
     * binDownloadUrl of pkg.
     */
    private String binDownloadUrl;

    /**
     * pkgId of pkg.
     */
    private String pkgId;

    /**
     * imageTags of pkg.
     */
    private String imageTags;

    /**
     * imageUsage of pkg.
     */
    private String imageUsage;

    /**
     * dockerfileLink of pkg.
     */
    private String dockerfileLink;

    /**
     * pullStr of pkg.
     */
    private String pullStr;

    /**
     * latestOsSupport of pkg.
     */
    private String latestOsSupport;

    /**
     * repo.
     */
    private Map<String, String> repo;

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
