package com.easysoftwareinput.domain.appver;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;

@Component
@Getter
@Setter
@ConfigurationProperties(prefix = "appver")
public class AppVerConfig {
    /**
     * app url.
     */
    private String appurl;

    /**
     * monitor url.
     */
    private String monurl;

    /**
     * rpm txt.
     */
    private String rpmTxt;

    /**
     * rpm euler.
     */
    private String rpmEuler;

    /**
     * rpm alias.
     */
    private String rpmAlias;
}
