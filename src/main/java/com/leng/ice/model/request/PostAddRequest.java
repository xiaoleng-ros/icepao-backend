package com.leng.ice.model.request;

import lombok.Data;

import java.io.Serializable;

/**
 * 帖子添加请求体
 */
@Data
public class PostAddRequest implements Serializable {

    /**
     * 内容
     */
    private String content;

}
