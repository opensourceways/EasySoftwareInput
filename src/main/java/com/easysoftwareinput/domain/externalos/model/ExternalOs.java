package com.easysoftwareinput.domain.externalos.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExternalOs {
    /**
     * origin os name.
     */
    private String originOsName;

    /**
     * origin os version.
     */
    private String originOsVer;

    /**
     * origin pkg name.
     */
    private String originPkg;

    /**
     * target os name.
     */
    private String targetOsName;

    /**
     * target os version.
     */
    private String targetOsVer;

    /**
     * target pkg name.
     */
    private String targetPkg;
}
