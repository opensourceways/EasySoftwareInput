package com.easysoftwareinput.domain.crawldown.model;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

import com.easysoftwareinput.common.config.MyPropertySourceFactory;

import lombok.Getter;
import lombok.Setter;

@Component
@Setter
@Getter
@PropertySource(value = "crawl-rpm.yml", factory = MyPropertySourceFactory.class)
@ConfigurationProperties(prefix = "rpm")
public class RpmCrawlConfig {
    /**
     * list of RpmCrawlEntity.
     */
    private List<RpmCrawlEntity> list;
}
