package com.easysoftwareinput.infrastructure;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;

@Data
@TableName("base_package_info")
public class BasePackageDO {
    
    @TableId
    private String name;
    
    private String category;
    
    private String maintainerId;
    
    private String maintainerEmail;
    
    private String maintainerGiteeId;
    
    private String maintainerUpdateAt;
    
    private String downloadCount;
    
}
