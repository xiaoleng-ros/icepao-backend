package com.leng.ice.model.request;

import lombok.Data;

import java.io.Serializable;

/**
 * 用户注册请求体
 */
@Data
public class UserRegisterRequest implements Serializable {

    /**
     * 序列化
     */
    private static final long serialVersionUID = -5602452186149132432L;

    /**
     * 昵称
     */
    String username;

    /**
     * 账号
     */
    String userAccount;

    /**
     * 密码
     */
    String userPassword;

    /**
     * 校验密码
     */
    String checkPassword;
}
