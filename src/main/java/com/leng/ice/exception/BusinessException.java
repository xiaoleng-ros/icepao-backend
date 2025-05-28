package com.leng.ice.exception;

import com.leng.ice.common.ErrorCode;
import lombok.Getter;

/**
 * 自定义异常
 *
 * @author leng
 */
@Getter
public class BusinessException extends RuntimeException {

    /**
     * 错误码
     */
    private final int code;

    /**
     * 错误提示
     */
    private final String description;

    /**
     * 构造函数
     * @param message
     * @param code
     * @param description
     */
    public BusinessException(String message,int code, String description) {
        super(message);
        this.code = code;
        this.description = description;
    }

    /**
     * 构造函数
     * @param errorCode
     */
    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.code = errorCode.getCode();
        this.description = errorCode.getDescription();
    }

    /**
     * 构造函数
     * @param errorCode
     * @param description
     */
    public BusinessException(ErrorCode errorCode,String description) {
        super(errorCode.getMessage());
        this.code = errorCode.getCode();
        this.description = description;
    }

}
