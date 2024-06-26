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

package com.easysoftwareinput.domain.oepkg.model;

import com.easysoftwareinput.domain.rpmpackage.model.BasePackage;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@SuppressFBWarnings("EQ_DOESNT_OVERRIDE_EQUALS")
public class OePkg extends BasePackage {
/**
     * id of pkg.
     */
    private String id;

    /**
     * version of pkg.
     */
    private String version;

    /**
     * os of pkg.
     */
    private String os;

    /**
     * arch of pkg.
     */
    private String arch;

    /**
     * rpmUpdateAt of pkg.
     */
    private String rpmUpdateAt;

    /**
     * srcRepo of pkg.
     */
    private String srcRepo;

    /**
     * rpmSize of pkg.
     */
    private String rpmSize;

    /**
     * binDownloadUrl of pkg.
     */
    private String binDownloadUrl;

    /**
     * srcDownloadUrl of pkg.
     */
    private String srcDownloadUrl;

    /**
     * summary of pkg.
     */
    private String summary;

    /**
     * osSupport of pkg.
     */
    private String osSupport;

    /**
     * repo of pkg.
     */
    private String repo;

    /**
     * repoType of pkg.
     */
    private String repoType;

    /**
     * installation of pkg.
     */
    private String installation;

    /**
     * description of pkg.
     */
    private String description;

    /**
     * requires of pkg.
     */
    private String requires;

    /**
     * provides of pkg.
     */
    private String provides;

    /**
     * conflicts of pkg.
     */
    private String conflicts;

    /**
     * changeLog of pkg.
     */
    private String changeLog;

    /**
     * upStream of pkg.
     */
    private String upStream;

    /**
     * security of pkg.
     */
    private String security;

    /**
     * similarPkgs of pkg.
     */
    private String similarPkgs;

    /**
     * pkgId of pkg.
     */
    private String pkgId;

    /**
     * subPath of pkg.
     */
    private String subPath;

    /**
     * license of pkg.
     */
    private String license;
}
