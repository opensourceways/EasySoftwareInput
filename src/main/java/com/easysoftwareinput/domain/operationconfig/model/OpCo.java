package com.easysoftwareinput.domain.operationconfig.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OpCo {
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
