package com.easysoftwareinput.domain.apppackage.model;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;

@Component
@Getter
@Setter
@ConfigurationProperties(prefix = "app")
public class AppConfig {
    /**
     * remote repo.
     */
    private String remoteRepo;
}
