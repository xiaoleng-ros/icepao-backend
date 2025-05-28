package com.leng.ice.model.request;

import lombok.Data;

/**
 * 帖子点赞请求
 *
 */
@Data
public class PostThumbAddRequest {

    /**
     * 帖子 id
     */
    private Long postId;

}
