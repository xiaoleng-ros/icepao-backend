package com.leng.ice.model.request;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.io.Serializable;

/**
 * 帖子更新封装类
 */
@Data
public class PostUpdateRequest implements Serializable {

    /**
     * 帖子id
     */
    private Long id;

    /**
     * 内容
     */
    private String content;

}
