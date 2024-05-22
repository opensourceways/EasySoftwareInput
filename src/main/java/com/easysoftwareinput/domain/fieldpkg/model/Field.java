package com.easysoftwareinput.domain.fieldpkg.model;

import java.io.Serial;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
public class Field {
        /**
     * Serializable class with a defined serial version UID.
     */
    @Serial
    private String os;

    /**
     * Architecture information.
     */
    private String arch;

    /**
     * Name of the entity.
     */
    private String name;

    /**
     * Version information.
     */
    private String version;

    /**
     * Category of the entity.
     */
    private String category;

    /**
     * URL for the icon.
     */
    private String iconUrl;

    /**
     * Tags associated with the entity.
     */
    private Set<String> tags;

    /**
     * Package IDs related to the entity.
     */
    private Map<String, String> pkgIds;

    /**
     * Description of the entity.
     */
    private String description;

    public Field() {
        this.tags = new HashSet<>();
        this.pkgIds = new HashMap<>();
    }
}
