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
package com.easysoftwareinput.infrastructure.elasticsearch.dataobject;

import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class RpmSearchDo extends BaseSearchDo {
    /**
     * Field.
     */
    private String name;
    /**
     * Field.
     */
    private String version;
    /**
     * Field.
     */
    private String os;
    /**
     * Field.
     */
    private String arch;
    /**
     * Field.
     */
    private String srcRepo;
    /**
     * Field.
     */
    private String summary;
    /**
     * Field.
     */
    private String description;
    /**
     * Field.
     */
    private String updatetime;
    /**
     * Field.
     */
    private String size;
    /**
     * Field.
     */
    private String binDownloadUrl;
    /**
     * Field.
     */
    private String category;
    /**
     * Field.
     */
    private String requiresText;
    /**
     * Field.
     */
    private String providesText;
    /**
     * Field.
     */
    private String downloadCount;
    /**
     * Field.
     */
    private String originPkg;


}
