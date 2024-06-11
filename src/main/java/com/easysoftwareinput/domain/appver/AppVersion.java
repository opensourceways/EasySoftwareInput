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

package com.easysoftwareinput.domain.appver;

import lombok.Data;

@Data
public class AppVersion {
    /**
     * name of app.
     */
    private String name;

    /**
     * upstream homepage of app.
     */
    private String upHomepage;

    /**
     * openEuller homepage of app.
     */
    private String eulerHomepage;

    /**
     * backend of app.
     */
    private String backend;

    /**
     * version of upstream app.
     */
    private String upstreamVersion;

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
     * version of openEuler os.
     */
    private String eulerOsVersion;

    /**
     * type.
     */
    private String type;
}
