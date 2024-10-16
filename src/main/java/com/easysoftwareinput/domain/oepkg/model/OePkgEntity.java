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

package com.easysoftwareinput.domain.oepkg.model;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import com.easysoftwareinput.infrastructure.BasePackageDO;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OePkgEntity {
    /**
     * src url of pkgs.
     */
    private Map<String, String> srcUrls = Collections.emptyMap();

    /**
     * start time of current service.
     */
    private long startTime;

    /**
     * map of os from file name.
     */
    private Map<String, OsMes> osMesMap = Collections.emptyMap();

    /**
     * number of updated pkgs.
     */
    private int count;

    /**
     * the pkgid already in table.
     */
    private Set<String> existedPkgIds = Collections.emptySet();

    /**
     * maintainers.
     */
    private Map<String, BasePackageDO> maintainers = Collections.emptyMap();

    /**
     * the size of element for each thread.
     */
    private int threadElementSize;

    /**
     * element sizes.
     */
    private int elementSize;

    /**
     * thread pool.
     */
    private ThreadPoolTaskExecutor executor;
}
