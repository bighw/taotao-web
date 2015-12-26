package com.taotao.web.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.taotao.common.service.RedisService;
import com.taotao.web.bean.Cart;
import com.taotao.web.bean.Item;

/**
 * 将购物车中的商品数据保存到Redis中
 */
@Service
public class CartRedisService {

    private static final Logger LOGGER = LoggerFactory.getLogger(CartRedisService.class);

    @Autowired
    private RedisService redisService;

    @Autowired
    private ItemService itemService;

    private static final ObjectMapper MAPPER = new ObjectMapper();

    public static final String KEY_STR = "CART_";

    public static final Integer SECONDS = 60 * 60 * 24 * 30;

    public void addItemToCart(Long itemId, Integer num, String cartKey) {
        // 判断该商品是否存在购物车中
        String key = getKey(cartKey);
        String itemIdStr = String.valueOf(itemId);
        String value = this.redisService.hget(key, itemIdStr);
        try {
            Cart cart = null;
            if (value == null) {
                // 该商品不存在
                // 查询商品数据
                Item item = this.itemService.queryItemById(itemId);
                // 写入数据
                cart = new Cart();
                cart.setItemId(itemId);
                cart.setItemImage(item.getImages()[0]);
                cart.setItemPrice(item.getPrice());
                cart.setItemTitle(item.getTitle());
                cart.setNum(num);
                cart.setCreated(new Date());
                cart.setUpdated(cart.getCreated());
            } else {
                // 存在，数量增加
                cart = MAPPER.readValue(value, Cart.class);
                cart.setNum(cart.getNum() + num);
                cart.setUpdated(new Date());
            }
            this.redisService.hset(key, itemIdStr, MAPPER.writeValueAsString(cart), SECONDS);
            // 记录数据的最后访问时间
            this.redisService.hset(key, "updated", String.valueOf(System.currentTimeMillis()));
        } catch (Exception e) {
            LOGGER.error("加入商品到购物车失败! itemId = " + itemId + ", cartKey = " + cartKey, e);
        }
    }

    public List<Cart> getCartList(String cartKey) {
        String key = getKey(cartKey);
        Map<String, String> map = this.redisService.hgetAll(key);
        List<Cart> carts = new ArrayList<Cart>(map.size());
        try {
            for (Map.Entry<String, String> entry : map.entrySet()) {
                if (!StringUtils.equals(entry.getKey(), "updated")) {
                    carts.add(MAPPER.readValue(entry.getValue(), Cart.class));
                }
            }
            // 设置生存时间
            this.redisService.expire(key, SECONDS);
            // 记录数据的最后访问时间
            this.redisService.hset(key, "updated", String.valueOf(System.currentTimeMillis()));
        } catch (Exception e) {
            LOGGER.error("查询购物车失败! cartKey = " + cartKey, e);
        }
        return carts;
    }

    /**
     * 刷新生存时间
     * 
     * @param ttCart
     */
    public void flush(String cartKey) {
        this.redisService.expire(getKey(cartKey), SECONDS);
    }

    private String getKey(String cartKey) {
        return KEY_STR + cartKey;
    }

    /**
     * 更新商品数量
     * 
     * @param itemId
     * @param num
     * @param cartKey
     */
    public void updateItemNum(Long itemId, Integer num, String cartKey) {
        String key = getKey(cartKey);
        String itemIdStr = String.valueOf(itemId);
        String value = this.redisService.hget(key, itemIdStr);
        if(value == null){
            return ;
        }
        try {
            Cart cart = MAPPER.readValue(value, Cart.class);
            cart.setNum(num);
            cart.setUpdated(new Date());
            this.redisService.hset(key, itemIdStr, MAPPER.writeValueAsString(cart), SECONDS);
        } catch (Exception e) {
            //TODO
            e.printStackTrace();
        }
    }

    /**
     * 删除商品
     * 
     * @param itemId
     * @param cartKey
     */
    public void deleteItem(Long itemId, String cartKey) {
        String key = getKey(cartKey);
        String itemIdStr = String.valueOf(itemId);
        this.redisService.hdel(key, itemIdStr);
        this.flush(cartKey);
    }
}
