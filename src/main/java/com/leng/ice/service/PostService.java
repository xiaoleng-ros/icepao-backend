package com.leng.ice.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.leng.ice.model.entity.Post;
import com.leng.ice.model.entity.User;
import com.leng.ice.model.request.PostCommentAddRequest;
import com.leng.ice.model.request.PostUpdateRequest;
import com.leng.ice.model.vo.PostVO;

/**
* @author leng
* @description 针对表【post(帖子)】的数据库操作Service
*/
public interface PostService extends IService<Post> {

    /**
     * 发布帖子
     * @param postAddRequest
     * @param loginUser
     * @return
     */
    long addPost(Post postAddRequest, User loginUser);

    /**
     * 删除帖子
     * @param id
     * @param loginUser
     * @return
     */
    boolean deletePost(Long id, User loginUser);

    /**
     * 更新帖子
     * @param postUpdateRequest
     * @param loginUser
     * @return
     */
    boolean updatePost(PostUpdateRequest postUpdateRequest, User loginUser);

    /**
     * 查询帖子
     * @param id
     * @return
     */
    PostVO getPostInfoById(Long id);

    /**
     * 查询帖子详细信息
     * @param id
     * @return
     */
    PostVO getPostInfoById(Long id,boolean isInfo);

    /**
     * 添加帖子评论(同时更新缓存)
     * @param postCommentAddRequest
     * @param loginUser
     * @return
     */
    boolean addComment(PostCommentAddRequest postCommentAddRequest, User loginUser);

    /**
     * 删除帖子评论
     * @param id
     * @param loginUser
     * @return
     */
    boolean deleteComment(long id, User loginUser);
}
