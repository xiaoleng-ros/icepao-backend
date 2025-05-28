package com.leng.ice.model.entity;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

import java.util.Date;

/**
 * 消息封装类
 */
@Data
public class Message {

    /**
     * 发送者账号
     */
    private String userAccount;

    /**
     * 接收者账号
     */
    private String consumer;

    /**
     * 消息类型
     */
    private String type;

    /**
     * 消息内容
     */
    private String content;

    /**
     * 头像
     */
    private String userAvatar;

    /**
     * 发送时间
     */
    @JSONField(format="yyyy-MM-dd HH:mm:ss")
    public Date date;

}
