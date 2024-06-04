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

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BasePackage {
    /**
     * name of pkg.
     */
    private String name;

    /**
     * category of pkg.
     */
    private String category;

    /**
     * maintainerId of pkg.
     */
    private String maintainerId;

    /**
     * maintainerEmail of pkg.
     */
    private String maintainerEmail;

    /**
     * maintainerGiteeId of pkg.
     */
    private String maintainerGiteeId;

    /**
     * maintainerUpdateAt of pkg.
     */
    private String maintainerUpdateAt;

    /**
     * downloadCount of pkg.
     */
    private String downloadCount;
}
