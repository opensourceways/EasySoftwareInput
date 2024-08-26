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
package com.easysoftwareinput.infrastructure.mapper;

import com.easysoftwareinput.infrastructure.elasticsearch.dataobject.AppSearchDo;
import com.easysoftwareinput.infrastructure.elasticsearch.dataobject.RpmSearchDo;
import com.easysoftwareinput.infrastructure.elasticsearch.dataobject.AppversionSearchDo;
import com.easysoftwareinput.infrastructure.elasticsearch.dataobject.FieldSearchDo;
import com.easysoftwareinput.infrastructure.elasticsearch.dataobject.EpkgSearchDo;
import com.easysoftwareinput.infrastructure.elasticsearch.dataobject.OepkgSearchDo;
import com.easysoftwareinput.infrastructure.elasticsearch.queryobject.SearchQo;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;


@Mapper
public interface BaseSearchMapper {
    /**
     * getRmpSearchList.
     *
     * @param searchQo searchQo.
     * @return List<RpmSearchDo> .
     */
    List<RpmSearchDo> getRmpSearchList(SearchQo searchQo);

    /**
     * getAppSearchList.
     *
     * @param searchQo searchQo.
     * @return List .
     */
    List<AppSearchDo> getAppSearchList(SearchQo searchQo);

    /**
     * getAppversionSearchList.
     *
     * @param searchQo searchQo.
     * @return List .
     */
    List<AppversionSearchDo> getAppversionSearchList(SearchQo searchQo);

    /**
     * getFieldSearchList.
     *
     * @param searchQo searchQo.
     * @return List .
     */
    List<FieldSearchDo> getFieldSearchList(SearchQo searchQo);

    /**
     * getSearchEpkgList.
     *
     * @param searchQo searchQo.
     * @return List .
     */
    List<EpkgSearchDo> getSearchEpkgList(SearchQo searchQo);

    /**
     * getSearchOepkgList.
     *
     * @param searchQo searchQo.
     * @return List .
     */
    List<OepkgSearchDo> getSearchOepkgList(SearchQo searchQo);
}
