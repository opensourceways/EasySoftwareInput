package com.easysoftwareinput.domain.rpmpackage.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RPMPackage extends BasePackage {
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
