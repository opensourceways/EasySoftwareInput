package com.easysoftwareinput.application.domainpackage;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.easysoftwareinput.application.iconurl.AbstractIconUrlServer;
import com.easysoftwareinput.domain.domainpackage.model.DomainIconUrlConfig;
import com.easysoftwareinput.domain.fieldpkg.model.IconUrl;
import com.easysoftwareinput.infrastructure.domainpackage.DomainPkgDO;
import com.easysoftwareinput.infrastructure.mapper.DomainPkgMapper;

@Component
public class DomainIconUrlService extends AbstractIconUrlServer<DomainPkgMapper, DomainPkgDO> {
    /**
     * config.
     */
    @Autowired
    private DomainIconUrlConfig config;

    /**
     * get icon url list.
     */
    @Override
    protected List<IconUrl> getIconUrlList() {
        return config.getIconUrlList();
    }
}
