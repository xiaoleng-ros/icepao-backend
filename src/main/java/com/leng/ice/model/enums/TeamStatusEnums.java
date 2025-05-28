package com.leng.ice.model.enums;

import lombok.Getter;

/**
 * 队伍状态枚举
 *
 * @author leng
 */
@Getter
public enum TeamStatusEnums {

    /**
     * 公开
     */
    PUBLIC(0,"公开"),

    /**
     * 私有
     */
    PRIVATE(1,"私有"),

    /**
     * 加密
     */
    SECRET(2,"加密");

    /**
     * 值
     */
    private int value;

    /**
     * 文本
     */
    private String text;

    /**
     * 根据值获取枚举
     * @param value
     * @return
     */
    public static TeamStatusEnums getEnumByValues(Integer value) {
        if(value == null){
            return null;
        }
        TeamStatusEnums[] values = TeamStatusEnums.values();
        for (TeamStatusEnums teamStatusEnum:values
             ) {
            if(teamStatusEnum.getValue() == value){
                return teamStatusEnum;
            }
        }
        return null;
    }

    /**
     * 构造函数
     * @param value
     * @param text
     */
    TeamStatusEnums(int value, String text) {
        this.value = value;
        this.text = text;
    }

    /**
     * 设置值
     * @param value
     */
    public void setValue(int value) {
        this.value = value;
    }

    /**
     * 设置文本
     * @param text
     */
    public void setText(String text) {
        this.text = text;
    }
}
