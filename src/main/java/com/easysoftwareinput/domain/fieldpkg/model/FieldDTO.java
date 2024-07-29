package com.easysoftwareinput.domain.fieldpkg.model;

import com.easysoftwareinput.infrastructure.rpmpkg.IDataObject;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FieldDTO {
    /**
     * os.
     */
    private String os;

    /**
     * name.
     */
    private String name;

    /**
     * arch.
     */
    private String arch;

    /**
     * pkg.
     */
    private IDataObject pkg;
}
