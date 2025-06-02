package com.leng.ice.controller;

import com.leng.ice.common.BaseResponse;
import com.leng.ice.common.ResultUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;

/**
 * Session测试控制器
 * 用于验证分布式Session功能
 */
@RestController
@RequestMapping("/api/session")
public class SessionTestController {

    /**
     * 设置Session数据
     */
    @PostMapping("/set")
    public BaseResponse<String> setSession(@RequestParam String key, 
                                         @RequestParam String value,
                                         HttpServletRequest request) {
        HttpSession session = request.getSession();
        session.setAttribute(key, value);
        return ResultUtils.success("Session数据设置成功，SessionId: " + session.getId());
    }

    /**
     * 获取Session数据
     */
    @GetMapping("/get")
    public BaseResponse<Map<String, Object>> getSession(@RequestParam String key,
                                                       HttpServletRequest request) {
        HttpSession session = request.getSession();
        Object value = session.getAttribute(key);
        
        Map<String, Object> result = new HashMap<>();
        result.put("sessionId", session.getId());
        result.put("key", key);
        result.put("value", value);
        result.put("maxInactiveInterval", session.getMaxInactiveInterval());
        
        return ResultUtils.success(result);
    }

    /**
     * 获取所有Session信息
     */
    @GetMapping("/info")
    public BaseResponse<Map<String, Object>> getSessionInfo(HttpServletRequest request) {
        HttpSession session = request.getSession();
        
        Map<String, Object> sessionInfo = new HashMap<>();
        sessionInfo.put("sessionId", session.getId());
        sessionInfo.put("creationTime", session.getCreationTime());
        sessionInfo.put("lastAccessedTime", session.getLastAccessedTime());
        sessionInfo.put("maxInactiveInterval", session.getMaxInactiveInterval());
        sessionInfo.put("isNew", session.isNew());
        
        return ResultUtils.success(sessionInfo);
    }
}