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

    public List<FieldDo> toDo(List<Field> fList) {
        List<FieldDo> dList = new ArrayList<>();
        for (Field f : fList) {
            dList.add(toDo(f));
        }
        return dList;
    }

    public FieldDo toDo(Field f) {
        FieldDo d = new FieldDo();
        BeanUtils.copyProperties(f, d);
        d.setTags(ObjectMapperUtil.writeValueAsString(f.getTags()));
        d.setPkgIds(ObjectMapperUtil.writeValueAsString(f.getPkgIds()));
        d.setUpdateAt(new Timestamp(System.currentTimeMillis()));
        return d;
    }
    
}
