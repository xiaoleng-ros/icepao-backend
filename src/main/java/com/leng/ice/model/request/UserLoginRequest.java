package com.leng.ice.model.request;

import lombok.Data;

import java.io.Serializable;

/**
 * 用户登录请求体
 */
@Data
public class UserLoginRequest implements Serializable {

    /**
     * 序列化
     */
    private static final long serialVersionUID = -5602452186149132432L;

    /**
     * 账号
     */
    String userAccount;

    /**
     * 密码
     */
    String userPassword;
}
