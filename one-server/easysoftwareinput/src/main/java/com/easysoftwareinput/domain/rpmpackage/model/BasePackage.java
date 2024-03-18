package com.easysoftwareinput.domain.rpmpackage.model;

import lombok.Data;

@Data
public class BasePackage {
    private String name;

    private String category;

    private String maintainerId;

    private String maintainerEmail;

    private String maintainerGiteeId;

    private String maintainerUpdateAt;

    private String downloadCount;

}
