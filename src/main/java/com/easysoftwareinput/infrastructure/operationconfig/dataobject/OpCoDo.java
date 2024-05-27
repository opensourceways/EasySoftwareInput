package com.easysoftwareinput.infrastructure.operationconfig.dataobject;

import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;

@Data
@TableName("operation_config")
public class OpCoDo {
    private String categorys;
    private String orderIndex;
    private String recommend;
    private String type;
}
