package com.leng.ice.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.leng.ice.common.ErrorCode;
import com.leng.ice.config.CacheConfig;
import com.leng.ice.contant.RedisConstant;
import com.leng.ice.exception.BusinessException;
import com.leng.ice.mapper.UserMapper;
import com.leng.ice.model.dto.UserConsumerQuery;
import com.leng.ice.model.entity.User;
import com.leng.ice.service.UserService;
import com.leng.ice.utils.AlgorithmUtils;
import com.leng.ice.utils.CosUtils;
import com.leng.ice.utils.TopNOptimizer;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.DigestUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.leng.ice.contant.UserConstant.ADMIN_ROLE;
import static com.leng.ice.contant.UserConstant.USER_LOGIN_STATE;

/**
 * @author leng
 * @description 针对表【user(用户)】的数据库操作Service实现
 */
@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
        implements UserService {

    @Resource
    UserMapper userMapper;

    @Resource
    private CosUtils cosUtils;

    @Resource
    private RedisTemplate<String,Object> redisTemplate;

    @Resource
    private RedissonClient redissonClient;

    @Resource
    private CacheConfig cacheConfig;

    /**
     * 盐值,密码加密
     */
    private static final String SALT = "leng";

    /**
     * 用户注册
     * @param username
     * @param userAccount
     * @param userPassword
     * @param checkPassword
     * @return
     */
    @Override
    public long userRegister(String username, String userAccount, String userPassword, String checkPassword) {
        //用户名、账号、密码 不能为空
        if (StringUtils.isAnyBlank(userAccount, userPassword, checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"账号或密码为空");
        }
        // 用户名校验
        if (username.length() > 8){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"用户名长度大于8");
        }
        // 账号校验
        if (userAccount.length() < 4){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"账号长度小于4");
        }
        // 密码校验
        if (userPassword.length()<8 || checkPassword.length()<8){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"密码长度小于8");
        }
        if (!userPassword.equals(checkPassword)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"校验密码不一致");
        }
        //账号非法字符校验
        String validPattern = "[\\u4e00-\\u9fa5\\u00A0\\s\"`~!@#$%^&*()+=|{}':;',\\[\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]";
        Matcher matcher = Pattern.compile(validPattern).matcher(userAccount);
        if (matcher.find()){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"账号含非法字符");
        }
        //账号不能重复
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userAccount",userAccount);
        long count = this.count(queryWrapper);
        if (count > 0){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"账号重复");
        }
        //加密
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT+userPassword).getBytes());
        //插入数据
        User user = new User();
        user.setUsername(username);
        user.setUserAccount(userAccount);
        user.setUserPassword(encryptPassword);
        //添加用户的默认信息
        user.setUsername(username);
        user.setAvatarUrl("https://xiaoleng-1333106413.cos.ap-chengdu.myqcloud.com/avatar/default.png");
        user.setUserProfile("这个人很懒，介绍都不写");
        user.setTags("[\"女\"]");
        boolean saveResult = this.save(user);
        if (!saveResult){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"数据插入错误");
        }
        Long id = user.getId();
        return id;
    }

    /**
     * 用户登录
     * @param userAccount 账号
     * @param userPassword  密码
     * @param httpServletRequest 请求对象
     * @return
     */
    @Override
    public User doLogin(String userAccount, String userPassword, HttpServletRequest httpServletRequest) {
        //账号，密码 不能为空
        if (StringUtils.isAnyBlank(userAccount, userPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"登录参数为空");
        }
        // 账号校验
        if (userAccount.length() < 4){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"账号长度小于4");
        }
        //账号非法字符校验
        String validPattern = "[\\u00A0\\s\"`~!@#$%^&*()+=|{}':;',\\[\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]";
        Matcher matcher = Pattern.compile(validPattern).matcher(userAccount);
        if (matcher.find()){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"账号非法");
        }
        //加密
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT+userPassword).getBytes());
        //账号查询
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userAccount",userAccount);
        queryWrapper.eq("userPassword",encryptPassword);
        User user = userMapper.selectOne(queryWrapper);

        //用户不存在
        if (user == null){
            log.info("user login failed,userAccount cannot match userPassword");
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"账号或者密码错误");
        }
        User safetyUser = getSafetyUser(user);
        //记录用户登录态
        httpServletRequest.getSession().setAttribute(USER_LOGIN_STATE,safetyUser);
        return safetyUser;
    }

    /**
     * 用户信息脱敏
     * @param originUser 用户信息
     * @return
     */
    @Override
    public User getSafetyUser(User originUser){
        if(originUser == null) return null;
        User safetyUser = new User();
        safetyUser.setId(originUser.getId());
        safetyUser.setUsername(originUser.getUsername());
        safetyUser.setUserAccount(originUser.getUserAccount());
        safetyUser.setAvatarUrl(originUser.getAvatarUrl());
        safetyUser.setGender(originUser.getGender());
        safetyUser.setPhone(originUser.getPhone());
        safetyUser.setEmail(originUser.getEmail());
        safetyUser.setUserProfile(originUser.getUserProfile());
        safetyUser.setUserRole(originUser.getUserRole());
        safetyUser.setUserState(originUser.getUserState());
        safetyUser.setCreateTime(originUser.getCreateTime());
        safetyUser.setTags(originUser.getTags());
        return safetyUser;
    }

    /**
     * 用户注销
     * @param httpServletRequest
     * @return
     */
    @Override
    public int userLogout(HttpServletRequest httpServletRequest) {
        httpServletRequest.getSession().removeAttribute(USER_LOGIN_STATE);
        return 1;
    }

    /**
     * 根据标签查询用户SQL版
     * @param tagNameList 标签列表
     * @return
     */
    @Override
    public List<User> searchUserByTags(List<String> tagNameList){
        if(CollectionUtils.isEmpty(tagNameList)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        //查询出所有的用户
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        List<User> userList = userMapper.selectList(queryWrapper);
        Gson gson = new Gson();
        //在内存中查询符合要求的标签
        return userList.stream().filter(user -> {
            String tagsStr = user.getTags();
            Set<String>tempTagNameStr = gson.fromJson(tagsStr,new TypeToken<Set<String>>(){}.getType());
            //是否为空，为空返回HashSet的默认值，否则返回数值
            tempTagNameStr = Optional.ofNullable(tempTagNameStr).orElse(new HashSet<>());
            //返回false会过滤掉
            for (String tagName:tagNameList
                 ) {
                if(!tempTagNameStr.contains(tagName)){
                    return false;
                }
            }
            return true;
        }).map(this::getSafetyUser).collect(Collectors.toList());
    }

    /**
     * 查询登录用户信息
     * @param httpServletRequest http请求的封装响应信息
     * @return user
     */
    @Override
    public User getLoginUser(HttpServletRequest httpServletRequest) {
        if(httpServletRequest == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = (User) httpServletRequest.getSession().getAttribute(USER_LOGIN_STATE);
        if(user == null){
            throw new BusinessException(ErrorCode.LOGIN_ERROR);
        }
        return user;
    }

    /**
     * 更新用户信息
     * @param user
     * @param httpServletRequest
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)  // 添加事务注解
    public boolean updateUser(User user, HttpServletRequest httpServletRequest) {
        long userId = user.getId();
        //todo 对传递来的user参数判断，不能为空等等，增加判断条件,同时更新会话的信息
        User loginUser = (User) this.getLoginUser(httpServletRequest);
        if(userId<=0){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        //如果是管理员可以改任何人的信息
        //本人可以改自己的信息
        if(!isAdmin(loginUser) && loginUser.getId() != userId){
            throw new BusinessException(ErrorCode.NO_ADMIN);
        }
        User oldUser = userMapper.selectById(userId);
        if(oldUser == null){
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }
        //0为男，1为女
        Integer gender = user.getGender();
        if (gender != null){
            if (gender != 1 && gender != 0){
                return false;
            }
        }
        int updateById = userMapper.updateById(user);
        if (updateById != 1){
            return false;
        }
        //普通用户重新更新会话，管理员不更新
        if (!isAdmin(loginUser)){
            oldUser = userMapper.selectById(userId);
            User safetyUser = getSafetyUser(oldUser);
            httpServletRequest.getSession().setAttribute(USER_LOGIN_STATE,safetyUser);
        }
        //更新缓存中的信息
        List<User> userList = getRecommend();
        if (userList == null){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"更新用户信息错误");
        }
        return true;
    }

    /**
     * 根据标签查询用户SQL版
     * @param tagNameList 标签列表
     * @return
     */
    @Deprecated
    public List<User> searchUserByTagsSQL(List<String> tagNameList){
        if(CollectionUtils.isEmpty(tagNameList)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        //模糊查询 %java%
        for (String tagName:tagNameList
             ) {
           queryWrapper = queryWrapper.like("tags",tagName);
        }
        List<User> userList = userMapper.selectList(queryWrapper);
        return userList.stream().map(this::getSafetyUser).collect(Collectors.toList());
    }

    /**
     * 查询是否为管理员
     * @param httpServletRequest 请求头封装信息
     * @return
     */
    @Override
    public boolean isAdmin(HttpServletRequest httpServletRequest) {
        Object userObj = httpServletRequest.getSession().getAttribute(USER_LOGIN_STATE);
        User user = (User) userObj;
        if (user == null) {
            return false;
        }
        return user.getUserRole() == ADMIN_ROLE;
    }

    /**
     * 查询是否为管理员
     * @param loginUser 用户信息
     * @return
     */
    @Override
    public boolean isAdmin(User loginUser) {
        if (loginUser == null) {
            return false;
        }
        return loginUser.getUserRole() == ADMIN_ROLE;
    }

    /**
     * 匹配推荐用户 （根据标签）- 优化版本
     * 使用优先队列优化TOP N运算，提升性能
     * @param num
     * @param loginUser
     * @return
     */
    @Override
    public List<User> matchUsers(long num, User loginUser) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("id","tags");
        queryWrapper.isNotNull("tags");
        List<User> userList = this.list(queryWrapper);
        String loginUserTags = loginUser.getTags();
        Gson gson = new Gson();
        List<String> tagList = gson.fromJson(loginUserTags, new TypeToken<List<String>>(){}.getType());
        // 过滤有效用户
        List<User> validUsers = userList.stream()
            .filter(user -> !StringUtils.isBlank(user.getTags()) && !user.getId().equals(loginUser.getId()))
            .collect(Collectors.toList());
        // 使用优先队列优化TOP N运算
        List<TopNOptimizer.ScoredElement<User>> topUsers = TopNOptimizer.getTopNWithMinHeap(
            validUsers,
            user -> {
                List<String> userTagList = gson.fromJson(user.getTags(), new TypeToken<List<String>>(){}.getType());
                return (long) AlgorithmUtils.minDistance(tagList, userTagList);
            },
            (int) num
        );
        // 提取用户ID列表
        List<Long> userIdList = topUsers.stream()
            .map(scoredElement -> scoredElement.getElement().getId())
            .collect(Collectors.toList());
        if (userIdList.isEmpty()) {
            return new ArrayList<>();
        }
        // 查询完整用户信息
        queryWrapper = new QueryWrapper<>();
        queryWrapper.in("id", userIdList);
        Map<Long, User> userIdUserMap = this.list(queryWrapper)
            .stream()
            .map(this::getSafetyUser)
            .collect(Collectors.toMap(User::getId, Function.identity()));
        // 按原始顺序返回结果
        List<User> finalUserList = new ArrayList<>();
        for (Long userId : userIdList) {
            User user = userIdUserMap.get(userId);
            if (user != null) {
                finalUserList.add(user);
            }
        }
        return finalUserList;
    }

    /**
     * 异步匹配推荐用户 - 高性能版本
     * 结合缓存和异步计算进一步提升性能
     */
//    @Async
//    public CompletableFuture<List<User>> matchUsersAsync(long num, User loginUser) {
//        Gson gson = new Gson();
//        // 检查缓存
//        String cacheKey = "match_users:" + loginUser.getId() + ":" + num;
//        String cachedResult = (String) redisTemplate.opsForValue().get(cacheKey);
//        if (cachedResult != null) {
//            List<User> users = gson.fromJson(cachedResult, new TypeToken<List<User>>(){}.getType());
//            return CompletableFuture.completedFuture(users);
//        }
//        // 执行匹配逻辑
//        List<User> result = matchUsers(num, loginUser);
//        // 缓存结果（5分钟过期）
//        redisTemplate.opsForValue().set(cacheKey, gson.toJson(result), 5, TimeUnit.MINUTES);
//        return CompletableFuture.completedFuture(result);
//    }

    /**
     * 上传头像
     * @param avatarImg
     * @param httpServletRequest
     * @return
     */
    @Override
    public String uploadAvatar(MultipartFile avatarImg, HttpServletRequest httpServletRequest) {
        User loginUser = getLoginUser(httpServletRequest);
        // 校验图片格式
        if (!imageTypeRight(avatarImg)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"图片格式错误");
        }
        try {
            // 上传到腾讯云COS
            String uploadPath = cosUtils.uploadFile(avatarImg, "avatar");
            loginUser.setAvatarUrl(uploadPath);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"图片上传失败");
        }
        boolean result = this.updateById(loginUser);
        if (!result){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"用户头像更新失败");
        }
        //普通用户重新更新会话，管理员不更新
        if (!isAdmin(loginUser)){
            User safetyUser = getSafetyUser(loginUser);
            httpServletRequest.getSession().setAttribute(USER_LOGIN_STATE,safetyUser);
        }
        return loginUser.getAvatarUrl();
    }

    /**
     * 根据账号查询用户信息
     * @param account
     * @return
     */
    @Override
    public UserConsumerQuery getByAccount(String account) {
        if (StringUtils.isBlank(account)){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        }
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userAccount",account);
        User user = this.getOne(queryWrapper);
        if (user == null){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        }
        UserConsumerQuery consumerQuery = new UserConsumerQuery();
        BeanUtils.copyProperties(user,consumerQuery);
        return consumerQuery;
    }

    /**
     * 获取推荐用户
     * @return
     */
    @Override
    public List<User> getRecommend() {
        //反复抢锁 保证数据一致性
        List<User> userList = new ArrayList<>();
        //获取锁
        RLock lock = redissonClient.getLock(RedisConstant.REDIS_RECOMMEND_UPDATE_KEY);
        try {
            while (true){
                //反复抢锁，保证数据一致性
                if (lock.tryLock(0,-1, TimeUnit.MILLISECONDS)){
                    //当前获得锁的线程的id是
                    System.out.println("getLock"+Thread.currentThread().getId());
                    //查询信息
                    //todo 可以单独mapper方法
                    QueryWrapper<User> queryWrapper = new QueryWrapper<>();
                    //用户脱敏
                    queryWrapper.select("id", "username", "userAccount"
                            , "userProfile", "avatarUrl", "gender", "phone"
                            , "email", "tags", "userRole", "updateTime", "createTime", "userState");
                    userList = this.list(queryWrapper);
                    // 在写缓存的地方修改（大约第477行）
                    ValueOperations<String, Object> valueOperations = redisTemplate.opsForValue();
                    //写缓存，设置过期时间
                    try {
                        valueOperations.set(RedisConstant.REDIS_RECOMMEND_KEY, userList,
                                           cacheConfig.getUserRecommendTtl(TimeUnit.SECONDS),
                                           TimeUnit.SECONDS);

                    } catch (Exception e) {
                        log.error("redis set key error", e);
                    }
                    return userList;
                }
            }
        } catch (InterruptedException e) {
            log.error("doCacheRecommendUser error", e);
        }finally {
            //只能自己释放自己的锁
            if (lock.isHeldByCurrentThread()){
                lock.unlock();
            }
        }
        return userList;
    }

    /**
     * 获取推荐用户
     * @return
     */
    @Override
    public List<User> getRecommendCache() {
        //查询缓存
        ValueOperations<String, Object> valueOperations = redisTemplate.opsForValue();
        List<User> userList= (List<User>) valueOperations.get(RedisConstant.REDIS_RECOMMEND_KEY);
        //缓存为空，查询数据库并写入缓存,不为空返回缓存数据
        return userList == null || userList.isEmpty() ? getRecommend() : userList;
    }

    /**
     * 验证图片的格式
     *
     * @param file 图片
     * @return
     */
    private boolean imageTypeRight(MultipartFile file) {
        // 首先校验图片格式
        List<String> imageType = Arrays.asList("jpg", "jpeg", "png", "bmp", "gif");
        // 获取文件名，带后缀
        String originalFilename = file.getOriginalFilename();
        // 获取文件的后缀格式
        String fileSuffix = null;  //不带 .
        if (originalFilename != null) {
            fileSuffix = originalFilename.substring(originalFilename.lastIndexOf(".") + 1).toLowerCase();
        }
        return imageType.contains(fileSuffix);
    }

}




