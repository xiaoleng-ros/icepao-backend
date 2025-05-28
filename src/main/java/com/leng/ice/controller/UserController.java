package com.leng.ice.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.leng.ice.common.BaseResponse;
import com.leng.ice.common.ErrorCode;
import com.leng.ice.common.ResultUtils;
import com.leng.ice.exception.BusinessException;
import com.leng.ice.model.entity.User;
import com.leng.ice.model.request.UserLoginRequest;
import com.leng.ice.model.request.UserRegisterRequest;
import com.leng.ice.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户接口
 *
 * @author MA_dou
 */
@RestController
@RequestMapping("/user")
@Slf4j
public class UserController {
    @Resource
    private UserService userService;

    /**
     * 用户注册
     *
     * @param userRegisterRequest
     * @return
     */
    @PostMapping("/register")
    public BaseResponse<Long> userRegister(@RequestBody UserRegisterRequest userRegisterRequest) {
        if (userRegisterRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "不能提交空表单");
        }
        String username = userRegisterRequest.getUsername();
        String userAccount = userRegisterRequest.getUserAccount();
        String userPassword = userRegisterRequest.getUserPassword();
        String checkPassword = userRegisterRequest.getCheckPassword();
        if (StringUtils.isAnyBlank(username, userAccount, userPassword, checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "注册参数不能为空");
        }
        long userRegister = userService.userRegister(username, userAccount, userPassword, checkPassword);
        return ResultUtils.success(userRegister);
    }

    /**
     * 用户登录
     *
     * @param userLoginRequest
     * @param request
     * @return
     */
    @PostMapping("/login")
    public BaseResponse<User> userLogin(@RequestBody UserLoginRequest userLoginRequest, HttpServletRequest request) {
        if (userLoginRequest == null) {
            throw new BusinessException(ErrorCode.NULL_ERROR, "请求头为空");
        }
        String userAccount = userLoginRequest.getUserAccount();
        String userPassword = userLoginRequest.getUserPassword();
        if (StringUtils.isAnyBlank(userAccount, userPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号或密码为空");
        }
        User user = userService.doLogin(userAccount, userPassword, request);
        return ResultUtils.success(user);
    }

    /**
     * 用户退出
     *
     * @param request
     * @return
     */
    @PostMapping("/logout")
    public BaseResponse<Integer> userLogout(HttpServletRequest request) {
        if (request == null) {
            return null;
        }
        int logout = userService.userLogout(request);
        return ResultUtils.success(logout);
    }

    /**
     * 搜索用户
     *
     * @param username
     * @param httpServletRequest
     * @return
     */
    @GetMapping("/search")
    public BaseResponse<List<User>> searchUsers(String username, HttpServletRequest httpServletRequest) {
        if (!userService.isAdmin(httpServletRequest)) {
            throw new BusinessException(ErrorCode.NO_ADMIN, "需要管理员权限");
        }
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        if (StringUtils.isNotBlank(username)) {
            queryWrapper.like("username", username);
        }
        List<User> userList = userService.list(queryWrapper);
        List<User> userList1 = userList.stream().map(user -> userService.getSafetyUser(user)).collect(Collectors.toList());
        return ResultUtils.success(userList1);
    }

    /**
     * 根据标签搜索用户
     *
     * @param tagNameList
     * @return
     */
    @GetMapping("/search/tags")
    public BaseResponse<List<User>> searchUsersByTags(@RequestParam(required = false) List<String> tagNameList) {
        if (CollectionUtils.isEmpty(tagNameList)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "标签参数错误");
        }
        List<User> userList = userService.searchUserByTags(tagNameList);
        return ResultUtils.success(userList);
    }

    /**
     * 获取当前用户
     *
     * @param httpServletRequest
     * @return
     */
    @GetMapping("/current")
    public BaseResponse<User> getCurrent(HttpServletRequest httpServletRequest) {
        Object userObj = userService.getLoginUser(httpServletRequest);
        User currentUser = (User) userObj;
        if (currentUser == null) {
            throw new BusinessException(ErrorCode.LOGIN_ERROR);
        }
        Long userId = currentUser.getId();

        User user = userService.getById(userId);
        User safetyUser = userService.getSafetyUser(user);
        return ResultUtils.success(safetyUser);
    }

    /**
     * 更新用户
     *
     * @param user
     * @param httpServletRequest
     * @return
     */
    @PostMapping("/update")
    public BaseResponse<Boolean> updateUser(@RequestBody User user, HttpServletRequest httpServletRequest) {
        if (user == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean result = userService.updateUser(user, httpServletRequest);
        return ResultUtils.success(result);
    }

    /**
     * 推荐用户
     *
     * @param pageSize
     * @param pageNum
     * @param httpServletRequest
     * @return
     */
    @GetMapping("/recommend")
    public BaseResponse<List<User>> recommendUser(long pageSize, long pageNum, HttpServletRequest httpServletRequest) {
        int left = (int) (pageSize*pageNum-pageSize);
        int right = (int) (pageSize*pageNum);

        List<User> userList = userService.getRecommendCache();
        if (userList == null){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"无用户信息");
        }
        if (left > userList.size()){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"无用户信息");
        }
            //当left或right值超过了List长度
        if (right>userList.size()){
            right = userList.size();
        }
        return ResultUtils.success(userList.subList(left, right));

    }

    /**
     * 删除用户
     *
     * @param id
     * @param httpServletRequest
     * @return
     */
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteUser(@RequestBody long id, HttpServletRequest httpServletRequest) {
        if (!userService.isAdmin(httpServletRequest)) {
            throw new BusinessException(ErrorCode.NO_ADMIN, "需要管理员权限");
        }
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户id不存在");
        }
        boolean byId = userService.removeById(id);
        return ResultUtils.success(byId);
    }

    /**
     * 匹配用户
     *
     * @param num
     * @param httpServletRequest
     * @return
     */
    @GetMapping("/match")
    public BaseResponse<List<User>> matchUsers(long num, HttpServletRequest httpServletRequest) {
        if (num <= 0 || num > 20) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(httpServletRequest);
        return ResultUtils.success(userService.matchUsers(num,loginUser));
    }

    /**
     * 上传头像
     *
     * @param avatarImg
     * @param httpServletRequest
     * @return
     */
    @PostMapping("/upload/avatar")
    public BaseResponse<String> uploadAvatar(MultipartFile avatarImg , HttpServletRequest httpServletRequest){
        if (avatarImg == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String result = userService.uploadAvatar(avatarImg, httpServletRequest);
        return ResultUtils.success(result);
    }
}



