package com.easysoftwareinput.common.entity;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ResultVo {
    /**
     * code.
     */
    private int code;

    /**
     * msg.
     */
    private Object msg;

    /**
     * data.
     */
    private Object data;

    /**
     * error.
     */
    private String error;
}
