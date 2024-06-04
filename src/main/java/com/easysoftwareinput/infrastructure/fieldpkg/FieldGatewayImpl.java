package com.easysoftwareinput.infrastructure.fieldpkg;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.easysoftwareinput.domain.fieldpkg.model.Field;
import com.easysoftwareinput.infrastructure.fieldpkg.converter.FieldConverter;
import com.easysoftwareinput.infrastructure.fieldpkg.dataobject.FieldDo;
import com.easysoftwareinput.infrastructure.mapper.FieldDoMapper;

@Component
public class FieldGatewayImpl extends ServiceImpl<FieldDoMapper, FieldDo> {
    /**
     * converter.
     */
    @Autowired
    private FieldConverter converter;

    /**
     * save all the pkg.
     * @param fList list of pkg.
     * @return boolean.
     */
    public boolean saveAll(List<Field> fList) {
        List<FieldDo> dList = converter.toDo(fList);
        return saveOrUpdateBatch(dList, 1000);
    }
}
