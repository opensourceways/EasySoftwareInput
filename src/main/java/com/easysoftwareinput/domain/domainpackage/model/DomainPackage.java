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
