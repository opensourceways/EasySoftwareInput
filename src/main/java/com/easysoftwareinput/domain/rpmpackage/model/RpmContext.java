package com.easysoftwareinput.domain.rpmpackage.model;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

import org.dom4j.Document;

import com.easysoftwareinput.infrastructure.BasePackageDO;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RpmContext {
    /**
     * doc.
     */
    private Document doc;

    /**
     * osmes.
     */
    private Map<String, String> osMes = Collections.emptyMap();

    /**
     * file index.
     */
    private int fileIndex;

    /**
     * src urls.
     */
    private Map<String, String> srcUrls = Collections.emptyMap();

    /**
     * maintainers.
     */
    private Map<String, BasePackageDO> maintainers = Collections.emptyMap();

    /**
     * count.
     */
    private AtomicLong count;

    /**
     * existed pkg id set.
     */
    private Set<String> existedPkgIdSet = Collections.emptySet();

    /**
     * repo names.
     */
    private Set<String> repoNames = Collections.emptySet();
}
