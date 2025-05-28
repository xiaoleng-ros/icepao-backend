package com.leng.ice.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 帖子
 * @TableName post
 */
@TableName(value ="post")
@Data
public class Post implements Serializable {

    /**
     * 帖子id
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户id
     */
    private Long userId;

    /**
     * 内容 大于10字小于600字
     */
    private String content;

    /**
     * 图片URL列表，使用JSON数组存储
     */
    private String imageUrls;

    /**
     * 视频URL
     */
    private String videoUrl;

    /**
     * 点赞数
     */
    private Integer thumbNum;

    /**
     * 状态 0 正常
     */
    private Integer postState;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 是否删除
     */
    @TableLogic
    private Integer isDelete;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
