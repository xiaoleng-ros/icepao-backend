package com.leng.ice.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 帖子
 * @TableName post
 */
@Data
public class PostVO implements Serializable {

    /**
     * 帖子id
     */
    private Long id;

    /**
     * 内容
     */
    private String content;

    /**
     * 用户id
     */
    private Long userId;

    /**
     * 用户昵称
     */
    private String username;

    /**
     * 头像
     */
    private String avatarUrl;

    /**
     * 状态 0 正常
     */
    private Integer postState;

    /**
     * 点赞数
     */
    private Integer thumbNum;

    /**
     * 当前用户是否点赞
     */
    private boolean isThumb;

    /**
     * 评论数量（动态计算，不存储在数据库）
     */
    private Integer commentCount;

    /**
     * 帖子的评论
     */
    private List<PostCommentVO> postCommentList;

}
