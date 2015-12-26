package com.taotao.web.interceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import com.taotao.web.bean.User;
import com.taotao.web.controller.UserController;
import com.taotao.web.service.UserService;
import com.taotao.web.threadlocal.UserThreadLocal;
import com.taotao.web.util.CookieUtils;

public class UserLoginHandlerInterceptor implements HandlerInterceptor {

    @Autowired
    private UserService userService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        // 如何检查用户是否登录
        String ticket = CookieUtils.getCookieValue(request, UserController.COOKIE_TICKET);
        if (StringUtils.isBlank(ticket)) {
            // 未登录
            response.sendRedirect("/user/login.html");
            UserThreadLocal.clear();//未登录清空ThreadLocal中的数据
            return false;
        }

        User user = this.userService.queryUserByTicket(ticket);
        if (user == null) {
            // 登录超时
            response.sendRedirect("/user/login.html");
            UserThreadLocal.clear();
            return false;
        }
        
        //将user保存到ThreadLocal中
        UserThreadLocal.set(user);
        
        //登录成功
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
            ModelAndView modelAndView) throws Exception {

    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler,
            Exception ex) throws Exception {

    }

}
