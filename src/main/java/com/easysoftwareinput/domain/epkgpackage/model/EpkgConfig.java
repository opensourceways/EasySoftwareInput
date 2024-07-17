package com.easysoftwareinput.domain.epkgpackage.model;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;

@Component
@ConfigurationProperties(prefix = "epkg")
@Getter
@Setter
public class EpkgConfig {
    /**
     * dir.
     */
    private String dir;

    /**
     * os name.
     */
    private String osName;

    /**
     * official.
     */
    private String official;

    /**
     * osver.
     */
    private String osVer;

    /**
     * base url.
     */
    private String baseUrl;

    /**
     * os type.
     */
    private String osType;
}
