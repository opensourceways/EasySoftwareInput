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

package com.easysoftwareinput.infrastructure.fieldpkg.converter;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import com.easysoftwareinput.common.utils.ObjectMapperUtil;
import com.easysoftwareinput.domain.fieldpkg.model.Field;
import com.easysoftwareinput.infrastructure.fieldpkg.dataobject.FieldDo;

@Component
public class FieldConverter {

    /**
     * convert pkg to data object.
     * @param fList list of pkg.
     * @return list of data object.
     */
    public List<FieldDo> toDo(List<Field> fList) {
        List<FieldDo> dList = new ArrayList<>();
        for (Field f : fList) {
            dList.add(toDo(f));
        }
        return dList;
    }

    /**
     * convert pkg to data object.
     * @param f pkg.
     * @return data object.
     */
    public FieldDo toDo(Field f) {
        FieldDo d = new FieldDo();
        BeanUtils.copyProperties(f, d);
        d.setTags(ObjectMapperUtil.writeValueAsString(f.getTags()));
        d.setPkgIds(ObjectMapperUtil.writeValueAsString(f.getPkgIds()));
        d.setUpdateAt(new Timestamp(System.currentTimeMillis()));
        return d;
    }
}
