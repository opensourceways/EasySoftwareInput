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

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RepoPkg {
    /**
     * name.
     */
    private String name;

    /**
     * os.
     */
    private String os;

    /**
     * repo url.
     */
    private String repoUrl;

    /**
     * create RepoPkg.
     * @param name name.
     * @param os os.
     * @param repoUrl repo url.
     * @return RepoPkg.
     */
    public static RepoPkg of(String name, String os, String repoUrl) {
        RepoPkg pkg = new RepoPkg();
        pkg.setName(name);
        pkg.setOs(os);
        pkg.setRepoUrl(repoUrl);
        return pkg;
    }
}
