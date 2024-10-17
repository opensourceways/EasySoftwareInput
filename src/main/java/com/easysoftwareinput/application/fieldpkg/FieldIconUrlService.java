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

package com.easysoftwareinput.application.fieldpkg;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.easysoftwareinput.application.iconurl.AbstractIconUrlServer;
import com.easysoftwareinput.domain.fieldpkg.model.FieldIconUrlConfig;
import com.easysoftwareinput.domain.fieldpkg.model.IconUrl;
import com.easysoftwareinput.infrastructure.fieldpkg.dataobject.FieldDo;
import com.easysoftwareinput.infrastructure.mapper.FieldDoMapper;

@Component
public class FieldIconUrlService extends AbstractIconUrlServer<FieldDoMapper, FieldDo> {
    /**
     * config.
     */
    @Autowired
    private FieldIconUrlConfig config;

    /**
     * get icon url list.
     */
    @Override
    protected List<IconUrl> getIconUrlList() {
        return config.getIconUrlList();
    }
}
