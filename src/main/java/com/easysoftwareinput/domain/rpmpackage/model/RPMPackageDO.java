package com.easysoftwareinput.domain.rpmpackage.model;

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
@TableName("rpm_pkg_base")
public class RPMPackageDO {
    /**
     * Serializable class with a defined serial version UID.
     */
    @Serial
    private String id;

    /**
     * Timestamp for creation.
     */
    private Timestamp createAt;

    /**
     * Timestamp for update.
     */
    private Timestamp updateAt;

    /**
     * Name of the entity.
     */
    private String name;

    /**
     * Version information.
     */
    private String version;

    /**
     * Operating system information.
     */
    private String os;

    /**
     * Architecture information.
     */
    private String arch;

    /**
     * Category of the entity.
     */
    private String category;

    /**
     * Timestamp for RPM package update.
     */
    private String rpmUpdateAt;

    /**
     * Source repository information.
     */
    private String srcRepo;

    /**
     * Size of the RPM package.
     */
    private String rpmSize;

    /**
     * Binary download URL.
     */
    private String binDownloadUrl;

    /**
     * Source download URL.
     */
    private String srcDownloadUrl;

    /**
     * Summary of the entity.
     */
    private String summary;

    /**
     * Operating system support information.
     */
    private String osSupport;

    /**
     * Repository information.
     */
    private String repo;

    /**
     * Type of the repository.
     */
    private String repoType;

    /**
     * Installation instructions.
     */
    private String installation;

    /**
     * Description of the entity.
     */
    private String description;

    /**
     * Requirements information.
     */
    private String requires;


    /**
     * Provides information.
     */
    private String provides;

    /**
     * Conflicts information.
     */
    private String conflicts;

    /**
     * Change log information.
     */
    private String changeLog;

    /**
     * Maintainer ID.
     */
    private String maintainerId;

    /**
     * Maintainer email.
     */
    private String maintainerEmail;

    /**
     * Maintainer Gitee ID.
     */
    private String maintainerGiteeId;

    /**
     * Timestamp for maintainer update.
     */
    private String maintainerUpdateAt;

    /**
     * Maintainer status information.
     */
    private String maintainerStatus;

    /**
     * Upstream information.
     */
    private String upStream;

    /**
     * Security information.
     */
    private String security;

    /**
     * Similar packages information.
     */
    private String similarPkgs;

    /**
     * Download count information.
     */
    private String downloadCount;

    /**
     * Package ID.
     */
    @TableId
    private String pkgId;

    /**
     * Sub-path information.
     */
    private String subPath;

}
