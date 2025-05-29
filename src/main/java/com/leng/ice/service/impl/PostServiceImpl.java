package com.leng.ice.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.leng.ice.common.ErrorCode;
import com.leng.ice.contant.RedisConstant;
import com.leng.ice.exception.BusinessException;
import com.leng.ice.mapper.PostMapper;
import com.leng.ice.model.entity.Notice;
import com.leng.ice.model.entity.Post;
import com.leng.ice.model.entity.PostComment;
import com.leng.ice.model.entity.User;
import com.leng.ice.model.request.PostCommentAddRequest;
import com.leng.ice.model.request.PostUpdateRequest;
import com.leng.ice.model.vo.PostCommentVO;
import com.leng.ice.model.vo.PostVO;
import com.leng.ice.service.NoticeService;
import com.leng.ice.service.PostCommentService;
import com.leng.ice.service.PostService;
import com.leng.ice.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author leng
 * @description 针对表【post(帖子)】的数据库操作Service实现
 */
@Service
@Slf4j
public class PostServiceImpl extends ServiceImpl<PostMapper, Post>
        implements PostService {

    @Resource
    UserService userService;

    @Resource
    PostCommentService postCommentService;

    @Resource
    private NoticeService noticeService;

    @Resource
    private RedissonClient redissonClient;

    @Resource
    private RedisTemplate<String,Object> redisTemplate;

    /**
     * 添加帖子
     * @param post
     * @param loginUser
     * @return
     */
    @Override
    public long addPost(Post post, User loginUser) {
        //请求参数是否为空
        if (post == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        //用户是否登录
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.LOGIN_ERROR);
        }
        final long userId = loginUser.getId();
        //内容内容大于10字小于200字
        String content = post.getContent();
        if (StringUtils.isBlank(content)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "内容为空");
        }
        if (content.length() < 10 || content.length() > 200) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "内容字数不符合要求");
        }
        //todo 判断敏感字符
        //信息插入帖子表
        post.setId(null);
        post.setUserId(userId);
        boolean result = this.save(post);
        Long postId = post.getId();
        if (!result || postId == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "发布帖子失败");
        }
        return postId;
    }

    /**
     * 删除帖子
     * @param id
     * @param loginUser
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deletePost(Long id, User loginUser) {
        Post post = this.getById(id);
        if (post == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "帖子不存在");
        }
        //是否为帖子创建人或者管理员
        Long userId = post.getUserId();
        if (!userService.isAdmin(loginUser) && userId != loginUser.getId()) {
            throw new BusinessException(ErrorCode.NO_ADMIN);
        }
        QueryWrapper<PostComment> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("postId", id);
        //删除帖子的评论。如果没有评论则不删除
        long postCommentCount = postCommentService.count(queryWrapper);
        if (postCommentCount > 0){
            boolean remove = postCommentService.remove(queryWrapper);
            if (!remove) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "删除帖子评论失败");
            }
        }
        return this.removeById(id);
    }

    /**
     * 修改帖子
     * @param postUpdateRequest
     * @param loginUser
     * @return
     */
    @Override
    public boolean updatePost(PostUpdateRequest postUpdateRequest, User loginUser) {
        Post oldPost = this.getById(postUpdateRequest.getId());
        if (oldPost == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "帖子不存在");
        }
        Long userId = oldPost.getUserId();
        //是否为帖子创建人或者管理员
        if (!userService.isAdmin(loginUser) && userId != loginUser.getId()) {
            throw new BusinessException(ErrorCode.NO_ADMIN);
        }
        String content = postUpdateRequest.getContent();
        if (StringUtils.isBlank(content) || content.length() < 10 || content.length() > 600) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "内容字数不符合要求");
        }
        Post post = new Post();
        BeanUtils.copyProperties(postUpdateRequest, post);
        return this.updateById(post);
    }

    /**
     * 根据id获取帖子详情
     * @param id
     * @return
     */
    @Override
    public PostVO getPostInfoById(Long id) {
        Post post = this.getById(id);
        if (post == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "贴子不存在");
        }
        //帖子脱敏
        PostVO postVO = new PostVO();
        BeanUtils.copyProperties(post, postVO);
        //查帖子的创建人信息
        User createUser = userService.getById(postVO.getUserId());
        postVO.setUsername(createUser.getUsername());
        postVO.setAvatarUrl(createUser.getAvatarUrl());
        //查询帖子评论
        Long postVOId = postVO.getId();
        //查询缓存，没有缓存就查询数据库并更新缓存
        List<PostCommentVO> postCommentVOList = postCommentService.getPostCommentVOListCache(postVOId);
        if (postCommentVOList.size() >5){
            postCommentVOList = postCommentVOList.subList(0,5);
        }
        postVO.setPostCommentList(postCommentVOList);
        return postVO;
    }

    /**
     * 根据id获取帖子详情
     * @param id
     * @return
     */
    @Override
    public PostVO getPostInfoById(Long id, boolean isInfo) {
        Post post = this.getById(id);
        if (post == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "贴子不存在");
        }
        //帖子脱敏
        PostVO postVO = new PostVO();
        BeanUtils.copyProperties(post, postVO);
        //查帖子的创建人信息
        User createUser = userService.getById(postVO.getUserId());
        postVO.setUsername(createUser.getUsername());
        postVO.setAvatarUrl(createUser.getAvatarUrl());
        //查询帖子评论
        Long postVOId = postVO.getId();
        //查询缓存，没有缓存就查询数据库并更新缓存
        List<PostCommentVO> postCommentVOList = postCommentService.getPostCommentVOListCache(postVOId);
        postVO.setPostCommentList(postCommentVOList);
        return postVO;
    }

    /**
     * 添加帖子评论
     * @param postCommentAddRequest
     * @param loginUser
     * @return
     */
    @Override
    public boolean addComment(PostCommentAddRequest postCommentAddRequest, User loginUser) {
        if (postCommentAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        //帖子id存在
        Long postId = postCommentAddRequest.getPostId();
        if (postId == null || postId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "帖子不存在");
        }
        //内容字数小于200，内容不能为空
        String content = postCommentAddRequest.getContent();
        if (StringUtils.isBlank(content) || content.length() == 0 || content.length() >= 200) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "内容字数不符合要求");
        }
        //判断评论的pid,pid为null代表该条评论pid是帖子的创建者，反之是回复者的id
        PostComment postComment = new PostComment();
        BeanUtils.copyProperties(postCommentAddRequest, postComment);
        //判断评论的父id
        Long pid = postComment.getPid();
        //获取帖子的创建者id
        Post post = this.getById(postId);
        if (post == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "帖子不存在");
        }
        //默认为评论的父id为帖子的创建者
        if (pid == null) {
            postComment.setPid(post.getUserId());
        }
        final long userId = loginUser.getId();
        postComment.setUserId(userId);
        //查询当前帖子的评论缓存
        List<PostCommentVO> commentVOListCache = postCommentService.getPostCommentVOListCache(postId);
        //抢锁更新数据库评论和更新缓存
        RLock lock = redissonClient.getLock(RedisConstant.REDIS_POST_COMMENT_UPDATE_KEY);
        try {
            while (true){
                //反复抢锁，保证数据一致性
                if (lock.tryLock(0,-1, TimeUnit.MILLISECONDS)){
                    //当前获得锁的线程的id是
                    System.out.println("getLock"+Thread.currentThread().getId());
                    //保存评论
                    boolean result = postCommentService.save(postComment);
                    //脱敏
                    PostCommentVO postCommentVO = new PostCommentVO();
                    BeanUtils.copyProperties(postComment,postCommentVO);
                    //添加默认值
                    postCommentVO.setAvatarUrl(loginUser.getAvatarUrl());
                    postCommentVO.setUsername(loginUser.getUsername());
                    postCommentVO.setCommentState(0);
                    //向评论列表添加评论
                    commentVOListCache.add(postCommentVO);
                    //发送通知给帖子的创建者
                    Notice notice = new Notice();
                    notice.setSenderId(userId);
                    notice.setReceiverId(post.getUserId());
                    notice.setTargetId(postId);
                    notice.setContent(postComment.getContent());
                    //1为评论
                    notice.setContentType(1);
                    long addNotice = noticeService.addNotice(notice);
                    if (addNotice<0) throw new BusinessException(ErrorCode.SYSTEM_ERROR,"通知失败");
                    //更新缓存
                    if (result){
                        ValueOperations<String, Object> valueOperations = redisTemplate.opsForValue();
                        try {
                            valueOperations.set(RedisConstant.REDIS_POST_COMMENT_KEY+postId,commentVOListCache);
                        } catch (Exception e) {
                            log.error("redis set key error",e);
                        }
                        return true;
                    }
                }
            }
        } catch (InterruptedException e) {
            log.error("addComment error", e);
            return false;
        }finally {
            //只能自己释放自己的锁
            if (lock.isHeldByCurrentThread()){
                lock.unlock();
            }
        }
    }

    /**
     * 删除帖子评论
     * @param id
     * @param loginUser
     * @return
     */
    //todo 缓存
    @Override
    public boolean deleteComment(long id, User loginUser) {
        PostComment postComment = postCommentService.getById(id);
        if (postComment == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "帖子不存在");
        }
        Long userId = postComment.getUserId();
        Long postId = postComment.getPostId();
        Post post = this.getById(postId);
        //是否为评论的创建人或者管理员，帖子的创建者
        if (!userService.isAdmin(loginUser) && userId != loginUser.getId() && post.getUserId() != loginUser.getId()) {
            throw new BusinessException(ErrorCode.NO_ADMIN);
        }
        return postCommentService.removeById(id);
    }
}




