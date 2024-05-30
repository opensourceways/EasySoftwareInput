package com.easysoftwareinput.infrastructure.fieldpkg.dataobject;

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
// TODO 修改表名
@TableName("field_package")
public class FieldDo {
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
    private String tags;

    /**
     * Package IDs related to the entity.
     */
    @TableId(value = "pkg_ids")
    private String pkgIds;

    /**
     * Description of the entity.
     */
    private String description;

    private Timestamp updateAt;

}
