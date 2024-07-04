package com.easysoftwareinput.infrastructure.rpmpkg;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.IService;

public interface Gateway<T> extends IService<T> {
    /**
     * get logger.
     * @return logger.
     */
    Logger getLogger();

    /**
     * save or update the data.
     * @param map save the unexisted, update the existed.
     * @return boolean.
     */
    default boolean saveAndUpdate(Map<Boolean, List<T>> map) {
        List<T> unexisted = map.get(false);
        List<T> existed = map.get(true);
        boolean inserted = false;
        try {
            saveBatch(unexisted);
        } catch (Exception e) {
            getLogger().error("fail-to-write, e: {}", e);
        }

        boolean updated = false;
        try {
            updated = updateBatchById(existed);
        } catch (Exception e) {
            getLogger().error("fail-to-update, e: {}", e);
        }

        return inserted && updated;
    }

    /**
     * get existed pkgids from table.
     * @param column column.
     * @return set of pkgids.
     */
    default List<T> getExistedIds(String column) {
        QueryWrapper<T> wrapper = new QueryWrapper<>();
        wrapper.select("distinct " + column);
        return list(wrapper);
    }

    /**
     * get length of data row from table.
     * @param startTime startTime.
     * @return length of data.
     */
    default long getChangedRow(long startTime) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String time = sdf.format(startTime);
        QueryWrapper<T> wrapper = new QueryWrapper<>();
        wrapper.ge("update_at", time);
        return count(wrapper);
    }
}
