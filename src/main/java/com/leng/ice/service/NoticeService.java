package com.leng.ice.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.leng.ice.model.entity.Notice;
import com.leng.ice.model.entity.User;
import com.leng.ice.model.request.NoticeUpdateRequest;
import com.leng.ice.model.vo.NoticeVO;

import java.util.List;


/**
* @author leng
* @description 针对表【notice(通知)】的数据库操作Service
*/
public interface NoticeService extends IService<Notice> {

    /**
     * 添加通知信息
     * @param notice
     * @return
     */
    long addNotice(Notice notice);

    /**
     * 删除通知信息
     * @param id
     * @param loginUser
     * @return
     */
    boolean deleteNotice(Long id, User loginUser);

    /**
     * 更新通知信息状态
     * @param noticeUpdateRequest
     * @param loginUser
     * @return
     */
    boolean updateNotice(NoticeUpdateRequest noticeUpdateRequest, User loginUser);

    /**
     * 查询通知
     * @param id
     * @return
     */
    //todo 从缓存中取
    NoticeVO getNoticeInfoById(Long id, String receiverUsername);

    /**
     * 查询通知
     * @param id
     * @return
     */
    //todo 从缓存中取
    NoticeVO getNoticeInfoById(Long id);

    /**
     * 查询通知列表
     */
    List<NoticeVO> getNoticeList(User loginUser);
}
