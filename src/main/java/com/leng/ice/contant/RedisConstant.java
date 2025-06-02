package com.leng.ice.contant;

public interface RedisConstant {

    /**
     * 用户列表redis的key
     */
    String REDIS_RECOMMEND_KEY = "ice:user:recommend";
    
    /**
     * 用户加入队伍的key
     */
    String REDIS_JOIN_TEAM_KEY = "ice:join_team";

    /**
     * 用户点赞的key
     */
    String REDIS_THUMB_KEY = "ice:THUMB";

    /**
     * 用户列表更新时锁的Key
     */
    String REDIS_RECOMMEND_UPDATE_KEY = "ice:user:recommend:update";

    /**
     * 帖子点赞缓存的key
     */
    String REDIS_POST_THUMB_KEY = "ice:post:thumb";

    /**
     * 帖子评论缓存的key
     */
    String REDIS_POST_COMMENT_KEY = "ice:post:comment";


    /**
     * 帖子评论更新时缓存的key
     */
    String REDIS_POST_COMMENT_UPDATE_KEY = "ice:post:comment:update";
}
