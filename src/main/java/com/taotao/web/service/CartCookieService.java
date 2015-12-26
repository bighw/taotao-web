package com.taotao.web.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.taotao.web.bean.Cart;
import com.taotao.web.bean.Item;
import com.taotao.web.util.CookieUtils;

@Service
public class CartCookieService {

    public static final String CART_COOKIE_KEY = "TT_CART";

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Autowired
    private ItemService itemService;

    public void addItemToCart(Long itemId, HttpServletRequest request, HttpServletResponse response) {
        // 读取原有的购物车数据
        List<Cart> carts = this.getCartList(request);

        Cart cart = null;

        // 判断当前的商品是否在集合中，如果在数量加一，如果不在直接添加
        for (Cart c : carts) {
            if (c.getItemId().equals(itemId)) {
                // 已经存在
                c.setNum(c.getNum() + 1);// TODO 待完成指定购买商品的数量
                cart = c;
                break;
            }
        }
        if (cart == null) {
            // 不存在
            Item item = this.itemService.queryItemById(itemId);
            cart = new Cart();
            cart.setItemId(itemId);
            cart.setItemImage(item.getImages()[0]);
            cart.setItemPrice(item.getPrice());
            cart.setItemTitle(item.getTitle());
            cart.setNum(1);// TODO 默认为1
            cart.setCreated(new Date());
            cart.setUpdated(cart.getCreated());
            carts.add(cart);
        }

        saveCartList(carts, request, response);

    }

    private void saveCartList(List<Cart> carts, HttpServletRequest request, HttpServletResponse response) {
        try {
            // 将商品集合写入到cookie中
            CookieUtils.setCookie(request, response, CART_COOKIE_KEY, MAPPER.writeValueAsString(carts),
                    60 * 60 * 24 * 30, true);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }

    public List<Cart> getCartList(HttpServletRequest request) {
        try {
            String cartsJson = CookieUtils.getCookieValue(request, CART_COOKIE_KEY, true);
            return MAPPER.readValue(cartsJson,
                    MAPPER.getTypeFactory().constructCollectionType(List.class, Cart.class));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ArrayList<Cart>();
    }

    public void updateItemNum(Long itemId, Integer num, HttpServletRequest request,
            HttpServletResponse response) {
        // 读取原有的购物车数据
        List<Cart> carts = this.getCartList(request);

        // 判断当前的商品是否在集合中，如果在数量加一，如果不在直接添加
        for (Cart c : carts) {
            if (c.getItemId().equals(itemId)) {
                // 已经存在
                c.setNum(num);// TODO 待完成指定购买商品的数量
                break;
            }
        }

        saveCartList(carts, request, response);

    }

    public void deleteItem(Long itemId, HttpServletRequest request, HttpServletResponse response) {
        // 读取原有的购物车数据
        List<Cart> carts = this.getCartList(request);

        // 判断当前的商品是否在集合中，如果在数量加一，如果不在直接添加
        for (Cart c : carts) {
            if (c.getItemId().equals(itemId)) {
                // 已经存在
                carts.remove(c);
                break;
            }
        }

        saveCartList(carts, request, response);
    }

}
