package com.easysoftwareinput.infrastructure.domainpackage;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.easysoftwareinput.domain.domainpackage.ability.DomainPackageConverter;
import com.easysoftwareinput.domain.domainpackage.model.DomainPackage;
import com.easysoftwareinput.infrastructure.mapper.DomainPkgMapper;

@Service
public class DomainPkgGatewayImpl {
    @Autowired
    DomainPkgMapper mapper;

    @Autowired
    DomainPackageConverter converter;

    public void saveAll(List<DomainPackage> pkgList) {
        for (DomainPackage pkg : pkgList) {
            save(pkg);
        }
    }

    public void save(DomainPackage pkg) {
        DomainPkgDO pkgDo = converter.toEntity(pkg);
        mapper.insert(pkgDo);
    }


}
