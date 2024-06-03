package com.easysoftwareinput.domain.epkgpackage.model;

import com.easysoftwareinput.domain.rpmpackage.model.BasePackage;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EPKGPackage extends BasePackage {
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
     * epkgUpdateAt of pkg.
     */
    private String epkgUpdateAt;

    /**
     * srcRepo of pkg.
     */
    private String srcRepo;

    /**
     * epkgSize of pkg.
     */
    private String epkgSize;

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
     * files of pkg.
     */
    private String files;

    /**
     * license of pkg.
     */
    private String license;
}
