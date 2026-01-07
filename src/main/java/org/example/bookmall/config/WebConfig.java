package org.example.bookmall.config;

import org.example.bookmall.interceptor.AdminLoginInterceptor;
import org.example.bookmall.interceptor.UserLoginInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 1. 管理员拦截器
        registry.addInterceptor(new AdminLoginInterceptor())
                .addPathPatterns("/admin/**") // 拦截/admin开头的接口
                // 排除：登录接口 + 所有.html静态页面
                .excludePathPatterns(
                        "/admin/login",  // 登录接口
                        "/admin/checkLogin",
                        "/admin/**/*.html"// 排除/admin下的所有.html页面
                );

        // 2. 用户拦截器 - 简化配置，具体放行逻辑在拦截器内部处理
        registry.addInterceptor(new UserLoginInterceptor())
                .addPathPatterns("/user/**") // 拦截所有/user/开头的请求
                // 只放行静态资源
                .excludePathPatterns(
                        "/user/login.html",
                        "/user/register.html",
                        "/user/**/*.css",
                        "/user/**/*.js",
                        "/user/**/*.png",
                        "/user/**/*.jpg",
                        "/user/**/*.jpeg",
                        "/user/**/*.gif"
                );
    }

    // 添加静态资源映射
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 获取项目根目录
        String projectPath = System.getProperty("user.dir");

        // 映射上传的封面图片
        String coverUploadPath = "file:" + projectPath + "/upload/cover/";
        registry.addResourceHandler("/upload/cover/**")
                .addResourceLocations(coverUploadPath);

        // 如果需要，可以添加其他静态资源映射
        // registry.addResourceHandler("/static/**")
        //         .addResourceLocations("classpath:/static/");
    }
}