package com.easysoftwareinput.domain.apppackage.model;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;

@Component
@ConfigurationProperties(prefix = "app")
@Getter
@Setter
public class OsTagMonitorAliasConfig {
    /**
     * app monitor alias list.
     */
    private List<OsTagMonitorAlias> appMonitorOsAliasList;
}
