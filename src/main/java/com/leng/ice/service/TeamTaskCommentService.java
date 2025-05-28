package com.leng.ice.service;

import com.leng.ice.model.entity.TeamTaskComment;
import com.baomidou.mybatisplus.extension.service.IService;
import com.leng.ice.model.vo.TeamTaskCommentVO;

import java.util.List;

/**
* @author leng
* @description 针对表【team_task_comment(队伍任务评论)】的数据库操作Service
*/
public interface TeamTaskCommentService extends IService<TeamTaskComment> {

    /**
     * 查询队伍任务评论
     * @param teamTaskVOId
     * @return
     */
    List<TeamTaskCommentVO> getTeamTaskCommentVOList(Long teamTaskVOId);

}
