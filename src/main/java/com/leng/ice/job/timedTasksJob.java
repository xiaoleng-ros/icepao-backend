package com.leng.ice.job;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.leng.ice.common.ErrorCode;
import com.leng.ice.contant.RedisConstant;
import com.leng.ice.exception.BusinessException;
import com.leng.ice.model.entity.Team;
import com.leng.ice.model.entity.User;
import com.leng.ice.service.TeamService;
import com.leng.ice.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 每天凌晨0点解散过期队伍
 *
 * @author leng
 */
@Component
@Slf4j
public class timedTasksJob {

    @Resource
    private TeamService teamService;

    @Resource
    private UserService userService;

    @Resource
    private RedissonClient redissonClient;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 每天0点执行解散过期队伍
     */
    @Scheduled(cron = "0 0 0 * * ?")
    public void deleteOutTeam() {
        //移除所有队伍中用户
        QueryWrapper<Team> queryWrapper = new QueryWrapper<>();
        queryWrapper.lt("expireTime", new Date());
        //过期的队伍
        List<Team> teamList = teamService.list(queryWrapper);
        teamList.forEach(team -> {
            //删除过期队伍与用户的关系
            Long teamId = team.getId();
            boolean result = teamService.removeByTeamId(teamId);
            if (!result){
                throw new BusinessException(ErrorCode.SYSTEM_ERROR,"定时任务错误");
            }
        });
    }

    /**
     * 缓存预热，每天每小时更新一次点执行，更新用户列表，集群部署下只需要一个服务器完成缓存更新
     */
    @Scheduled(cron = "0 0 0-23 * * ?")
    public void updateUserList() {
        //获取锁
        RLock lock = redissonClient.getLock("ice:precachejob:docache:lock");
        try {
            if (lock.tryLock(0,-1, TimeUnit.MILLISECONDS)){
                //当前获得锁的线程的id是
                System.out.println("getLock"+Thread.currentThread().getId());
                //查询信息
                QueryWrapper<User> queryWrapper = new QueryWrapper<>();
                //用户脱敏
                queryWrapper.select("id", "username", "userAccount"
                        , "userProfile", "avatarUrl", "gender", "phone"
                        , "email", "tags", "userRole", "updateTime", "createTime", "userState");
                List<User> userPage = userService.list(queryWrapper);
                String redisKey = RedisConstant.REDIS_RECOMMEND_KEY;
                ValueOperations<String, Object> valueOperations = redisTemplate.opsForValue();
                //写缓存
                try {
                    valueOperations.set(redisKey,userPage);
                } catch (Exception e) {
                    log.error("redis set key error",e);
                }
            }
        } catch (InterruptedException e) {
            log.error("doCacheRecommendUser error", e);
        }finally {
            //只能自己释放自己的锁
            if (lock.isHeldByCurrentThread()){
                lock.unlock();
            }
        }
    }
}
