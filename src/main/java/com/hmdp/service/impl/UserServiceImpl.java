package com.hmdp.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmdp.dto.LoginFormDTO;
import com.hmdp.dto.Result;
import com.hmdp.dto.UserDTO;
import com.hmdp.entity.User;
import com.hmdp.mapper.UserMapper;
import com.hmdp.service.IUserService;
import com.hmdp.utils.RedisConstants;
import com.hmdp.utils.RegexUtils;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.RandomUtil;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.annotation.Resource;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
@Slf4j
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Override
    public Result sendCode(String phone) {
        if(RegexUtils.isPhoneInvalid(phone)){
            return Result.fail("手机号格式错误");
        }
        String key = RandomUtil.randomString(6);
        stringRedisTemplate.opsForValue().set(RedisConstants.LOGIN_CODE_KEY+phone,key,RedisConstants.LOGIN_CODE_TTL,TimeUnit.MINUTES);
        
        log.info("[模拟短信] 手机号={} 验证码={}", phone, key);
        return Result.ok();
    }

    @Override
    public Result login(LoginFormDTO loginForm) {
        String phone = loginForm.getPhone();
        String code = loginForm.getCode();
        String key = stringRedisTemplate.opsForValue().get(RedisConstants.LOGIN_CODE_KEY+phone);
        if(key == null || !key.equals(code)){
            return Result.fail("验证码错误");
        }
        User user=query().eq("phone", phone).one();
        if(user == null){
            user=createUser(phone);
            save(user);
        }
        String token=UUID.randomUUID().toString(true);
        String redisToken=RedisConstants.LOGIN_USER_KEY+token;
        UserDTO userDTO=BeanUtil.copyProperties(user,UserDTO.class);
        Map<String, Object> redisMap=BeanUtil.beanToMap(userDTO, new HashMap<>(),
            CopyOptions.create()
                .setIgnoreNullValue(true)
                .setFieldValueEditor((fieldName, fieldValue) -> fieldValue.toString()));
        stringRedisTemplate.opsForHash().putAll(redisToken,redisMap);
        stringRedisTemplate.expire(redisToken,RedisConstants.LOGIN_USER_TTL,TimeUnit.MINUTES);
        return Result.ok(token);
    }

    public static User createUser(String phone){
        User user=new User();
        user.setPhone(phone);
        user.setNickName(RandomUtil.randomString(10));
        user.setPassword(null);
        user.setIcon(null);
        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());
        return user;
    }
}
