package com.leng.ice.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.leng.ice.model.entity.TeamTaskComment;
import com.leng.ice.model.entity.User;
import com.leng.ice.model.vo.TeamTaskCommentVO;
import com.leng.ice.service.TeamTaskCommentService;
import com.leng.ice.mapper.TeamTaskCommentMapper;
import com.leng.ice.service.UserService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
* @author leng
* @description 针对表【team_task_comment(队伍任务评论)】的数据库操作Service实现
*/
@Service
public class TeamTaskCommentServiceImpl extends ServiceImpl<TeamTaskCommentMapper, TeamTaskComment>
    implements TeamTaskCommentService{

   @Resource
   private UserService userService;

    /**
     * 获取队伍任务评论列表
     * @param teamTaskId
     * @return
     */
    @Override
    public List<TeamTaskCommentVO> getTeamTaskCommentVOList(Long teamTaskId) {
        //反复抢锁 保证数据一致性
        List<TeamTaskCommentVO> teamTaskCommentVOList = new ArrayList<>();
        //查询信息
        QueryWrapper<TeamTaskComment> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("teamTaskId", teamTaskId);
        List<TeamTaskComment> teamTaskCommentList = this.list(queryWrapper);
        //队伍任务有评论就加载评论
        if (teamTaskCommentList != null && teamTaskCommentList.size() > 0) {
            //查询评论用户的信息
            List<Long> userIdList = teamTaskCommentList.stream().map(TeamTaskComment::getUserId).collect(Collectors.toList());
            QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
            userQueryWrapper.in("id", userIdList);
            // userId -> user 用户id对应用户信息
            Map<Long, List<User>> userListMap = userService.list(userQueryWrapper)
                    .stream().collect(Collectors.groupingBy(User::getId));
            //将查出来的用户信息与评论信息对接
            //将信息复制到返回类中
            teamTaskCommentList.forEach(teamTaskComment -> {
                TeamTaskCommentVO teamTaskCommentVO = new TeamTaskCommentVO();
                BeanUtils.copyProperties(teamTaskComment, teamTaskCommentVO);
                teamTaskCommentVOList.add(teamTaskCommentVO);
            });
            //将用户信息对接给评论
            teamTaskCommentVOList.forEach(teamTaskCommentVO -> {
                User user = userListMap.get(teamTaskCommentVO.getUserId()).get(0);
                teamTaskCommentVO.setUsername(user.getUsername());
                teamTaskCommentVO.setAvatarUrl(user.getAvatarUrl());
            });
        }
        return teamTaskCommentVOList;
    }
}




