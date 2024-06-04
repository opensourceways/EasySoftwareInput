package com.easysoftwareinput.domain.domainpackage.model;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import lombok.Data;

@Data
public class DomainPackage {
    /**
     * os of pkg.
     */
    private String os;

    /**
     * arch of pkg.
     */
    private String arch;

    /**
     * name of pkg.
     */
    private String name;

    /**
     * version of pkg.
     */
    private String version;

    /**
     * category of pkg.
     */
    private String category;

    /**
     * iconUrl of pkg.
     */
    private String iconUrl;

    /**
     * tags of pkg.
     */
    private Set<String> tags;

    /**
     * pkgIds of pkg.
     */
    private Map<String, String> pkgIds;

    /**
     * description of pkg.
     */
    private String description;

    /**
     * init the pkg.
     */
    public DomainPackage() {
        this.tags = new HashSet<>();
        this.pkgIds = new HashMap<>();
        this.pkgIds.put("IMAGE", "");
        this.pkgIds.put("RPM", "");
        this.pkgIds.put("EPKG", "");
    }
}
