package com.easysoftwareinput.infrastructure.mapper;

import java.util.List;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

public interface EasyBaseMapper<T> extends BaseMapper<T> {
    /**
     * insert batch.
     * @param entityList lsit of pkg.
     * @return the number of inserted.
     */
    Integer insertBatchSomeColumn(List<T> entityList);
}
