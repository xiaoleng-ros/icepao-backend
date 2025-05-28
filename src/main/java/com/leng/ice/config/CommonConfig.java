package com.leng.ice.config;

import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 读取项目相关配置
 * 
 * @author boostor
 */
@Getter
@Component
@ConfigurationProperties(prefix = "innovation")
public class CommonConfig {
    
    /** 项目名称 */
    private String name;

    /** 版本 */
    private String version;

    /** 版权年份 */
    private String copyrightYear;

    /** 实例演示开关 */
    private boolean demoEnabled;

    /** 获取地址开关 */
    @Getter
    private static boolean addressEnabled;

    /**
     * 设置项目名称
     * @param name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * 设置项目版本号
     * @param version
     */
    public void setVersion(String version) {
        this.version = version;
    }

    /**
     * 设置版权年份
     * @param copyrightYear
     */
    public void setCopyrightYear(String copyrightYear) {
        this.copyrightYear = copyrightYear;
    }

    /**
     * 设置是否开启演示
     * @param demoEnabled
     */
    public void setDemoEnabled(boolean demoEnabled) {
        this.demoEnabled = demoEnabled;
    }

    /**
     * 设置是否开启地址开关
     * @param addressEnabled
     */
    public void setAddressEnabled(boolean addressEnabled) {
        CommonConfig.addressEnabled = addressEnabled;
    }

}