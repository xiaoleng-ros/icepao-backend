package com.leng.ice.common;

import lombok.Getter;

/**
 * 错误码
 * @author 14125
 */
@Getter
public enum ErrorCode {


    SUCCESS(0,"ok",""),
    PARAMS_ERROR(40000,"请求参数错误",""),
    NULL_ERROR(40001,"请求数据为空",""),
    LOGIN_ERROR(40100,"未登录",""),
    NO_AUTH(40101,"内部参数错误",""),
    NO_ADMIN(40102,"权限不足",""),
    SYSTEM_ERROR(50000,"系统内部异常","");

    /**
     * 状态码
     */
    private final int code;

    /**
     * 状态码信息
     */
    private final String message;

    /**
     * 状态码详情描述
     */
    private final String description;

    /**
     * 构造函数
     * @param code
     * @param message
     */
    ErrorCode(int code, String message, String description) {
        this.code = code;
        this.message = message;
        this.description = description;
    }

}
