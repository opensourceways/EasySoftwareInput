package com.easysoftwareinput.infrastructure.domainpackage;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("domain_package")
public class DomainPkgDO {
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
    private String tags;

    /**
     * pkgIds of pkg.
     */
    @TableId(value = "pkg_id")
    private String pkgIds;

    /**
     * description of pkg.
     */
    private String description;
}
