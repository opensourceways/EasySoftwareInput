/* Copyright (c) 2024 openEuler Community
 EasySoftwareInput is licensed under the Mulan PSL v2.
 You can use this software according to the terms and conditions of the Mulan PSL v2.
 You may obtain a copy of Mulan PSL v2 at:
     http://license.coscl.org.cn/MulanPSL2
 THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
 EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
 MERCHANTABILITY OR FIT FOR A PARTICULAR PURPOSE.
 See the Mulan PSL v2 for more details.
*/

package com.easysoftwareinput.common.entity;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

public enum MessageCode {
    /**
     * Request Error.
     */
    EC0001("EC0001", "Request Error", "请求异常"),

    /**
     * Wrong parameter.
     */
    EC0002("EC0002", "Wrong parameter", "参数错误"),

    /**
     * Unsupported community.
     */
    EC0003("EC0003", "Unsupported community", "不支持该community"),

    /**
     * Update failed.
     */
    EC0004("EC0004", "Update failed", "更新失败"),

    /**
     * Delete failed.
     */
    EC0005("EC0005", "Delete failed", "删除失败"),

    /**
     * Insert failed.
     */
    EC0006("EC0006", "Insert failed", "插入失败"),

    /**
     * query failed.
     */
    EC0007("EC0007", "query failed", "查找失败"),

    /**
     * Item existed.
     */
    EC0008("EC0008", "Item existed", "项目已存在"),

    /**
     * Item not existed.
     */
    EC0009("EC0009", "Item not existed", "项目不存在"),

    /**
     * Request exceeds the limit.
     */
    EC00010("EC00010", "Request exceeds the limit", "请求超过限制"),

    /**
     * Failed to retrieve field using reflection.
     */
    EC00011("EC00011", "Failed to retrieve field using reflection", "无法通过反射获取字段"),

    /**
     * Unauthorized.
     */
    EC00012("EC00012", "Unauthorized", "身份认证失败"),

    /**
     * Unable to connect to database.
     */
    EC00013("EC00013", "Unable to connect to database", "无法连接数据库"),

    /**
     * Json Exception.
     */
    EC00014("EC00014", "Json Exception", "无法解析json字符串"),

    /**
     * Gitee Connection Exception.
     */
    EC00015("EC00015", "Gitee Connection Exception", "无法调用Gitee Api"),

    /**
     * Read /repodata/primary.xml failed.
     */
    EC00016("EC00016", "Read /repodata/primary.xml failed", "读取/repodata/primary.xml失败"),

    /**
     * thread interrupted exception.
     */
    EC00017("EC00017", "thread interrupted exception", "线程中断异常"),

    /**
     * Internal Server Error.
     */
    ES0001("ES0001", "Internal Server Error", "服务异常"),

    /**
     * batch save to sql happens error.
     */
    ES0002("ES0002", "batch save to sql happens error", "批数据写入异常"),

    /**
     * convert entity error.
     */
    ES0003("ES0003", "convert entity error", "rpmpkg数据实体转换发生异常");

    /**
     * code.
     */
    private final String code;

    /**
     * msgEn.
     */
    private final String msgEn;

    /**
     * msgZh.
     */
    private final String msgZh;

    MessageCode(String code, String msgEn, String msgZh) {
        this.code = code;
        this.msgEn = msgEn;
        this.msgZh = msgZh;
    }

    /**
     * get code.
     * @return code.
     */
    public String getCode() {
        return code;
    }

    /**
     * get msgen.
     * @return msgen.
     */
    public String getMsgEn() {
        return msgEn;
    }

    /**
     * get msgzh.
     * @return msgzh.
     */
    public String getMsgZh() {
        return msgZh;
    }

    /**
     * msgCode map.
     */
    public static final Map<String, MessageCode> MSG_CODE_MAP = Arrays.stream(MessageCode.values())
            .collect(Collectors.toMap(MessageCode::getCode, e -> e));
}
