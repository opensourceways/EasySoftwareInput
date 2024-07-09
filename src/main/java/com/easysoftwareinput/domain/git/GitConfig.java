package com.easysoftwareinput.domain.git;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;

@Component
@ConfigurationProperties(prefix = "app-git")
@Getter
@Setter
public class GitConfig {
    /**
     * remotePath.
     */
    private String remotePath;

    /**
     * localPath.
     */
    private String localPath;

    /**
     * userName.
     */
    private String userName;

    /**
     * password.
     */
    private String password;

    /**
     * branch.
     */
    private String branch;
}
