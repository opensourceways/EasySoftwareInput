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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.easysoftwareinput.common.components.ComponentMap;
import com.easysoftwareinput.common.utils.ObjectMapperUtil;
import com.easysoftwareinput.domain.fieldpkg.model.Field;
import com.easysoftwareinput.domain.fieldpkg.model.FieldDTO;
import com.easysoftwareinput.infrastructure.fieldpkg.dataobject.FieldDo;
import com.easysoftwareinput.infrastructure.rpmpkg.IDataObject;
import com.easysoftwareinput.infrastructure.rpmpkg.converter.IConverter;

@Component
public class FieldConverter {
    /**
     * component map.
     */
    @Autowired
    private ComponentMap componentMap;

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

        Map<String, String> sortedMap = f.getPkgIds().entrySet().stream().sorted(Map.Entry.comparingByKey()).collect(
            Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (s1, s2) -> s1, LinkedHashMap::new)
        );
        d.setPkgIds(ObjectMapperUtil.writeValueAsString(sortedMap));

        d.setMaintainers(ObjectMapperUtil.writeValueAsString(f.getMaintianers()));
        d.setUpdateAt(new Timestamp(System.currentTimeMillis()));
        return d;
    }

    /**
     * convert pkg to FieldDTO.
     * @param list list of pkgs.
     * @return lsit of FieldDTO.
     */
    public List<FieldDTO> toFieldDto(List<IDataObject> list) {
        return handleOs(list);
    }

    /**
     * handle os.
     * @param list list of pkgs.
     * @return list of pkgs.
     */
    public List<FieldDTO> handleOs(List<IDataObject> list) {
        Map<String, List<IDataObject>> osMap = list.stream().collect(Collectors.groupingBy(IDataObject::getOs));
        List<FieldDTO> resList = new ArrayList<>();
        for (Map.Entry<String, List<IDataObject>> entry : osMap.entrySet()) {
            List<FieldDTO> fList = handleName(entry.getValue());
            String os = entry.getKey();
            fList.stream().map(f -> {
                f.setOs(os);
                return f;
            }).collect(Collectors.toList());
            resList.addAll(fList);
        }
        return resList;
    }

    /**
     * handle name.
     * @param osList list of pkgs with the same os.
     * @return list of pkgs.
     */
    public List<FieldDTO> handleName(List<IDataObject> osList) {
        Map<String, List<IDataObject>> nameMap = osList.stream().collect(Collectors.groupingBy(IDataObject::getName));
        List<FieldDTO> resList = new ArrayList<>();
        for (Map.Entry<String, List<IDataObject>> entry : nameMap.entrySet()) {
            List<FieldDTO> fList = handleArch(entry.getValue());
            String name = entry.getKey();
            fList = fList.stream().map(f -> {
                f.setName(name);
                return f;
            }).collect(Collectors.toList());
            resList.addAll(fList);
        }
        return resList;
    }

    /**
     * handle arch.
     * @param nameList list of pkgs with the name name.
     * @return list of pkgs.
     */
    public List<FieldDTO> handleArch(List<IDataObject> nameList) {
        Map<String, List<IDataObject>> archMap = nameList.stream().collect(Collectors.groupingBy(IDataObject::getArch));
        List<FieldDTO> resList = new ArrayList<>();
        for (List<IDataObject> archList : archMap.values()) {
            IDataObject obj = pickOne(archList);
            FieldDTO f = new FieldDTO();
            f.setArch(obj.getArch());
            f.setPkg(obj);
            resList.add(f);
        }
        return resList;
    }

    /**
     * pick one from list.
     * @param archList list of pkgs.
     * @return one.
     */
    public IDataObject pickOne(List<IDataObject> archList) {
        if (archList == null || archList.isEmpty()) {
            return null;
        } else if (archList.size() == 1) {
            return archList.get(0);
        } else {
            IConverter iconverter = componentMap.getConverter(archList.get(0).getClass());
            IDataObject obj = iconverter.pickOneFromList(archList);
            return obj;
        }

    }
}
