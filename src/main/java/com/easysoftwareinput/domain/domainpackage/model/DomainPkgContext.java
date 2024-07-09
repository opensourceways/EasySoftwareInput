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

import java.util.List;

import com.easysoftwareinput.domain.oepkg.model.OePkgEntity;
import com.easysoftwareinput.infrastructure.domainpackage.DomainPkgDO;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DomainPkgContext extends OePkgEntity {
    /**
     * list of pkgs.
     */
    private List<DomainPkgDO> pkgList;
}
