package org.example.bookmall.interceptor;

import org.example.bookmall.entity.User;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.PrintWriter;

// 拦截器：校验管理员是否登录
public class AdminLoginInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 1. 获取Session
        HttpSession session = request.getSession();
        // 2. 校验Session中是否有管理员信息（Key与登录接口一致）
        User loginAdmin = (User) session.getAttribute("LOGIN_ADMIN");

        // 3. 未登录：返回401未授权，阻止接口访问
        if (loginAdmin == null) {
            response.setContentType("application/json;charset=UTF-8");
            PrintWriter out = response.getWriter();
            out.write("{\"success\":false,\"msg\":\"请先登录管理员账号\"}");
            out.flush();
            out.close();
            return false;
        }
        // 4. 已登录：放行接口
        return true;
    }
}