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

package com.easysoftwareinput.domain.repopkgnamemapper;

import java.util.Collections;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RepoPkgNamePkg {
    /**
     * branch.
     */
    private String branch;

    /**
     * org.
     */
    private String org;

    /**
     * name.
     */
    private String pkgName;

    /**
     * repoName.
     */
    private String repoName;

    /**
     * whether current branch has spec.
     */
    private boolean hasSpec;

    /**
     * raw context of .spec.
     */
    private List<String> rawSpecContextList = Collections.emptyList();

    /**
     * create RepoPkgNamePkg object.
     * @param branch branch.
     * @param org org.
     * @param repoName repoName.
     * @return RepoPkgNamePkg object.
     */
    public static RepoPkgNamePkg of(String branch, String org, String repoName) {
        RepoPkgNamePkg context = new RepoPkgNamePkg();
        context.setBranch(branch);
        context.setOrg(org);
        context.setRepoName(repoName);
        return context;
    }
}
