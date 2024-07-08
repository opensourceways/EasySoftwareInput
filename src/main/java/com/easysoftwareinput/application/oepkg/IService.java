package com.easysoftwareinput.application.oepkg;

import org.slf4j.Logger;

import com.easysoftwareinput.infrastructure.rpmpkg.Gateway;

public interface IService {
    /**
     * get gateway.
     * @return gateway.
     */
    Gateway getCurrentGateway();

    /**
     * get start time.
     * @return start time.
     */
    long getStartTime();

    /**
     * get count.
     * @return count.
     */
    long getCount();

    /**
     * get logger.
     * @return logger.
     */
    Logger getLogger();

    /**
     * valid data.
     * @param <T> generic type.
     * @return boolean.
     */
    default <T> boolean validData() {
        long tableRow = getCurrentGateway().getChangedRow(getStartTime());
        long updatedRow = getCount();
        if (tableRow == updatedRow) {
            getLogger().info("no error, need to be stored: {}, stored: {}", updatedRow, tableRow);
            return true;
        } else {
            getLogger().error("error, need to be stored: {}, stored: {}", updatedRow, tableRow);
            return false;
        }
    }
}
