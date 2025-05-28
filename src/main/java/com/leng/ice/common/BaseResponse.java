package com.leng.ice.common;

import lombok.Data;

import java.io.Serializable;

/**
 * 异常返回类
 * @param <T>
 */
@Data
public class BaseResponse<T> implements Serializable {

    /**
     * 业务状态码
     */
    private int code;

    /**
     * 数据
     */
    private T data;

    /**
     * 信息
     */
    private String message;

    /**
     * 描述
     */
    private String description;

    /**
     *
     * @param code
     * @param data
     * @param message
     * @param description
     */
    public BaseResponse(int code, T data, String message,String description) {
        this.code = code;
        this.data = data;
        this.message = message;
        this.description = description;
    }

    /**
     *
     * @param code
     * @param data
     * @param message
     */
    public BaseResponse(int code, T data,String message) {
        this(code,data,message,"");
    }

    /**
     *
     * @param code
     * @param data
     */
    public BaseResponse(int code, T data) {
        this(code,data,"","");
    }

    /**
     *
     * @param errorCode
     */
    public BaseResponse(ErrorCode errorCode){
        this(errorCode.getCode(),null,errorCode.getMessage(),errorCode.getDescription());
    }
}
