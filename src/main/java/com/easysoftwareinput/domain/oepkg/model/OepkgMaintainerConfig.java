package com.easysoftwareinput.domain.oepkg.model;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;

@Component
@ConfigurationProperties(prefix = "oepkg")
@Getter
@Setter
public class OepkgMaintainerConfig {
    /**
     * maintianer list.
     */
    private List<OepkgMaintainer> maintainerList;
}
