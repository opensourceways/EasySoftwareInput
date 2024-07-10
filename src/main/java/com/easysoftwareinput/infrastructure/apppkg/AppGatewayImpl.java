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

package com.easysoftwareinput.infrastructure.apppkg;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.easysoftwareinput.domain.apppackage.model.AppPackage;
import com.easysoftwareinput.infrastructure.apppkg.converter.AppConverter;
import com.easysoftwareinput.infrastructure.apppkg.dataobject.AppDo;
import com.easysoftwareinput.infrastructure.archnum.OsArchNumDO;
import com.easysoftwareinput.infrastructure.archnum.converter.ArchNumConverter;
import com.easysoftwareinput.infrastructure.mapper.AppDoMapper;

@Component
public class AppGatewayImpl extends ServiceImpl<AppDoMapper, AppDo> {
    /**
     * mapper.
     */
    @Autowired
    private AppDoMapper mapper;

    /**
     * converter.
     */
    @Autowired
    private AppConverter converter;

    /**
     * ArchNumConverter.
     */
    @Autowired
    private ArchNumConverter archNumConverter;

    /**
     * save all pkg.
     * @param appList list of pkg.
     * @return boolean.
     */
    public boolean saveAll(List<AppPackage> appList) {
        List<AppDo> dList = converter.toDo(appList);
        return saveOrUpdateBatch(dList, 50);
    }

    /**
     * get distinct os from table.
     * @return a lsit of os.
     */
    public List<String> getOs() {
        QueryWrapper<AppDo> wrapper = new QueryWrapper<>();
        wrapper.select("distinct os");
        List<AppDo> doList = mapper.selectList(wrapper);
        List<String> osList = new ArrayList<>();
        for (AppDo pkg : doList) {
            osList.add(pkg.getOs());
        }
        return osList;
    }

    /**
     * get pkgs by os.
     * @param os os.
     * @return a list of pkg.
     */
    public List<AppDo> getPkg(String os) {
        QueryWrapper<AppDo> wrapper = new QueryWrapper<>();
        wrapper.select("os, arch, name, app_ver, category, icon_url, pkg_id, description, maintainer_id");
        wrapper.eq("os", os);
        return mapper.selectList(wrapper);
    }

    /**
     * get pkgs which will be converted to domain pkg.
     * @return a lsit of pkg.
     */
    public List<AppDo> getDomain() {
        QueryWrapper<AppDo> wrapper = new QueryWrapper<>();
        wrapper.select("os, arch, name, app_ver, category, icon_url, pkg_id, description");
        return mapper.selectList(wrapper);
    }

    /**
     * get one pkg by name.
     * @param name name.
     * @return one pkg.
     */
    public AppDo queryPkgIdByName(String name) {
        QueryWrapper<AppDo> wrapper = new QueryWrapper<>();
        wrapper.select("name, pkg_id");
        wrapper.eq("name", name);
        wrapper.last("limit 1");
        List<AppDo> list = mapper.selectList(wrapper);
        if (list.size() >= 1) {
            return list.get(0);
        }
        return new AppDo();
    }

    /**
     * get the pkgs group by os and arch.
     * @return list of OsArchNumDO.
     */
    public List<OsArchNumDO> getOsArchNum() {
        List<AppDo> list = lambdaQuery()
                .select(AppDo::getOs, AppDo::getArch, AppDo::getCount)
                .groupBy(AppDo::getOs, AppDo::getArch).list();
        return archNumConverter.ofList(list, "IMAGE");
    }
}
