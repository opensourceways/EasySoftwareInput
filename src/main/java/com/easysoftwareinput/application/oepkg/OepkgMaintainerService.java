package com.easysoftwareinput.application.oepkg;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.easysoftwareinput.domain.oepkg.model.OepkgMaintainer;
import com.easysoftwareinput.domain.oepkg.model.OepkgMaintainerConfig;
import com.easysoftwareinput.infrastructure.oepkg.OepkgGatewayImpl;
import com.easysoftwareinput.infrastructure.oepkg.dataobject.OepkgDO;

@Component
public class OepkgMaintainerService {
    /**
     * logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(OepkgMaintainerService.class);

    /**
     * maintainer config.
     */
    @Autowired
    private OepkgMaintainerConfig maintianerConfig;

    /**
     * oepkg gateway.
     */
    @Autowired
    private OepkgGatewayImpl gateway;

    /**
     * update maintainer.
     */
    public void updateMaintainerList() {
        for (OepkgMaintainer maintainer : maintianerConfig.getMaintainerList()) {
            if (StringUtils.isBlank(maintainer.getPkgName())) {
                LOGGER.error("undefined pkg name, {}", maintainer);
                continue;
            }

            UpdateWrapper<OepkgDO> wrapper = new UpdateWrapper<>();
            wrapper.eq("name", maintainer.getPkgName());
            wrapper.set("maintainer_id", maintainer.getId());
            wrapper.set("maintainer_gitee_id", maintainer.getGiteeId());
            wrapper.set("maintainer_email", maintainer.getEmail());
            wrapper.set("category", maintainer.getCategory());
            int update;
            try {
                update = gateway.getBaseMapper().update(wrapper);
            } catch (Exception e) {
                LOGGER.error("fail update, name: {}, cause: {}", maintainer.getPkgName(), e.getMessage());
                continue;
            }
            if (update != 1) {
                LOGGER.error("fail update maintainer, name: {}", maintainer.getPkgName());
            }
        }
    }
}
