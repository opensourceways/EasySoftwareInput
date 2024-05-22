package com.easysoftwareinput.infrastructure.domainpackage;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.easysoftwareinput.domain.domainpackage.ability.DomainPackageConverter;
import com.easysoftwareinput.domain.domainpackage.model.DomainPackage;
import com.easysoftwareinput.infrastructure.mapper.DomainPkgMapper;

@Service
public class DomainPkgGatewayImpl extends ServiceImpl<DomainPkgMapper, DomainPkgDO> {
    @Autowired
    DomainPackageConverter converter;

    public boolean saveAll(List<DomainPackage> fList) {
        List<DomainPkgDO> dList = converter.toDo(fList);
        return saveOrUpdateBatch(dList, 1000);
    }
}

