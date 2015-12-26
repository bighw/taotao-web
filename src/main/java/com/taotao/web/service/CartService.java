package com.taotao.web.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.taotao.common.bean.HttpResult;
import com.taotao.common.service.ApiService;
import com.taotao.web.bean.Cart;
import com.taotao.web.bean.Item;
import com.taotao.web.bean.TaotaoResult;
import com.taotao.web.threadlocal.UserThreadLocal;

@Service
public class CartService {

    @Autowired
    private ApiService apiService;

    @Autowired
    private ItemService itemService;

    @Value("${CART_ADD_API_URL}")
    private String CART_ADD_API_URL;

    @Value("${CART_QUERY_API_URL}")
    private String CART_QUERY_API_URL;

    @Value("${CART_UPDATE_API_URL}")
    private String CART_UPDATE_API_URL;

    @Value("${CART_DELETE_API_URL}")
    private String CART_DELETE_API_URL;

    private static final ObjectMapper MAPPER = new ObjectMapper();

    /**
     * 添加商品到购物车（持久化）
     * 
     * @param itemId
     * @return
     */
    public Boolean addItemToCart(Long itemId,Integer num) {
        try {
            Map<String, Object> params = new HashMap<String, Object>();
            params.put("userId", UserThreadLocal.get().getId());
            params.put("itemId", itemId);
            Item item = this.itemService.queryItemById(itemId);
            params.put("itemTitle", item.getTitle());
            params.put("itemImage", item.getImages()[0]);
            params.put("itemPrice", item.getPrice());
            params.put("num", num);
            HttpResult httpResult = this.apiService.doPost(CART_ADD_API_URL, params);
            if (httpResult.getCode() == 200) {
                JsonNode jsonNode = MAPPER.readTree(httpResult.getContent());
                Integer status = jsonNode.get("status").intValue();
                return status == 200 || status == 202;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @SuppressWarnings("unchecked")
    public List<Cart> queryCartList() {
        try {
            String jsonData = this.apiService.doGet(CART_QUERY_API_URL + UserThreadLocal.get().getId());
            TaotaoResult taotaoResult = TaotaoResult.formatToList(jsonData, Cart.class);
            return (List<Cart>) taotaoResult.getData();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    
    @SuppressWarnings("unchecked")
    public List<Cart> queryCartListByItemIds(Long[] itemIds) {
        try {
            Map<String, Object> params = new HashMap<String, Object>(1);
            params.put("itemIds", StringUtils.join(itemIds, ','));
            HttpResult httpResult = this.apiService.doPost(CART_QUERY_API_URL + UserThreadLocal.get().getId(),
                    params);
            TaotaoResult taotaoResult = TaotaoResult.formatToList(httpResult.getContent(), Cart.class);
            return (List<Cart>) taotaoResult.getData();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 更新商品数量
     * 
     * @param itemId
     * @param num
     * @return
     */
    public Boolean updateItemNum(Long itemId, Integer num) {
        try {
            String url = CART_UPDATE_API_URL + UserThreadLocal.get().getId() + "/" + itemId + "/" + num;
            HttpResult httpResult = this.apiService.doPost(url, null);
            if (httpResult.getCode() != 200) {
                return false;
            }
            TaotaoResult taotaoResult = TaotaoResult.format(httpResult.getContent());
            if (taotaoResult.getStatus() == 200) {
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 删除商品
     * 
     * @param itemId
     * @return
     */
    public Boolean deleteItem(Long itemId) {
        try {
            String url = CART_DELETE_API_URL + UserThreadLocal.get().getId() + "/" + itemId;
            HttpResult httpResult = this.apiService.doPost(url, null);
            if (httpResult.getCode() != 200) {
                return false;
            }
            TaotaoResult taotaoResult = TaotaoResult.format(httpResult.getContent());
            if (taotaoResult.getStatus() == 200) {
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

}
