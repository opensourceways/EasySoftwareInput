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
    @TableId(value = "pkg_ids")
    private String pkgIds;

    /**
     * description of pkg.
     */
    private String description;
}
