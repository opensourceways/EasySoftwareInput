package com.easysoftwareinput.infrastructure.archnum;

import java.sql.Timestamp;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@TableName("arch_num")
public class OsArchNumDO {
    /**
     * os.
     */
    private String os;

    /**
     * type.
     */
    private String type;

    /**
     * arch.
     */
    private String archName;

    /**
     * count.
     */
    private int count;

    /**
     * pkgId.
     */
    @TableId(value = "pkg_id")
    private String pkgId;

    /**
     * update at.
     */
    private Timestamp updateAt;

    /**
     * get created time of pkg.
     * @return created time of pkg.
     */
    public Timestamp getUpdateAt() {
        if (this.updateAt == null) {
            return null;
        }
        return (Timestamp) this.updateAt.clone();
    }

    /**
     * set updated time of pkg.
     * @param stamp updated time of pkg.
     */
    public void setUpdateAt(Timestamp stamp) {
        if (stamp != null) {
            this.updateAt = (Timestamp) stamp.clone();
        }
    }
}
