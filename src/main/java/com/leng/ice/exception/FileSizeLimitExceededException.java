package com.leng.ice.exception;

/**
 * 文件名大小限制异常类
 * 
 * @author boostor
 */
public class FileSizeLimitExceededException extends FileException {
    private static final long serialVersionUID = 1L;

    /**
     * 构造函数
     *
     * @param defaultMaxSize
     * 允许的最大文件大小
     */
    public FileSizeLimitExceededException(long defaultMaxSize) {
        super("upload.exceed.maxSize", new Object[] { defaultMaxSize });
    }
}
