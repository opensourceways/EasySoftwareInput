package com.easysoftwareinput.domain.rpmpackage.model;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;

@Component
@ConfigurationProperties(prefix = "rpm")
@Getter
@Setter
public class RpmConfig {
    /**
     * local path.
     */
    private String dir;

    /**
     * official domain.
     */
    private String official;

    /**
     * remote gitee repo.
     */
    private String remoteRepo;
}
