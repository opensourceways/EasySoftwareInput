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

package com.easysoftwareinput.application.iconurl;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.easysoftwareinput.domain.fieldpkg.model.IconUrl;

public abstract class AbstractIconUrlServer<M extends BaseMapper<T>, T> extends ServiceImpl<M, T> {
    /**
     * logger.
     */
    private Logger logger;

    /**
     * constructor.
     */
    public AbstractIconUrlServer() {
        logger = LoggerFactory.getLogger(getClass());
    }

    /**
     * get icon url list.
     * @return icon url list.
     */
    protected abstract List<IconUrl> getIconUrlList();

    /**
     * update icon url list.
     */
    public final void updateIconUrlList() {
        List<IconUrl> iconUrlList = getIconUrlList();
        if (iconUrlList == null || iconUrlList.isEmpty()) {
            logger.info("no field icon url");
            return;
        }

        for (IconUrl iconUrl : iconUrlList) {
            updateIconUrl(iconUrl);
        }
    }

    /**
     * update icon url.
     * @param iconUrl icon url.
     * @return boolean.
     */
    public boolean updateIconUrl(IconUrl iconUrl) {
        String name = iconUrl.getName();
        String url = iconUrl.getUrl();
        if (StringUtils.isBlank(name)) {
            logger.info("no name in config, url: {}", url);
            return false;
        }

        UpdateWrapper<T> wrapper = new UpdateWrapper<>();
        wrapper.eq("name", name);
        wrapper.set("icon_url", url);
        int update = 0;
        try {
            update = getBaseMapper().update(wrapper);
        } catch (Exception e) {
            logger.error("fail update, name: {}, cause: {}", name, e.getMessage());
        }
        return update > 0;
    }
}
