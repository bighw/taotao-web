package com.taotao.web.interceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import com.taotao.common.service.RedisService;
import com.taotao.web.controller.CartController;
import com.taotao.web.service.CartRedisService;
import com.taotao.web.service.UserService;
import com.taotao.web.util.CookieUtils;

public class ItemHandlerInterceptor implements HandlerInterceptor {

    @Autowired
    private UserService userService;

    @Autowired
    private RedisService redisService;

    @Autowired
    private CartRedisService cartRedisService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        // 刷新购物车的cookie和redis中的数据的生存时间
        String cookieValue = CookieUtils.getCookieValue(request, "TT_CRART_FLUSH");
        if (cookieValue == null) {
            // 今天没刷新
            String ttCart = CookieUtils.getCookieValue(request, CartController.CART_COOKIE_KEY);
            this.cartRedisService.flush(ttCart);
            CookieUtils.setCookie(request, response, CartController.CART_COOKIE_KEY, ttCart,
                    CartRedisService.SECONDS);
            CookieUtils.setCookie(request, response, "TT_CRART_FLUSH", "true", 60 * 60 * 24);
        }
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
