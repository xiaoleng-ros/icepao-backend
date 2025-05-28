package com.leng.ice.model.request;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 队伍加入请求体
 */
@Data
public class TeamJoinRequest implements Serializable {

    /**
     * 队伍id
     */
    private Long teamId;

    /**
     * 队伍密码
     */
    private String teamPassword;

}
