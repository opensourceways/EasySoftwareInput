package com.easysoftwareinput.infrastructure.domainpackage;

import com.baomidou.mybatisplus.annotation.TableName;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("domain_package")
public class DomainPkgDO {
    private String os;
    private String arch;
    private String name;
    private String version;
    private String category;
    private String iconUrl;
    private String tags;
    private String pkgIds;
    private String description;
}
