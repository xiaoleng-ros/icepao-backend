package com.leng.ice.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.leng.ice.model.entity.PostComment;
import com.leng.ice.model.vo.PostCommentVO;

import java.util.List;

/**
* @author leng
* @description 针对表【post_comment(帖子)】的数据库操作Service
*/
public interface PostCommentService extends IService<PostComment> {

    /**
     * 根据帖子id获取评论
     * @param postId
     * @return
     */
    List<PostCommentVO> getPostCommentVOList(Long postId);

    /**
     *  根据帖子id获取评论（查询缓存）
     * @param postId
     * @return
     */
    List<PostCommentVO> getPostCommentVOListCache(Long postId);
}
