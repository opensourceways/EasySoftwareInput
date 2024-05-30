package com.easysoftwareinput.infrastructure.mapper;

import java.util.List;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

public interface EasyBaseMapper<T> extends BaseMapper<T> {
    Integer insertBatchSomeColumn(List<T> entityList);
}
