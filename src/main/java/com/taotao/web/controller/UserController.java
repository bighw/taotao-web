package com.taotao.web.controller;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.taotao.web.service.UserService;
import com.taotao.web.util.CookieUtils;

@RequestMapping("user")
@Controller
public class UserController {

    public static final String COOKIE_TICKET = "TT_TICKET";

    @Autowired
    private UserService userService;

    @RequestMapping(value = "register", method = RequestMethod.GET)
    public String register() {
        return "register";
    }

    @RequestMapping(value = "login", method = RequestMethod.GET)
    public String login() {
        return "login";
    }

    /**
     * 注册
     * 
     * @param username
     * @param password
     * @param phone
     * @return
     */
    @RequestMapping(value = "doRegister", method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity<Map<String, Object>> doRegister(@RequestParam("username") String username,
            @RequestParam("password") String password, @RequestParam("phone") String phone) {
        Boolean bool = this.userService.doRegister(username, password, phone);
        Map<String, Object> result = new HashMap<String, Object>();
        if (bool) {
            result.put("status", "200");
            return ResponseEntity.ok(result);
        } else {
            return ResponseEntity.ok(null);
        }
    }

    @RequestMapping(value = "doLogin", method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity<Map<String, Object>> doLogin(@RequestParam("username") String username,
            @RequestParam("password") String password, HttpServletRequest request,
            HttpServletResponse response,
            @CookieValue(value = CartController.CART_COOKIE_KEY, required = false) String cartKey) {
        Map<String, Object> result = new HashMap<String, Object>();
        String ticket = this.userService.doLogin(username, password, cartKey);
        if (ticket != null) {
            // 登录成功
            result.put("status", 200);
            // 将ticket写入到cookie中
            CookieUtils.setCookie(request, response, COOKIE_TICKET, ticket);
        } else {
            result.put("status", 400);
        }
        return ResponseEntity.ok(result);

    }

}
