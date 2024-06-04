package com.easysoftwareinput.domain.rpmpackage.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BasePackage {
    /**
     * name of pkg.
     */
    private String name;

    /**
     * category of pkg.
     */
    private String category;

    /**
     * maintainerId of pkg.
     */
    private String maintainerId;

    /**
     * maintainerEmail of pkg.
     */
    private String maintainerEmail;

    /**
     * maintainerGiteeId of pkg.
     */
    private String maintainerGiteeId;

    /**
     * maintainerUpdateAt of pkg.
     */
    private String maintainerUpdateAt;

    /**
     * downloadCount of pkg.
     */
    private String downloadCount;
}
