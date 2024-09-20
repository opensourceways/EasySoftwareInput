package com.easysoftwareinput.domain.oepkg.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OepkgMaintainer {
    /**
     * pkg name.
     */
    private String pkgName;

    /**
     * gitee id.
     */
    private String giteeId;

    /**
     * id.
     */
    private String id;

    /**
     * email.
     */
    private String email;

    /**
     * category.
     */
    private String category;
}
