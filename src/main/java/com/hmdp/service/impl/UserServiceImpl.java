package com.hmdp.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmdp.dto.LoginFormDTO;
import com.hmdp.dto.Result;
import com.hmdp.entity.User;
import com.hmdp.mapper.UserMapper;
import com.hmdp.service.IUserService;
import com.hmdp.utils.RegexUtils;

import cn.hutool.core.util.RandomUtil;

import java.time.LocalDateTime;

import javax.servlet.http.HttpSession;

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
    @Override
    public Result sendCode(String phone, HttpSession session) {
        if(RegexUtils.isPhoneInvalid(phone)){
            return Result.fail("手机号格式错误");
        }
        String key = RandomUtil.randomString(6);
        session.setAttribute(phone, key);
        log.info("[模拟短信] 手机号={} 验证码={}", phone, key);
        return Result.ok();
    }

    @Override
    public Result login(LoginFormDTO loginForm, HttpSession session) {
        String phone = loginForm.getPhone();
        String code = loginForm.getCode();
        String key = session.getAttribute(phone).toString();
        if(key == null || !key.equals(code)){
            return Result.fail("验证码错误");
        }
        User user=query().eq("phone", phone).one();
        if(user == null){
            user=new User();
            user.setPhone(phone);
            user.setNickName(RandomUtil.randomString(10));
            user.setPassword(null);
            user.setIcon(null);
            user.setCreateTime(LocalDateTime.now());
            user.setUpdateTime(LocalDateTime.now());
            save(user);
        }
        session.setAttribute("user", user);
        return Result.ok(user);
    }
}
