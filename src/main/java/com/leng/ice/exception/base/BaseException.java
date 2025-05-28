package com.leng.ice.exception.base;


import com.leng.ice.utils.MessageUtils;
import com.leng.ice.utils.StringUtils;
import lombok.Getter;

/**
 * 基础异常
 * 
 * @author boostor
 */
@Getter
public class BaseException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * 所属模块
     */
    private String module;

    /**
     * 错误码
     */
    private String code;

    /**
     * 错误码对应的参数
     */
    private Object[] args;

    /**
     * 错误消息
     */
    private String defaultMessage;

    /**
     * 构造函数
     * @param module 模块
     * @param code 错误码
     * @param args 错误码对应的参数
     * @param defaultMessage 默认信息
     */
    public BaseException(String module, String code, Object[] args, String defaultMessage) {
        this.module = module;
        this.code = code;
        this.args = args;
        this.defaultMessage = defaultMessage;
    }

    /**
     * 构造函数
     * @param module 模块
     * @param code 错误码
     * @param args 错误码对应的参数
     */
    public BaseException(String module, String code, Object[] args) {
        this(module, code, args, null);
    }

    /**
     * 构造函数
     * @param module
     * @param defaultMessage
     */
    public BaseException(String module, String defaultMessage) {
        this(module, null, null, defaultMessage);
    }

    /**
     * 构造函数
     * @param code 错误码
     * @param args 错误码对应的参数
     */
    public BaseException(String code, Object[] args) {
        this(null, code, args, null);
    }

    /**
     * 构造函数
     * @param defaultMessage
     */
    public BaseException(String defaultMessage) {
        this(null, null, null, defaultMessage);
    }

    /**
     * 获取错误码
     */
    @Override
    public String getMessage()
    {
        String message = null;
        if (!StringUtils.isEmpty(code))
        {
            message = MessageUtils.message(code, args);
        }
        if (message == null)
        {
            message = defaultMessage;
        }
        return message;
    }

}
