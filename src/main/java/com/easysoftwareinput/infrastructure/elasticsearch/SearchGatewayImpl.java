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
package com.easysoftwareinput.infrastructure.elasticsearch;

import com.easysoftwareinput.infrastructure.elasticsearch.dataobject.AppSearchDo;
import com.easysoftwareinput.infrastructure.elasticsearch.dataobject.RpmSearchDo;
import com.easysoftwareinput.infrastructure.elasticsearch.dataobject.AppversionSearchDo;
import com.easysoftwareinput.infrastructure.elasticsearch.dataobject.FieldSearchDo;
import com.easysoftwareinput.infrastructure.elasticsearch.dataobject.EpkgSearchDo;
import com.easysoftwareinput.infrastructure.elasticsearch.dataobject.OepkgSearchDo;
import com.easysoftwareinput.infrastructure.elasticsearch.queryobject.SearchQo;
import com.easysoftwareinput.infrastructure.mapper.BaseSearchMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class SearchGatewayImpl {
    /**
     * baseSearchMapper.
     */
    @Autowired
    private BaseSearchMapper baseSearchMapper;

    /**
     * querySearchApplicationPage.
     *
     * @param pageNum  pageNum.
     * @param pageSize pageSize.
     * @return List<AppSearchDo>.
     */
    public List<AppSearchDo> querySearchApplicationPage(Integer pageNum, Integer pageSize) {

        SearchQo searchQo = getSearchQo(pageNum, pageSize);
        List<AppSearchDo> appSearchList = baseSearchMapper.getAppSearchList(searchQo);
        return appSearchList;
    }

    /**
     * querySearchRpmPage.
     *
     * @param pageNum  pageNum.
     * @param pageSize pageSize.
     * @return List<RpmSearchDo> .
     */

    public List<RpmSearchDo> querySearchRpmPage(Integer pageNum, Integer pageSize) {
        SearchQo searchQo = getSearchQo(pageNum, pageSize);
        List<RpmSearchDo> rmpSearchList = baseSearchMapper.getRmpSearchList(searchQo);
        return rmpSearchList;
    }

    /**
     * querySearchAppversionPage.
     *
     * @param pageNum  pageNum.
     * @param pageSize pageSize.
     * @return List<AppversionSearchDo>.
     */
    public List<AppversionSearchDo> querySearchAppversionPage(Integer pageNum, Integer pageSize) {
        SearchQo searchQo = getSearchQo(pageNum, pageSize);
        List<AppversionSearchDo> appversionSearchList = baseSearchMapper.getAppversionSearchList(searchQo);
        return appversionSearchList;
    }

    /**
     * querySearchFieldPage.
     *
     * @param pageNum  pageNum.
     * @param pageSize pageSize.
     * @return List<FieldSearchDo>.
     */
    public List<FieldSearchDo> querySearchFieldPage(Integer pageNum, Integer pageSize) {
        SearchQo searchQo = getSearchQo(pageNum, pageSize);
        List<FieldSearchDo> fieldSearchList = baseSearchMapper.getFieldSearchList(searchQo);
        return fieldSearchList;
    }

    /**
     * querySearchEpkgPage.
     *
     * @param pageNum  pageNum.
     * @param pageSize pageSize.
     * @return List<EpkgSearchDo>.
     */
    public List<EpkgSearchDo> querySearchEpkgPage(Integer pageNum, Integer pageSize) {
        SearchQo searchQo = getSearchQo(pageNum, pageSize);
        List<EpkgSearchDo> searchEpkgList = baseSearchMapper.getSearchEpkgList(searchQo);
        return searchEpkgList;
    }

    /**
     * querySearchOepkgPage.
     *
     * @param pageNum  pageNum.
     * @param pageSize pageSize.
     * @return List<OepkgSearchDo>.
     */
    public List<OepkgSearchDo> querySearchOepkgPage(Integer pageNum, Integer pageSize) {
        SearchQo searchQo = getSearchQo(pageNum, pageSize);
        List<OepkgSearchDo> oepkgSearchList = baseSearchMapper.getSearchOepkgList(searchQo);
        return oepkgSearchList;
    }

    /**
     * get Search Queryobject.
     *
     * @param pageNum  pageNum.
     * @param pageSize pageSize.
     * @return SearchQo.
     */
    private SearchQo getSearchQo(Integer pageNum, Integer pageSize) {
        SearchQo searchQo = new SearchQo();
        searchQo.setStart(pageNum * pageSize);
        searchQo.setPagesize(pageSize);
        return searchQo;
    }
}
