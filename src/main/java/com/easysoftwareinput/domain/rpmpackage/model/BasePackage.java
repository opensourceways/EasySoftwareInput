package com.easysoftwareinput.domain.rpmpackage.model;

import com.easysoftwareinput.domain.apppackage.model.AppPackage;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BasePackage {
    private String name;

    private String category;

    private String maintainerId;

    private String maintainerEmail;

    private String maintainerGiteeId;

    private String maintainerUpdateAt;

    private String downloadCount;

    public BasePackage(BasePackage other) {  
        
        this.name = other.name;  
        this.category = other.category;  
        this.maintainerId = other.maintainerId;  
        this.maintainerEmail = other.maintainerEmail;  
        this.maintainerGiteeId = other.maintainerGiteeId;  
        this.maintainerUpdateAt = other.maintainerUpdateAt;  
        this.downloadCount = other.downloadCount;  
    }

}
