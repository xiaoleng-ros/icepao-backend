package com.leng.ice.exception;


import com.leng.ice.exception.base.BaseException;

/**
 * 文件信息异常类
 * 
 * @author boostor
 */
public class FileException extends BaseException {

    private static final long serialVersionUID = 1L;

    /**
     * 构造函数
     * @param code 错误代码
     * @param args 错误参数
     */
    public FileException(String code, Object[] args) {
        super("file", code, args, null);
    }

}
