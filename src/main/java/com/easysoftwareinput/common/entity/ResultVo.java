package com.easysoftwareinput.common.entity;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ResultVo {
        private int code;
    private Object msg;
    private Object data;
    private String error;
}
