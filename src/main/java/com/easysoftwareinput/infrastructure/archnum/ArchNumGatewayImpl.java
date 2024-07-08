package com.easysoftwareinput.infrastructure.archnum;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.easysoftwareinput.domain.oepkg.model.OePkgEntity;
import com.easysoftwareinput.infrastructure.mapper.OsArchNumDOMapper;
import com.easysoftwareinput.infrastructure.rpmpkg.Gateway;

@Component
@Transactional
public class ArchNumGatewayImpl extends ServiceImpl<OsArchNumDOMapper, OsArchNumDO> implements Gateway<OsArchNumDO> {
    /**
     * logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(ArchNumGatewayImpl.class);

    /**
     * save all data to database.
     * @param context context.
     * @param list lsit of pkg.
     * @return boolean.
     */
    public boolean saveAll(OePkgEntity context, List<OsArchNumDO> list) {
        Set<String> existedPkgIdSet = context.getExistedPkgIds();
        Map<Boolean, List<OsArchNumDO>> map = list.stream().collect(Collectors.partitioningBy(
                d -> existedPkgIdSet.contains(d.getPkgId())));
        return saveAndUpdate(map);
    }

    /**
     * get Logger.
     */
    @Override
    public Logger getLogger() {
        return ArchNumGatewayImpl.LOGGER;
    }

}
