package com.easysoftwareinput.domain.domainpackage.model;

import java.util.Map;
import java.util.Set;

import com.baomidou.mybatisplus.annotation.TableName;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("domain_package")
public class DomainPackage {
    private String os;
    private String arch;
    private String name;
    private String version;
    private String category;
    private String iconUrl;
    private Set<String> tags;
    private Map<String, String> pkgIds;
    private String description;
}
