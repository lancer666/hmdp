package com.hmdp.config;

import com.hmdp.interceptor.LoginInterceptor;
import javax.annotation.Resource;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class MvcConfig implements WebMvcConfigurer {
    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 登录拦截器
        registry.addInterceptor(new LoginInterceptor(stringRedisTemplate))
            .excludePathPatterns(
                "/user/code",      // 发送验证码
                "/user/login",     // 登录接口
                "/blog/hot",       // 热门博客（首页展示）
                "/shop/**",        // 商户信息（首页展示）
                "/shop-type/**",   // 商户类型（首页展示）
                "/voucher/**",     // 优惠券信息
                "/upload/**",      // 文件上传
                "/error"           // 错误页面
            ).order(1);
    }
}