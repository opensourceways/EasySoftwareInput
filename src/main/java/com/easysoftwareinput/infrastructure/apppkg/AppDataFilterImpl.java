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
package com.easysoftwareinput.infrastructure.apppkg;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.easysoftwareinput.common.filter.DataFilter;
import com.easysoftwareinput.domain.apppackage.model.AppPackage;

@Component
public class AppDataFilterImpl implements DataFilter<AppPackage> {
    /**
     * filteringData.
     *
     * @param data
     * @return return filterd data.
     */
    @Override
    public Map<Boolean, List<AppPackage>> filteringData(List<AppPackage> data) {
        Map<Boolean, List<AppPackage>> res = new HashMap<>();
        List<AppPackage> cleanData = new ArrayList<>();
        List<AppPackage> dirtyData = new ArrayList<>();

        for (AppPackage app : data) {
            // 前端展示字段空值全校验
            if (!isValid(app)) {
                dirtyData.add(app);
                continue;
            }

            // 字段业务含义校验 type 标签校验
            if (app.getType() != null && !app.getType().equals("IMAGE")) {
                dirtyData.add(app);
                continue;
            }
            cleanData.add(app);
        }

        res.computeIfAbsent(true, k -> new ArrayList<>()).addAll(cleanData);
        res.computeIfAbsent(false, k -> new ArrayList<>()).addAll(dirtyData);

        return res;
    }

    /**
     * valid check.
     *
     * @param app
     * @return is vaild.
     */
    public boolean isValid(AppPackage app) {
        return !isEmptyOrWhitespace(app.getName()) && !isEmptyOrWhitespace(app.getOs())
                && !isEmptyOrWhitespace(app.getOsSupport()) && !isEmptyOrWhitespace(app.getMaintainerEmail())
                && !isEmptyOrWhitespace(app.getMaintainerGiteeId()) && !isEmptyOrWhitespace(app.getMaintainerId())
                && !isEmptyOrWhitespace(app.getArch()) && !isEmptyOrWhitespace(app.getAppVer())
                && !isEmptyOrWhitespace(app.getCategory());
    }

    /**
     * is empty or whitespace check.
     *
     * @param str
     * @return is vaild.
     */
    private boolean isEmptyOrWhitespace(String str) {
        return str == null || str.trim().isEmpty();
    }

}
