package org.example.bookmall.interceptor;

import org.springframework.web.servlet.HandlerInterceptor;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.Arrays;
import java.util.List;

/**
 * 用户登录拦截器：适配你的用户端代码，精准拦截/放行
 */
public class UserLoginInterceptor implements HandlerInterceptor {

    // 定义需要放行的接口列表
    private static final List<String> EXCLUDE_PATHS = Arrays.asList(
            "/user/register",      // 注册接口
            "/user/login",         // 登录接口
            "/user/searchBook",    // 搜索图书
            "/user/getHotBooks",   // 热销榜TOP10
            "/user/getAllHotBooks", // 全部热销图书
            "/user/getBookById",   // 图书详情
            "/user/get",           // 用户查询
            "/user/getAllBooks"    // 获取所有图书（分页）
    );

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 1. 获取当前请求路径
        String requestURI = request.getRequestURI();

        // 2. 检查是否是需要放行的接口
        for (String excludePath : EXCLUDE_PATHS) {
            if (requestURI.contains(excludePath)) {
                return true; // 直接放行
            }
        }

        // 3. 对于HTML页面也放行（让用户可以先浏览页面）
        if (requestURI.endsWith(".html") ||
                requestURI.contains("/static/") ||
                requestURI.contains("/css/") ||
                requestURI.contains("/js/") ||
                requestURI.contains("/images/")) {
            return true;
        }

        // 4. 检查登录状态
        HttpSession session = request.getSession(false); // 不创建新session
        if (session == null || session.getAttribute("LOGIN_USER") == null) {
            // 未登录：如果是AJAX请求，返回JSON错误
            if (isAjaxRequest(request)) {
                response.setContentType("application/json;charset=UTF-8");
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("{\"code\":401,\"msg\":\"未登录或登录已过期\"}");
                return false;
            }

            // 普通请求：重定向到登录页
            response.setContentType("text/html;charset=UTF-8");
            String loginUrl = request.getContextPath() + "/user/login.html";
            response.sendRedirect(loginUrl);
            return false;
        }

        // 5. 已登录：放行
        return true;
    }

    /**
     * 判断是否为AJAX请求
     */
    private boolean isAjaxRequest(HttpServletRequest request) {
        String requestedWith = request.getHeader("X-Requested-With");
        return "XMLHttpRequest".equals(requestedWith) ||
                "application/json".equals(request.getContentType()) ||
                request.getHeader("Accept") != null && request.getHeader("Accept").contains("application/json");
    }
}