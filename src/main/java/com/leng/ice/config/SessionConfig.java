package com.leng.ice.config;

import org.springframework.boot.autoconfigure.session.DefaultCookieSerializerCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.session.MapSessionRepository;
import org.springframework.session.SessionRepository;
import org.springframework.session.config.annotation.web.http.EnableSpringHttpSession;
import org.springframework.session.web.http.DefaultCookieSerializer;

import java.util.concurrent.ConcurrentHashMap;

/**
 * session配置
 */
@Configuration
@EnableSpringHttpSession
public class SessionConfig {

    /**
     * 配置session存储仓库
     * @return
     */
    @Bean
    public SessionRepository sessionRepository() {
        return new MapSessionRepository(new ConcurrentHashMap<>());
    }

    /**
     * 配置cookieSerializer
     * @return
     */
    @Bean
    DefaultCookieSerializerCustomizer cookieSerializerCustomizer() {
        return new DefaultCookieSerializerCustomizer() {
            @Override
            public void customize(DefaultCookieSerializer cookieSerializer) {
                cookieSerializer.setSameSite("None"); // 设置cookie的SameSite属性为None，否则跨域set-cookie会被chrome浏览器阻拦
                cookieSerializer.setUseSecureCookie(true); // sameSite为None时，useSecureCookie必须为true
            }
        };
    }
}