package com.easysoftwareinput.domain.crawldown.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RpmCrawlEntity {
    /**
     * os.
     */
    private String os;

    /**
     * download url.
     */
    private String url;

    /**
     * target file.
     */
    private String target;

    /**
     * local path.
     */
    private String dir;
}
