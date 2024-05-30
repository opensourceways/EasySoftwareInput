package com.easysoftwareinput.infrastructure.operationconfig;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.easysoftwareinput.domain.operationconfig.model.OpCo;
import com.easysoftwareinput.infrastructure.mapper.OpCoDoMapper;
import com.easysoftwareinput.infrastructure.operationconfig.converter.OpCoConverter;
import com.easysoftwareinput.infrastructure.operationconfig.dataobject.OpCoDo;

import co.elastic.clients.elasticsearch._types.query_dsl.Query;

@Component
public class OpCoGatewayImpl extends ServiceImpl<OpCoDoMapper, OpCoDo>  {
    @Autowired
    OpCoDoMapper mapper;

    public boolean saveAll(List<OpCo> opCos) {
        if (opCos.size() == 0) {
            return true;
        }

        List<OpCoDo> dList = OpCoConverter.toDo(opCos);
        boolean d = deleteByType(dList);
        if (! d) {
            return false;
        }

        return saveBatch(dList);
    }

    public boolean deleteByType(List<OpCoDo> dList) {
        if (dList.size() == 0) {
            return true;
        }

        String type = dList.get(0).getType();
        QueryWrapper<OpCoDo> wrapper = new QueryWrapper<>();
        wrapper.eq("type", type);
        long l = mapper.selectCount(wrapper);
        long d = (long) mapper.delete(wrapper);
        return l == d;
    }
}
