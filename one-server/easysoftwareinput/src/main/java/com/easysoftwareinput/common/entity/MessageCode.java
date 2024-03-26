package com.easysoftwareinput.common.entity;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

public enum MessageCode {
    // client exception
    EC0001("EC0001", "Request Error", "请求异常"),
    EC0002("EC0002", "Wrong parameter", "参数错误"),
    EC0003("EC0003", "Unsupported community", "不支持该community"),
    EC0004("EC0004", "Update failed", "更新失败"),
    EC0005("EC0005", "Delete failed", "删除失败"),
    EC0006("EC0006", "Insert failed", "插入失败"),
    EC0007("EC0007", "query failed", "查找失败"),
    EC0008("EC0008", "Item existed", "项目已存在"),
    EC0009("EC0009", "Item not existed", "项目不存在"),
    EC00010("EC00010", "Request exceeds the limit", "请求超过限制"),
    EC00011("EC00011", "Failed to retrieve field using reflection", "无法通过反射获取字段"),
    EC00012("EC00012", "Unauthorized", "身份认证失败"),
    EC00013("EC00013", "Unable to connect to database", "无法连接数据库"),
    EC00014("EC00014", "Json Exception", "无法解析json字符串"),
    EC00015("EC00015", "Gitee Connection Exception", "无法调用Gitee Api"),
    EC00016("EC00016", "Read /repodata/primary.xml failed", "读取/repodata/primary.xml失败"),
    EC00017("EC00017", "thread interrupted exception", "线程中断异常"),

    // self service exception
    ES0001("ES0001", "Internal Server Error", "服务异常"),
    ES0002("ES0002", "batch save to sql happens error", "批数据写入异常"),
    ES0003("ES0003", "convert entity error", "rpmpkg数据实体转换发生异常");
    private final String code;
    private final String msgEn;
    private final String msgZh;

    MessageCode(String code, String msgEn, String msgZh) {
        this.code = code;
        this.msgEn = msgEn;
        this.msgZh = msgZh;
    }

    public String getCode() {
        return code;
    }

    public String getMsgEn() {
        return msgEn;
    }

    public String getMsgZh() {
        return msgZh;
    }

    public static final Map<String, MessageCode> msgCodeMap = Arrays.stream(MessageCode.values())
            .collect(Collectors.toMap(MessageCode::getCode, e -> e));
}
