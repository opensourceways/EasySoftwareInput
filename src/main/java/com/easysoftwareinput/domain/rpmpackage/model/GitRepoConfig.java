package com.easysoftwareinput.domain.rpmpackage.model;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;

@Component
@ConfigurationProperties(prefix = "git-repo")
@Getter
@Setter
public class GitRepoConfig {
    /**
     * org-template.
     */
    private String orgTemplate;

    /**
     * token.
     */
    private String token;

    /**
     * per page.
     */
    private String perPage;

    /**
     * page interval.
     */
    private int pageInterval;
}
