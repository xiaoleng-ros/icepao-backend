package com.leng.ice.config;

import org.springframework.boot.autoconfigure.session.DefaultCookieSerializerCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;
import org.springframework.session.web.http.DefaultCookieSerializer;

/**
 * Redis分布式Session配置
 * 解决集群间登录态同步问题
 */
@Configuration
@EnableRedisHttpSession(maxInactiveIntervalInSeconds = 86400) // 24小时过期
public class SessionConfig {

    /**
     * 配置Cookie序列化器，支持跨域
     * @return DefaultCookieSerializerCustomizer
     */
    @Bean
    DefaultCookieSerializerCustomizer cookieSerializerCustomizer() {
        return new DefaultCookieSerializerCustomizer() {
            @Override
            public void customize(DefaultCookieSerializer cookieSerializer) {
                cookieSerializer.setSameSite("None"); // 设置cookie的SameSite属性为None，支持跨域
                cookieSerializer.setUseSecureCookie(true); // sameSite为None时，useSecureCookie必须为true
                cookieSerializer.setCookieName("ICE_SESSION"); // 自定义Session Cookie名称
                cookieSerializer.setCookiePath("/"); // Cookie路径
                cookieSerializer.setDomainNamePattern("^.+?\\.(\\w+\\.[a-z]+)$"); // 支持子域名共享
            }
        };
    }
}