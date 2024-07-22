package com.easysoftwareinput.domain.maintainer;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "maintainer")
public class MaintainerConfig {
    /**
     * id.
     */
    private String id;

    /**
     * email.
     */
    private String email;

    /**
     * gitee id.
     */
    private String giteeId;
}
