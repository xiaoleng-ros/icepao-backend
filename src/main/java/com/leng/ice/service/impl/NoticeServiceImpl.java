package com.leng.ice.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.leng.ice.common.ErrorCode;
import com.leng.ice.exception.BusinessException;
import com.leng.ice.mapper.NoticeMapper;
import com.leng.ice.model.entity.Notice;
import com.leng.ice.model.entity.Post;
import com.leng.ice.model.entity.User;
import com.leng.ice.model.request.NoticeUpdateRequest;
import com.leng.ice.model.vo.NoticeVO;
import com.leng.ice.model.vo.TeamTaskVO;
import com.leng.ice.service.NoticeService;
import com.leng.ice.service.PostService;
import com.leng.ice.service.TeamTaskService;
import com.leng.ice.service.UserService;
import org.springframework.beans.BeanUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author leng
 * @description 针对表【notice(通知)】的数据库操作Service实现
 */
@Service
public class NoticeServiceImpl extends ServiceImpl<NoticeMapper, Notice>
        implements NoticeService {

    @Resource
    private UserService userService;

    @Lazy
    @Resource
    private PostService postService;

    @Lazy
    @Resource
    private TeamTaskService teamTaskService;

    /**
     * 发布通知
     * @param notice
     * @return
     */
    @Override
    public long addNotice(Notice notice) {
        //请求参数是否为空
        if (notice == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        //todo 判断敏感字符
        //信息插入帖子表
        notice.setId(null);
        boolean result = this.save(notice);
        Long noticeId = notice.getId();
        if (!result || noticeId == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "发布帖子失败");
        }
        return noticeId;
    }

    /**
     * 删除通知
     * @param id
     * @param loginUser
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)  // 添加事务注解
    public boolean deleteNotice(Long id, User loginUser) {
        Notice notice = this.getById(id);
        if (notice == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "通知不存在");
        }
        //是否为通知的接收者或者管理员 
        Long userId = notice.getReceiverId();
        if (!userService.isAdmin(loginUser) && userId != loginUser.getId()) {
            throw new BusinessException(ErrorCode.NO_ADMIN);
        }
        return this.removeById(id);
    }

    /**
     * 更新通知
     * @param noticeUpdateRequest
     * @param loginUser
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)  // 添加事务注解
    public boolean updateNotice(NoticeUpdateRequest noticeUpdateRequest, User loginUser) {
        Notice oldNotice = this.getById(noticeUpdateRequest.getId());
        if (oldNotice == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "通知不存在");
        }
        Long userId = oldNotice.getReceiverId();
        //是否为帖子创建人或者管理员
        if (!userService.isAdmin(loginUser) && userId != loginUser.getId()) {
            throw new BusinessException(ErrorCode.NO_ADMIN);
        }
        Integer noticeState = noticeUpdateRequest.getNoticeState();
        if (noticeState != 0 && noticeState != 1) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Notice notice = new Notice();
        BeanUtils.copyProperties(noticeUpdateRequest, notice);
        return this.updateById(notice);
    }

    /**
     * 获取通知信息
     * @param id
     * @return
     */
    //todo 从缓存中取
    @Override
    public NoticeVO getNoticeInfoById(Long id) {
        Notice notice = this.getById(id);
        if (notice == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "通知不存在");
        }
        
        // 查询发送者的信息
        Long senderId = notice.getSenderId();
        User sender = userService.getById(senderId);
        if (sender == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "发送者不存在");
        }
        String senderUsername = sender.getUsername();
        
        // 查询接收者的信息
        Long receiverId = notice.getReceiverId();
        User receiver = userService.getById(receiverId);
        if (receiver == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "接收者不存在");
        }
        String receiverUsername = receiver.getUsername();
        
        // 查询对象内容（帖子）
        Long targetId = notice.getTargetId();
        Post target = postService.getById(targetId);
        if (target == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "目标帖子不存在");
        }
        
        String content = target.getContent();
        if (content != null && content.length() > 20) {
            content = content.substring(0, 20) + "...";
        }
        
        NoticeVO noticeVO = new NoticeVO();
        BeanUtils.copyProperties(notice, noticeVO);
        noticeVO.setReceiverName(receiverUsername);
        noticeVO.setSenderName(senderUsername);
        noticeVO.setTargetContent(content != null ? content : "");
        return noticeVO;
    }

    /**
     * 获取通知信息（用户端）
     * @param id
     * @param receiverUsername
     * @return
     */
    @Override
    public NoticeVO getNoticeInfoById(Long id, String receiverUsername) {
        Notice notice = this.getById(id);
        if (notice == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "通知不存在");
        }
        // 查询发送者的信息
        Long senderId = notice.getSenderId();
        User sender = userService.getById(senderId);
        if (sender == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "发送者不存在");
        }
        String senderUsername = sender.getUsername();
        // 查询对象内容
        Long targetId = notice.getTargetId();
        String content = "";
        if (notice.getContentType() != 5) {
            Post target = postService.getById(targetId);
            if (target == null) {
                // 容错处理：帖子不存在时显示默认内容
                content = "[原帖子已删除]";
            } else {
                content = target.getContent();
            }
        } else {
            TeamTaskVO teamTaskInfo = teamTaskService.getTeamTaskInfoById(targetId);
            if (teamTaskInfo == null) {
                // 容错处理：任务不存在时显示默认内容
                content = "[原任务已删除]";
            } else {
                content = teamTaskInfo.getDescription();
            }
        }
        if (content != null && content.length() > 20) {
            content = content.substring(0, 20) + "...";
        }
        NoticeVO noticeVO = new NoticeVO();
        BeanUtils.copyProperties(notice, noticeVO);
        noticeVO.setReceiverName(receiverUsername);
        noticeVO.setSenderName(senderUsername);
        noticeVO.setTargetContent(content != null ? content : "");
        return noticeVO;
    }

    /**
     * 获取通知列表
     * @param loginUser
     * @return
     */
    @Override
    public List<NoticeVO> getNoticeList(User loginUser) {
        long userId = loginUser.getId();
        QueryWrapper<Notice> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("receiverId",userId).orderByDesc("id");
        List<Notice> noticeList = this.list(queryWrapper);
        List<NoticeVO> noticeVOList = new ArrayList<>();
        if (noticeList != null){
            noticeVOList = noticeList.stream().map(notice ->
                            getNoticeInfoById(notice.getId(), loginUser.getUsername()))
                            .collect(Collectors.toList());
        }
        return noticeVOList;
    }
}




