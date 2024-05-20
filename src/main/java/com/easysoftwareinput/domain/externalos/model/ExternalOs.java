package com.easysoftwareinput.domain.externalos.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExternalOs {
    private String originOsName;
    private String originOsVer;
    private String originPkg;
    private String targetOsName;
    private String targetOsVer;
    private String targetPkg;
}
