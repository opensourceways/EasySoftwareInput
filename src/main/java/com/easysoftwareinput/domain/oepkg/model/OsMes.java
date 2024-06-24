package com.easysoftwareinput.domain.oepkg.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OsMes {
    /**
     * name of os.
     */
    private String osName;

    /**
     * version of os.
     */
    private String osVer;

    /**
     * type of os.
     */
    private String osType;

    /**
     * download url of os pkg.
     */
    private String baseUrl;
}
