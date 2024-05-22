package com.easysoftwareinput.infrastructure.apppkg.dataobject;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serial;
import java.sql.Timestamp;

@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("application_package")
public class AppDo {
    /**
     * Serializable class with a defined serial version UID.
     */
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * Description of the entity.
     */
    private String description;

    /**
     * Name of the entity.
     */
    private String name;

    /**
     * License information.
     */
    private String license;

    /**
     * Download link or location.
     */
    private String download;

    /**
     * Category of the entity.
     */
    private String category;

    /**
     * Environment details.
     */
    private String environment;

    /**
     * Installation instructions.
     */
    private String installation;

    /**
     * Similar packages related to this entity.
     */
    private String similarPkgs;

    /**
     * Dependency packages required.
     */
    private String dependencyPkgs;

    /**
     * ID of the entity.
     */
    private String id;

    /**
     * Timestamp for creation.
     */
    private Timestamp createAt;

    /**
     * Timestamp for last update.
     */
    private Timestamp updateAt;

    /**
     * Type of entity.
     */
    private String type;

    /**
     * URL for the icon.
     */
    private String iconUrl;

    /**
     * Version of the application.
     */
    private String appVer;

    /**
     * Supported operating systems.
     */
    private String osSupport;

    /**
     * Operating system information.
     */
    private String os;

    /**
     * Architecture details.
     */
    private String arch;

    /**
     * Maintainer's ID.
     */
    private String maintainerId;

    /**
     * Maintainer's email address.
     */
    private String maintainerEmail;

    /**
     * Maintainer's Gitee ID.
     */
    private String maintainerGiteeId;

    /**
     * Timestamp for last maintainer update.
     */
    private String maintainerUpdateAt;

    /**
     * Security level information.
     */
    private String securityLevel;

    /**
     * Safety label information.
     */
    private String safeLabel;

    /**
     * Download count information.
     */
    private String downloadCount;

    /**
     * Size of the application package.
     */
    private String appSize;

    /**
     * Source repository information.
     */
    private String srcRepo;

    /**
     * Source download URL.
     */
    private String srcDownloadUrl;

    /**
     * Binary download URL.
     */
    private String binDownloadUrl;

    /**
     * Package ID.
     */
    @TableId
    private String pkgId;

    /**
     * Tags associated with images.
     */
    private String imageTags;

    /**
     * Usage information for images.
     */
    private String imageUsage;

    private String latestOsSupport;

}

