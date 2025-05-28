package com.leng.ice.utils;

/**
 * 媒体类型工具类
 * 
 * @author boostor
 */
public class MimeTypeUtils {

    /**
     * 图片
     */
    public static final String IMAGE_PNG = "image/png";

    /**
     * 图片
     */
    public static final String IMAGE_JPG = "image/jpg";

    /**
     * 图片
     */
    public static final String IMAGE_JPEG = "image/jpeg";

    /**
     * 图片
     */
    public static final String IMAGE_BMP = "image/bmp";

    /**
     * 图片
     */
    public static final String IMAGE_GIF = "image/gif";

    /**
     * 图片
     */
    public static final String[] IMAGE_EXTENSION = { "bmp", "gif", "jpg", "jpeg", "png" };

    /**
     * 音视频
     */
    public static final String[] FLASH_EXTENSION = { "swf", "flv" };

    /**
     * 音视频
     */
    public static final String[] MEDIA_EXTENSION = { "swf", "flv", "mp3", "wav", "wma", "wmv", "mid", "avi", "mpg",
            "asf", "rm", "rmvb" };

    /**
     * 音视频
     */
    public static final String[] VIDEO_EXTENSION = { "mp4", "avi", "rmvb" };

    /**
     * 允许上传的文件后缀
     */
    public static final String[] DEFAULT_ALLOWED_EXTENSION = {
            // 图片
            "bmp", "gif", "jpg", "jpeg", "png",
            // word excel powerpoint
            "doc", "docx", "xls", "xlsx", "ppt", "pptx", "html", "htm", "txt",
            // 压缩文件
            "rar", "zip", "gz", "bz2",
            // 视频格式
            "mp4", "avi", "rmvb",
            // 日志下载
            "log",
            // pdf
            "pdf"};

    /**
     * 获取文件名的后缀
     *
     * @param prefix 媒体类型
     * @return 后缀名
     */
    public static String getExtension(String prefix) {
        switch (prefix)
        {
            case IMAGE_PNG:
                return "png";
            case IMAGE_JPG:
                return "jpg";
            case IMAGE_JPEG:
                return "jpeg";
            case IMAGE_BMP:
                return "bmp";
            case IMAGE_GIF:
                return "gif";
            default:
                return "";
        }
    }
}
