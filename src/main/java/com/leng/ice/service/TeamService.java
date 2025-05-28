package com.leng.ice.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.leng.ice.model.entity.Team;
import com.leng.ice.model.entity.User;
import com.leng.ice.model.dto.TeamQuery;
import com.leng.ice.model.request.TeamJoinRequest;
import com.leng.ice.model.request.TeamQuitRequest;
import com.leng.ice.model.request.TeamUpdateRequest;
import com.leng.ice.model.vo.TeamVO;
import com.leng.ice.model.vo.UserTeamVO;

import java.util.List;

/**
* @author leng
* @description 针对表【team(队伍)】的数据库操作Service
*/
public interface TeamService extends IService<Team> {

    /**
     * 创建队伍
     * @param team
     * @param loginUser
     * @return
     */
    long addTeam(Team team, User loginUser);

    /**
     * 字段检索队伍
     * @param teamQuery
     * @param isAdmin
     * @return
     */
    List<UserTeamVO> listTeams(TeamQuery teamQuery,boolean isAdmin);

    /**
     * 更新队伍信息
     * @param teamUpdateRequest
     * @param loginUser
     * @return
     */
    boolean updateTeam(TeamUpdateRequest teamUpdateRequest, User loginUser);

    /**
     * 加入队伍
     * @param teamJoinRequest
     * @param loginUser
     * @return
     */
    boolean joinTeam(TeamJoinRequest teamJoinRequest, User loginUser);

    /**
     * 退出队伍
     * @param teamQuitRequest
     * @param loginUser
     * @return
     */
    boolean quitTeam(TeamQuitRequest teamQuitRequest, User loginUser);

    /**
     * 队长删除队伍
     * @param teamId
     * @param loginUser
     * @return
     */
    boolean deleteTeam(long teamId, User loginUser);

    /**
     * 根据队伍Id查询队伍信息
     * @param id
     * @return TeamVO
     */
    TeamVO getTeamInfoById(long id);

    /**
     * 根据队伍的id解散队伍
     * @param teamId
     * @return
     */
    boolean removeByTeamId(long teamId);
}
