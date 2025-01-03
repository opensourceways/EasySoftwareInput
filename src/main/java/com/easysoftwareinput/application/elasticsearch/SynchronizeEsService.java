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
package com.easysoftwareinput.application.elasticsearch;

import com.alibaba.fastjson.JSONObject;
import com.easysoftwareinput.domain.elasticsearch.ability.EsDataHandler;
import com.easysoftwareinput.infrastructure.elasticsearch.SearchGatewayImpl;
import com.easysoftwareinput.infrastructure.elasticsearch.dataobject.BaseSearchDo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


@Service
public class SynchronizeEsService {
    /**
     * logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(SynchronizeEsService.class);
    /**
     * index.
     */
    @Value("${es.index}")
    private String index;


    /**
     * mappingfilepath.
     */
    @Value("${es.mappingfilepath}")
    private String mappingfilepath;
    /**
     * indexStatus.
     */
    private Boolean indexStatusIsOk = Boolean.FALSE;

    /**
     * search gateway.
     */
    @Autowired
    private SearchGatewayImpl searchGateway;

    /**
     * esDataHandler.
     */
    @Autowired
    private EsDataHandler esDataHandler;

    /**
     * run the program.
     */
    public void run() {
        List<String> types = Arrays.asList("APP", "APPVER", "FIELD", "EPKG", "OEPKG", "RPM");
        for (String pkg : types) {
            dbDataImportToEs(pkg);
        }
    }


    /**
     * import db date to es by type.
     *
     * @param type type.
     */
    public void dbDataImportToEs(String type) {
        try {
            if (!checkIndexIsOk()) {
                return;
            }
            long startTime = System.currentTimeMillis();
            List<BaseSearchDo> toEsDataList = new ArrayList<>();
            int pageNum = 0;
            int pageSize = 3000;
            do {
                toEsDataList.clear();
                switch (type) {
                    case "APP":
                        toEsDataList.addAll(searchGateway.querySearchApplicationPage(pageNum, pageSize));
                        break;

                    case "RPM":
                        toEsDataList.addAll(searchGateway.querySearchRpmPage(pageNum, pageSize));
                        break;

                    case "APPVER":
                        toEsDataList.addAll(searchGateway.querySearchAppversionPage(pageNum, pageSize));
                        break;

                    case "FIELD":
                        toEsDataList.addAll(searchGateway.querySearchFieldPage(pageNum, pageSize));
                        break;

                    case "EPKG":
                        toEsDataList.addAll(searchGateway.querySearchEpkgPage(pageNum, pageSize));
                        break;

                    case "OEPKG":
                        toEsDataList.addAll(searchGateway.querySearchOepkgPage(pageNum, pageSize));
                        break;
                    default:
                        break;
                }
                if (!CollectionUtils.isEmpty(toEsDataList)) {
                    for (BaseSearchDo d : toEsDataList) {
                        esDataHandler.updateEsData(index,
                                JSONObject.toJSONString(d),
                                (d.getPkgId() == null ? d.getId() : d.getPkgId()) + "#" + d.getDataType());
                    }
                }
                pageNum++;
            } while (toEsDataList.size() >= pageSize);
            long endTime = System.currentTimeMillis();
            LOGGER.info("{}数据导入完成,耗时:{}", type, endTime - startTime);
        } catch (Exception e) {
            LOGGER.error("dbDataImportToEs error", e.getMessage());
        }
    }

    /**
     * check es Index IsOk.
     *
     * @return boolean.
     */
    private boolean checkIndexIsOk() {
        if (!this.indexStatusIsOk) {
            Boolean esIndex = esDataHandler.createEsIndex(index, mappingfilepath);
            this.indexStatusIsOk = esIndex;
        } else {
            this.indexStatusIsOk = esDataHandler.checkEsIndexExist(index);
        }
        return this.indexStatusIsOk;
    }
}

