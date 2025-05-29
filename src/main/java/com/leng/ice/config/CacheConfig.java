package com.leng.ice.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * Redis缓存配置类
 * 统一管理各业务模块的缓存过期时间
 * @author leng
 */
@Data
@Component
@ConfigurationProperties(prefix = "spring.redis.cache")
public class CacheConfig {
    
    /**
     * 默认过期时间（秒）
     */
    private Long defaultTtl = 3600L;
    
    /**
     * 缓存过期时间配置
     */
    private TtlConfig ttl = new TtlConfig();
    
    @Data
    public static class TtlConfig {
        /**
         * 用户推荐缓存过期时间（秒）
         */
        private Long userRecommend = 7200L;
        
        /**
         * 帖子评论缓存过期时间（秒）
         */
        private Long postComment = 1800L;
        
        /**
         * 用户信息缓存过期时间（秒）
         */
        private Long userInfo = 3600L;
        
        /**
         * 队伍信息缓存过期时间（秒）
         */
        private Long teamInfo = 1800L;
    }
    
    /**
     * 获取用户推荐缓存过期时间
     * @param timeUnit 时间单位
     * @return 过期时间
     */
    public Long getUserRecommendTtl(TimeUnit timeUnit) {
        return timeUnit.convert(ttl.getUserRecommend(), TimeUnit.SECONDS);
    }
    
    /**
     * 获取帖子评论缓存过期时间
     * @param timeUnit 时间单位
     * @return 过期时间
     */
    public Long getPostCommentTtl(TimeUnit timeUnit) {
        return timeUnit.convert(ttl.getPostComment(), TimeUnit.SECONDS);
    }
    
    /**
     * 获取用户信息缓存过期时间
     * @param timeUnit 时间单位
     * @return 过期时间
     */
    public Long getUserInfoTtl(TimeUnit timeUnit) {
        return timeUnit.convert(ttl.getUserInfo(), TimeUnit.SECONDS);
    }
    
    /**
     * 获取队伍信息缓存过期时间
     * @param timeUnit 时间单位
     * @return 过期时间
     */
    public Long getTeamInfoTtl(TimeUnit timeUnit) {
        return timeUnit.convert(ttl.getTeamInfo(), TimeUnit.SECONDS);
    }
    
    /**
     * 获取默认缓存过期时间
     * @param timeUnit 时间单位
     * @return 过期时间
     */
    public Long getDefaultTtl(TimeUnit timeUnit) {
        return timeUnit.convert(defaultTtl, TimeUnit.SECONDS);
    }
}