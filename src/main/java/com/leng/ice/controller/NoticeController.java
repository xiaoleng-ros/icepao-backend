package com.leng.ice.controller;

import com.leng.ice.common.BaseResponse;
import com.leng.ice.common.ErrorCode;
import com.leng.ice.common.ResultUtils;
import com.leng.ice.exception.BusinessException;
import com.leng.ice.model.entity.User;
import com.leng.ice.model.request.NoticeUpdateRequest;
import com.leng.ice.model.request.ObjectIdRequest;
import com.leng.ice.model.vo.NoticeVO;
import com.leng.ice.service.NoticeService;
import com.leng.ice.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 通知接口
 *
 * @author MA_dou
 */
@RestController
@RequestMapping("/notice")
@Slf4j
public class NoticeController {
    @Resource
    NoticeService noticeService;

    @Resource
    UserService userService;

    /**
     * 删除帖子
     *
     * @param objectIdRequest
     * @param httpServletRequest
     * @return
     */
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteNotice(@RequestBody ObjectIdRequest objectIdRequest, HttpServletRequest httpServletRequest) {
        if (objectIdRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Long id = objectIdRequest.getId();
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(httpServletRequest);
        boolean result = noticeService.deleteNotice(id, loginUser);
        if (!result) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "删除失败");
        }
        return ResultUtils.success(true);
    }

    /**
     * 更新通知状态
     *
     * @param noticeUpdateRequest
     * @param httpServletRequest
     * @return
     */
    @PostMapping("/update")
    public BaseResponse<Boolean> updateNotice(@RequestBody NoticeUpdateRequest noticeUpdateRequest, HttpServletRequest httpServletRequest) {
        if (noticeUpdateRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(httpServletRequest);
        boolean result = noticeService.updateNotice(noticeUpdateRequest, loginUser);
        if (!result) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "数据更新失败");
        }
        return ResultUtils.success(true);
    }

    /**
     * 查询通知
     *
     * @param id
     * @return
     */
    @GetMapping("/get")
    public BaseResponse<NoticeVO> getNoticeById(@RequestParam long id) {
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        NoticeVO noticeVO = noticeService.getNoticeInfoById(id);
        if (noticeVO == null) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "查询帖子失败");
        }
        return ResultUtils.success(noticeVO);
    }

    /**
     * 查询通知列表
     *
     * @param httpServletRequest
     * @return
     */
    @GetMapping("/list")
    public BaseResponse<List<NoticeVO>> getNoticeList(HttpServletRequest httpServletRequest) {
        User loginUser = userService.getLoginUser(httpServletRequest);
        List<NoticeVO> noticeList = noticeService.getNoticeList(loginUser);
        if (noticeList == null) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "查询通知列表失败");
        }
        return ResultUtils.success(noticeList);
    }


}
