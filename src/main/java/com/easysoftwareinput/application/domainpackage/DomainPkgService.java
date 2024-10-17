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

package com.easysoftwareinput.application.domainpackage;

import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.easysoftwareinput.domain.domainpackage.ability.DomainPackageConverter;
import com.easysoftwareinput.domain.domainpackage.model.DomainPkgContext;
import com.easysoftwareinput.infrastructure.domainpackage.DomainPkgDO;
import com.easysoftwareinput.infrastructure.domainpackage.DomainPkgGatewayImpl;
import com.easysoftwareinput.infrastructure.fieldpkg.FieldGatewayImpl;
import com.easysoftwareinput.infrastructure.fieldpkg.dataobject.FieldDo;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class DomainPkgService {
    /**
     * logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(DomainPkgService.class);

    /**
     * domain pkg gateway.
     */
    @Autowired
    private DomainPkgGatewayImpl gateway;

    /**
     * field gateway.
     */
    @Autowired
    private FieldGatewayImpl fieldGateway;

    /**
     * converter.
     */
    @Autowired
    private DomainPackageConverter converter;

    /**
     * icon url service.
     */
    @Autowired
    private DomainIconUrlService iconUrlService;

    /**
     * context.
     */
    private DomainPkgContext context;

    /**
     * init the DomainPkgContext.
     * @return DomainPkgContext.
     */
    public DomainPkgContext initContext() {
        DomainPkgContext context = new DomainPkgContext();
        context.setStartTime(System.currentTimeMillis());
        context.setExistedPkgIds(gateway.getExistedPkgIds());

        List<FieldDo> fList = fieldGateway.getMainPage();
        List<DomainPkgDO> dList = converter.ofFieldDO(fList);

        if (dList == null) {
            dList = Collections.emptyList();
        }
        context.setPkgList(dList);
        context.setCount(dList.size());
        return context;
    }

    /**
     * valid the data.
     * @return if no error, return true, esle false.
     */
    public boolean validData() {
        long tableRow = gateway.getChangedRow(this.context.getStartTime());
        long updatedRow = context.getCount();
        if (tableRow == updatedRow) {
            LOGGER.info("no error in storing data. need to be stored: {}, stored: {}", updatedRow, tableRow);
            return true;
        } else {
            LOGGER.error("error in storing data. need to be stored: {}, stored: {}", updatedRow, tableRow);
            return false;
        }
    }

    /**
     * run the grogram.
     */
    public void run() {
        this.context = initContext();

        gateway.saveAll(this.context);

        validData();

        iconUrlService.updateIconUrlList();
        log.info("finish-write-domain-pkg");
    }

}
