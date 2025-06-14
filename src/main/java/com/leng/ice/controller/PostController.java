package com.leng.ice.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.leng.ice.common.BaseResponse;
import com.leng.ice.common.ErrorCode;
import com.leng.ice.common.ResultUtils;
import com.leng.ice.exception.BusinessException;
import com.leng.ice.model.entity.Post;
import com.leng.ice.model.entity.User;
import com.leng.ice.model.request.ObjectIdRequest;
import com.leng.ice.model.request.PostAddRequest;
import com.leng.ice.model.request.PostCommentAddRequest;
import com.leng.ice.model.request.PostUpdateRequest;
import com.leng.ice.model.vo.PostVO;
import com.leng.ice.service.PostService;
import com.leng.ice.service.PostThumbService;
import com.leng.ice.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

/**
 * 帖子接口
 *
 * @author MA_dou
 */
@RestController
@RequestMapping("/post")
@Slf4j
public class PostController {
    @Resource
    PostService postService;

    @Resource
    UserService userService;

    @Resource
    PostThumbService postThumbService;

    /**
     * 添加帖子
     *
     * @param postAddRequest
     * @param httpServletRequest
     * @return
     */
    @PostMapping("/add")
    public BaseResponse<Long> addPost(@RequestBody PostAddRequest postAddRequest, HttpServletRequest httpServletRequest) {
        if (postAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(httpServletRequest);
        Post post = new Post();
        BeanUtils.copyProperties(postAddRequest, post);
        long postId = postService.addPost(post, loginUser);
        return ResultUtils.success(postId);
    }

    /**
     * 删除帖子
     *
     * @param objectIdRequest
     * @param httpServletRequest
     * @return
     */
    @PostMapping("/delete")
    public BaseResponse<Boolean> deletePost(@RequestBody ObjectIdRequest objectIdRequest, HttpServletRequest httpServletRequest) {
        if (objectIdRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Long id = objectIdRequest.getId();
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(httpServletRequest);
        boolean result = postService.deletePost(id, loginUser);
        if (!result) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "删除失败");
        }
        return ResultUtils.success(true);
    }

    /**
     * 更新帖子
     *
     * @param postUpdateRequest
     * @param httpServletRequest
     * @return
     */
    @PostMapping("/update")
    public BaseResponse<Boolean> updatePost(@RequestBody PostUpdateRequest postUpdateRequest, HttpServletRequest httpServletRequest) {
        if (postUpdateRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(httpServletRequest);
        boolean result = postService.updatePost(postUpdateRequest, loginUser);
        if (!result) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "数据更新失败");
        }
        return ResultUtils.success(true);
    }

    /**
     * 查询帖子
     *
     * @param id
     * @return
     */
    @GetMapping("/get")
    public BaseResponse<PostVO> getTeamById(@RequestParam long id,HttpServletRequest httpServletRequest) {
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(httpServletRequest);
        PostVO postVO = postService.getPostInfoById(id,true);
        postVO.setThumb(postThumbService.isPostThumb(postVO.getId(),loginUser.getId()));
        if (postVO == null) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "查询帖子失败");
        }
        return ResultUtils.success(postVO);
    }

    /**
     * 返回帖子列表（分页）
     * todo 可以做分页缓存，该方法多次查询了四次数据库，优化该方法可以大大提高速度
     * @param pageSize
     * @param pageNum
     * @param httpServletRequest
     * @return
     */
    @GetMapping("/list")
    public BaseResponse<List<PostVO>> getPostList(long pageSize, long pageNum, HttpServletRequest httpServletRequest) {
        QueryWrapper<Post> queryWrapper = new QueryWrapper<>();
        Page<Post> postPage = postService.page(new Page<>(pageNum, pageSize), queryWrapper);
        //获取查询数据
        List<Post> records = postPage.getRecords();
        List<PostVO> postVOList = new ArrayList<>();
        if (CollectionUtils.isEmpty(records)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        //查询点赞列表的postId，postId存在着set
        User loginUser = userService.getLoginUser(httpServletRequest);
        //给帖子信息进行脱敏和该用户是否点赞的判断
        records.forEach(record -> {
            PostVO postInfoById = postService.getPostInfoById(record.getId());
            //查询缓存 当前登录用户存是否在点赞中
            postInfoById.setThumb(postThumbService.isPostThumb(postInfoById.getId(),loginUser.getId()));
            postVOList.add(postInfoById);
        });
        return ResultUtils.success(postVOList);
    }


    /**
     * 添加帖子评论
     *
     * @param postCommentAddRequest
     * @param httpServletRequest
     * @return
     */
    @PostMapping("/addComment")
    public BaseResponse<Boolean> addPostComment(@RequestBody PostCommentAddRequest postCommentAddRequest, HttpServletRequest httpServletRequest) {
        if (postCommentAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(httpServletRequest);
        boolean result = postService.addComment(postCommentAddRequest, loginUser);
        return ResultUtils.success(result);
    }

    /**
     * 删除帖子评论
     *
     * @param objectIdRequest
     * @param httpServletRequest
     * @return
     */
    @PostMapping("/deleteComment")
    public BaseResponse<Boolean> deleteComment(@RequestBody ObjectIdRequest objectIdRequest, HttpServletRequest httpServletRequest) {
        if (objectIdRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Long id = objectIdRequest.getId();
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(httpServletRequest);
        boolean result = postService.deleteComment(id, loginUser);
        if (!result) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "删除失败");
        }
        return ResultUtils.success(true);
    }

}
