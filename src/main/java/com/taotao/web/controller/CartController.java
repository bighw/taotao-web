package com.taotao.web.controller;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.taotao.web.bean.Cart;
import com.taotao.web.bean.User;
import com.taotao.web.service.CartRedisService;
import com.taotao.web.service.CartService;
import com.taotao.web.threadlocal.UserThreadLocal;
import com.taotao.web.util.CookieUtils;

@RequestMapping("cart")
@Controller
public class CartController {

    @Autowired
    private CartService cartService;

    // @Autowired
    // private CartCookieService cartCookieService;

    @Autowired
    private CartRedisService cartRedisService;

    public static final String CART_COOKIE_KEY = "TT_CART";

    /**
     * 添加商品到购物车
     * 
     * @param itemId
     * @return
     */
    @RequestMapping(value = "add/{itemId}", method = RequestMethod.POST)
    public String add(@PathVariable("itemId") Long itemId,
            @RequestParam(value = "num", defaultValue = "1") Integer num, HttpServletRequest request,
            HttpServletResponse response,
            @CookieValue(value = CART_COOKIE_KEY, required = false) String cartKey) {
        // 判断用户是否登录
        User user = UserThreadLocal.get();
        if (user == null) {
            if (cartKey == null) {
                // 第一次加入购物车，生成key
                cartKey = DigestUtils.md5Hex(itemId + "" + System.currentTimeMillis());
                // 将key保存到cookie中
                CookieUtils.setCookie(request, response, CART_COOKIE_KEY, cartKey, CartRedisService.SECONDS);
            }
            // 未登录
            this.cartRedisService.addItemToCart(itemId, num, cartKey);
        } else {
            // 已登录
            this.cartService.addItemToCart(itemId, num);
        }
        return "redirect:/cart/show.html";
    }

    /**
     * 显示购物车商品数据
     * 
     * @return
     */
    @RequestMapping(value = "show", method = RequestMethod.GET)
    public ModelAndView showCart(@CookieValue(value = CART_COOKIE_KEY, required = false) String cartKey) {
        List<Cart> carts = null;
        // 判断用户是否登录
        User user = UserThreadLocal.get();
        if (user == null) {
            // 未登录
            carts = this.cartRedisService.getCartList(cartKey);
        } else {
            // 已登录
            carts = this.cartService.queryCartList();
        }

        ModelAndView mv = new ModelAndView("cart");
        mv.addObject("cartList", carts);
        return mv;
    }

    /**
     * 更新商品数量
     * 
     * @param itemId
     * @param num
     * @return
     */
    @RequestMapping(value = "update/{itemId}/{num}", method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity<Void> updateItemNum(@PathVariable("itemId") Long itemId,
            @PathVariable("num") Integer num,
            @CookieValue(value = CART_COOKIE_KEY, required = false) String cartKey) {
        // 判断用户是否登录
        User user = UserThreadLocal.get();
        if (user == null) {
            // 未登录
            this.cartRedisService.updateItemNum(itemId, num, cartKey);
        } else {
            // 已登录
            this.cartService.updateItemNum(itemId, num);
        }
        return ResponseEntity.ok(null);
    }

    /**
     * 从购物车中删除商品数据
     * 
     * @param itemId
     * @return
     */
    @RequestMapping(value = "delete/{itemId}", method = RequestMethod.GET)
    public String deleteItem(@PathVariable("itemId") Long itemId,
            @CookieValue(value = CART_COOKIE_KEY, required = false) String cartKey) {
        // 判断用户是否登录
        User user = UserThreadLocal.get();
        if (user == null) {
            // 未登录
            this.cartRedisService.deleteItem(itemId, cartKey);
        } else {
            // 已登录
            this.cartService.deleteItem(itemId);
        }
        return "redirect:/cart/show.html";
    }
}
