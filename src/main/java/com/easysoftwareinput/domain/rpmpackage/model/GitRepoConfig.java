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

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;

@Component
@ConfigurationProperties(prefix = "git-repo")
@Getter
@Setter
public class GitRepoConfig {
    /**
     * org-template.
     */
    private String orgTemplate;

    /**
     * repo branch template.
     */
    private String repoBranchTemplate;

    /**
     * tree template.
     */
    private String treeTemplate;

    /**
     * blob text template.
     */
    private String blobTextTemplate;

    /**
     * token.
     */
    private String token;

    /**
     * per page.
     */
    private String perPage;

    /**
     * page interval.
     */
    private int pageInterval;
}
