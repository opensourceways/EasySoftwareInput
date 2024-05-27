package com.easysoftwareinput.common.utils;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.easysoftwareinput.common.entity.MessageCode;
import com.power.common.util.StringUtil;


public class QueryWrapperUtil {
    private static final Logger logger = LoggerFactory.getLogger(QueryWrapperUtil.class);

    public static <T, U> QueryWrapper<T> createQueryWrapper(T t, U u) {
        QueryWrapper<T> wrapper = new QueryWrapper<>();
        
        Field[] fields = u.getClass().getDeclaredFields();
        for (Field field: fields) {
            field.setAccessible(true);

            Object value = null;
            try {
                value = field.get(u);
            } catch (Exception e) {
                logger.error(MessageCode.EC00011.getMsgEn(), e);
            }
            if (! (value instanceof String)) {
                continue;
            }

            String vStr = (String) value;
            vStr = StringUtils.trimToEmpty(vStr);
            if (StringUtils.isBlank(vStr)) {
                continue;
            }

            String undLine = StringUtil.camelToUnderline(field.getName());
            
            //","代表该字段有多个参数
            if (vStr.contains(",")) {
                List<String> items = splitStr(vStr);
                if (items.size() == 0) {
                    continue;
                }
                wrapper.in(undLine, items);
            } else {
                vStr = StringUtils.trimToEmpty(vStr);
                wrapper.eq(undLine, vStr);
            }
            
        }
        return wrapper;
    }

    private static List<String> splitStr(String vStr) {
        List<String> res = new ArrayList<>();

        String[] sps = StringUtils.split(vStr, ",");
        if (sps.length == 0) {
            return res;
        }

        for (String sp : sps) {
            if (StringUtils.isBlank(StringUtils.trimToEmpty(sp))) {
                continue;
            }
            res.add(sp);
        }
        return res;
    }
}
