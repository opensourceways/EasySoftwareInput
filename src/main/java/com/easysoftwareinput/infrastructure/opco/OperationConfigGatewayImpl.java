package com.easysoftwareinput.infrastructure.opco;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.easysoftwareinput.domain.operationconfig.ability.OpCoConverter;
import com.easysoftwareinput.domain.operationconfig.model.OpCo;
import com.easysoftwareinput.infrastructure.mapper.OpCoDoMapper;
import com.easysoftwareinput.infrastructure.opco.dataobject.OpCoDo;

@Component
public class OperationConfigGatewayImpl extends ServiceImpl<OpCoDoMapper, OpCoDo> {
    /**
     * mapper.
     */
    @Autowired
    private OpCoDoMapper mapper;

    /**
     * update data.
     *
     * @param list
     * @return boolean.
     */
    public boolean updateAll(List<OpCo> list) {
        if (list == null || list.size() == 0) {
            return true;
        }
        List<OpCoDo> dList = OpCoConverter.toDo(list);
        deleteByType(dList);
        return saveBatch(dList);
    }

    /**
     * delelte by type.
     *
     * @param dList list of data object.
     */
    private void deleteByType(List<OpCoDo> dList) {
        Set<String> types = dList.stream().map(OpCoDo::getType).collect(Collectors.toSet());
        QueryWrapper<OpCoDo> wrapper = new QueryWrapper<>();
        wrapper.in("type", types);
        mapper.delete(wrapper);
    }
}
