package com.leng.ice.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.leng.ice.common.BaseResponse;
import com.leng.ice.common.ErrorCode;
import com.leng.ice.common.ResultUtils;
import com.leng.ice.exception.BusinessException;
import com.leng.ice.model.entity.TeamTask;
import com.leng.ice.model.entity.User;
import com.leng.ice.model.request.ObjectIdRequest;
import com.leng.ice.model.request.TeamTaskAddRequest;
import com.leng.ice.model.request.TeamTaskCommentAddRequest;
import com.leng.ice.model.vo.TeamTaskVO;
import com.leng.ice.service.TeamTaskService;
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
 * 队伍任务接口
 *
 * @author MA_dou
 */
@RestController
@RequestMapping("/teamTask")
@Slf4j
public class TeamTaskController {
    @Resource
    TeamTaskService teamTaskService;

    @Resource
    UserService userService;

    /**
     * 添加队伍任务
     *
     * @param teamTaskAddRequest
     * @param httpServletRequest
     * @return
     */
    @PostMapping("/add")
    public BaseResponse<Long> addTeamTask(@RequestBody TeamTaskAddRequest teamTaskAddRequest, HttpServletRequest httpServletRequest) {
        if (teamTaskAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(httpServletRequest);
        TeamTask teamTask = new TeamTask();
        BeanUtils.copyProperties(teamTaskAddRequest, teamTask);
        Long teamTaskId = teamTaskService.addTeamTask(teamTask, loginUser);
        return ResultUtils.success(teamTaskId);
    }

    /**
     * 删除队伍任务
     *
     * @param objectIdRequest
     * @param httpServletRequest
     * @return
     */
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteTeamTask(@RequestBody ObjectIdRequest objectIdRequest, HttpServletRequest httpServletRequest) {
        if (objectIdRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Long id = objectIdRequest.getId();
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(httpServletRequest);
        boolean result = teamTaskService.deleteTeamTask(id, loginUser);
        if (!result) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "删除失败");
        }
        return ResultUtils.success(true);
    }

    /**
     * 查询队伍任务
     *
     * @param id
     * @return
     */
    @GetMapping("/get")
    public BaseResponse<TeamTaskVO> getTeamById(@RequestParam long id,HttpServletRequest httpServletRequest) {
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(httpServletRequest);
        TeamTaskVO teamTaskVO = teamTaskService.getTeamTaskInfoById(id);
        if (teamTaskVO == null) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "查询队伍任务失败");
        }
        return ResultUtils.success(teamTaskVO);
    }

    /**
     * 返回队伍任务列表（分页）
     * todo 可以做分页缓存，该方法多次查询了四次数据库，优化该方法可以大大提高速度
     * @param pageSize
     * @param pageNum
     * @param httpServletRequest
     * @return
     */
    @GetMapping("/list")
    public BaseResponse<List<TeamTaskVO>> getTeamTaskList(long pageSize, long pageNum, HttpServletRequest httpServletRequest) {
        QueryWrapper<TeamTask> queryWrapper = new QueryWrapper<>();
        Page<TeamTask> teamTaskPage = teamTaskService.page(new Page<>(pageNum, pageSize), queryWrapper);
        //获取查询数据
        List<TeamTask> records = teamTaskPage.getRecords();
        List<TeamTaskVO> teamTaskVOList = new ArrayList<>();
        if (CollectionUtils.isEmpty(records)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        //查询点赞列表的teamTaskId，teamTaskId存在着set
        User loginUser = userService.getLoginUser(httpServletRequest);
        //给队伍任务信息进行脱敏和该用户是否点赞的判断
        records.forEach(record -> {
            TeamTaskVO teamTaskInfoById = teamTaskService.getTeamTaskInfoById(record.getId());
            //查询缓存 当前登录用户存是否在点赞中
            teamTaskVOList.add(teamTaskInfoById);
        });
        return ResultUtils.success(teamTaskVOList);
    }

    /**
     * 添加队伍任务评论
     *
     * @param teamTaskCommentAddRequest
     * @param httpServletRequest
     * @return
     */
    @PostMapping("/addComment")
    public BaseResponse<Boolean> addTeamTaskComment(@RequestBody TeamTaskCommentAddRequest teamTaskCommentAddRequest, HttpServletRequest httpServletRequest) {
        if (teamTaskCommentAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(httpServletRequest);
        boolean result = teamTaskService.addComment(teamTaskCommentAddRequest, loginUser);
        return ResultUtils.success(result);
    }

    /**
     * 删除队伍任务评论
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
        boolean result = teamTaskService.deleteComment(id, loginUser);
        if (!result) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "删除失败");
        }
        return ResultUtils.success(true);
    }

    /**
     * 完成任务
     *
     * @param objectIdRequest
     * @param httpServletRequest
     * @return
     */
    @PostMapping("/finish")
    public BaseResponse<Boolean> finishTask(@RequestBody ObjectIdRequest objectIdRequest, HttpServletRequest httpServletRequest) {
        if (objectIdRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Long id = objectIdRequest.getId();
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(httpServletRequest);
        boolean result = teamTaskService.finishTask(id, loginUser);
        if (!result) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "完成任务操作失败");
        }
        return ResultUtils.success(true);
    }
}
