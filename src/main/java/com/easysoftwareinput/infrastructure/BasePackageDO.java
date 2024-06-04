package com.easysoftwareinput.infrastructure;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;

@Data
@TableName("base_package_info")
public class BasePackageDO {
    /**
     * name of pkg.
     */
    @TableId
    private String name;

    /**
     * category of pkg.
     */
    private String category;

    /**
     * maintianer of pkg.
     */
    private String maintainerId;

    /**
     * maintainer email of pkg.
     */
    private String maintainerEmail;

    /**
     * maintainer gitee id of pkg.
     */
    private String maintainerGiteeId;

    /**
     * maintianer update at of pkg.
     */
    private String maintainerUpdateAt;

    /**
     * download count of pkg.
     */
    private String downloadCount;
}
