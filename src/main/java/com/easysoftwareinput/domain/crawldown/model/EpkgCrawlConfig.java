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
@PropertySource(value = "classpath:crawl-rpm.yml", factory = MyPropertySourceFactory.class)
@ConfigurationProperties(prefix = "epkg")
public class EpkgCrawlConfig {
    /**
     * list of RpmCrawlEntity.
     */
    private List<RpmCrawlEntity> list;
}
