package com.easysoftwareinput.domain.appver;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;

@Component
@ConfigurationProperties(prefix = "appver")
@Getter
@Setter
public class RpmVerMonitorAliasConfig {
    /**
     * rpm monitor alias list.
     */
    private List<RpmVerMonitorAlias> rpmMonitorAliasList;
}
