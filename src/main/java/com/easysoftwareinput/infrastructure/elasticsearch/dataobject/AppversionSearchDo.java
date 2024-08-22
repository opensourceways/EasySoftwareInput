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
public class AppversionSearchDo extends BaseSearchDo {
    /**
     * name of app.
     */
    private String name;

    /**
     * version of upstream app.
     */
    private String upstreamVersion;
    /**
     * backend of app.
     */
    private String backend;

    /**
     * version of openEuler app.
     */
    private String openeulerVersion;

    /**
     * version of ci app.
     */
    private String ciVersion;

    /**
     * status of openEuler pkg.
     */
    private String status;

    /**
     * openEuller homepage of app.
     */
    private String eulerHomepage;

    /**
     * version of openEuler os.
     */
    private String eulerOsVersion;

}
