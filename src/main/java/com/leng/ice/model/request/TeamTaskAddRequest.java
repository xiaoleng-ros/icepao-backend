package com.leng.ice.model.request;

import lombok.Data;

import java.io.Serializable;

/**
 * 队伍任务添加请求体
 */
@Data
public class TeamTaskAddRequest implements Serializable {

    /**
     * 队伍id
     */
    private Long teamId;

    /**
     * 用户id
     */
    private Long userId;

    /**
     * 任务名称
     */
    private String taskName;

    /**
     * 任务描述
     */
    private String description;

}
