package com.easysoftwareinput.infrastructure.mapper;

import org.springframework.stereotype.Component;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.easysoftwareinput.infrastructure.apppkg.dataobject.AppDo;

@Component("appmapper")
public interface AppDoMapper extends BaseMapper<AppDo> {
    
}
