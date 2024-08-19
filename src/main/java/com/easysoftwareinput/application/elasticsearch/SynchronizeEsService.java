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
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


@Service
@Slf4j
public class SynchronizeEsService {
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
        Arrays.asList("APP", "APPVER", "FIELD", "EPKG", "OEPKG","RPM");
    }


    /**
     * import db date to es by type.
     *
     * @param type type.
     */
    public void dbDataImportToEs(String type) {
        if (!checkIndexIsOk()) {
            return;
        }
        long startTime = System.currentTimeMillis();
        List<BaseSearchDo> toEsDataList = new ArrayList<>();
        int pageNum = 0;
        int pageSize = 1000;
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
                    esDataHandler.updateEsData(index, JSONObject.toJSONString(d), d.getId() + "#" + d.getDataType());
                }
                log.info("{}插入数据：{}", type, toEsDataList.size());
            }
            pageNum++;
        } while (toEsDataList.size() == pageSize);
        long endTime = System.currentTimeMillis();
        log.info("{}数据导入完成,耗时:{}", type, endTime - startTime);
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

