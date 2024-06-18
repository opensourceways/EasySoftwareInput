package com.easysoftwareinput.infrastructure.opco.dataobject;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;

@Data
@TableName("operation_config")
public class OpCoDo {
    /**
     * id.
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;
    /**
     * category.
     */
    private String categorys;
    /**
     * order of current object.
     */
    private String orderIndex;

    /**
     * recommend pkgs.
     */
    private String recommend;

    /**
     * config type.
     */
    private String type;
}
