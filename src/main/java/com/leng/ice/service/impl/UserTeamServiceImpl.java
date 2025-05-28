package com.leng.ice.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.leng.ice.mapper.UserTeamMapper;
import com.leng.ice.model.entity.UserTeam;
import com.leng.ice.service.UserTeamService;
import org.springframework.stereotype.Service;

/**
* @author leng
* @description 针对表【user_team(用户队伍关系)】的数据库操作Service实现
*/
@Service

public class UserTeamServiceImpl extends ServiceImpl<UserTeamMapper, UserTeam>
    implements UserTeamService {

}




