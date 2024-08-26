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
package com.easysoftwareinput.domain.elasticsearch.ability;

import com.easysoftwareinput.common.utils.JsonFileUtil;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.CreateIndexResponse;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.core.TimeValue;
import org.elasticsearch.xcontent.XContentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class EsDataHandler {
    /**
     * logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(EsDataHandler.class);
    /**
     * restHighLevelClient.
     */
    @Autowired
    private RestHighLevelClient restHighLevelClient;

    /**
     * create Es Index.
     *
     * @param index       index.
     * @param mappingPath es field mappingfile path.
     * @return Boolean.
     */

    public Boolean createEsIndex(String index, String mappingPath) {
        try {
            if (checkEsIndexExist(index)) {
                return Boolean.TRUE;
            }
            CreateIndexRequest createIndexRequest = new CreateIndexRequest(index);
            String mapping = JsonFileUtil.read(mappingPath);

            createIndexRequest.mapping(mapping, XContentType.JSON);
            createIndexRequest.setTimeout(TimeValue.timeValueMillis(1));
            CreateIndexResponse createIndexResponse = restHighLevelClient
                    .indices()
                    .create(createIndexRequest, RequestOptions.DEFAULT);
            if (createIndexResponse != null && index.equals(createIndexResponse.index())) {
                return Boolean.TRUE;
            }

        } catch (Exception e) {
            LOGGER.error("createEsIndex error", e.getMessage());
        }

        return Boolean.FALSE;
    }

    /**
     * create Es Index.
     *
     * @param index index.
     * @return Boolean.
     */
    public Boolean deleteIndex(String index) {

        boolean exists = checkEsIndexExist(index);
        if (!exists) {
            //不存在就结束
            return Boolean.TRUE;
        }
        //索引存在，就执行删除
        long start = System.currentTimeMillis();

        DeleteIndexRequest request = new DeleteIndexRequest(index);
        request.timeout(TimeValue.timeValueMinutes(2));
        request.timeout("2m");
        try {
            AcknowledgedResponse delete = restHighLevelClient.indices().delete(request, RequestOptions.DEFAULT);
            LOGGER.info("删除索引成功：" + index);
        } catch (Exception e) {
            LOGGER.error("deleteIndex error", e.getMessage());
        } finally {
            try {
                restHighLevelClient.close();
            } catch (Exception e) {
                LOGGER.error("index  close error", e.getMessage());
            }
        }
        long end = System.currentTimeMillis();
        //计算删除耗时
        LOGGER.info("删除{}索引成功，耗时：{}", index, end - start);
        return Boolean.TRUE;

    }

    /**
     * checkEsIndexExist.
     *
     * @param index index.
     * @return Boolean.
     */
    public Boolean checkEsIndexExist(String index) {
        GetIndexRequest request = new GetIndexRequest(index);
        request.local(false);
        request.humanReadable(true);
        request.includeDefaults(false);
        try {
            return restHighLevelClient.indices().exists(request, RequestOptions.DEFAULT);
        } catch (Exception e) {
            LOGGER.error("checkEsIndexExist error", e.getMessage());
        }
        return Boolean.FALSE;
    }

    /**
     * updateEsData.
     *
     * @param index index.
     * @param json  date.
     * @param id    date id in index.
     * @return Boolean.
     */
    public Boolean updateEsData(String index, String json, String id) {
        UpdateRequest updateRequest = new UpdateRequest(index, id)
                .doc(json, XContentType.JSON)
                .upsert(json, XContentType.JSON);

        try {
            restHighLevelClient.update(updateRequest, RequestOptions.DEFAULT);
        } catch (Exception e) {
            LOGGER.error("updateEsData error", e.getMessage());
        }
        return Boolean.TRUE;
    }

}
