package com.taotao.web.service;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.taotao.common.bean.HttpResult;
import com.taotao.common.service.ApiService;
import com.taotao.web.bean.Order;

@Service
public class OrderService {

    @Autowired
    private ApiService apiService;

    @Value("${TAOTAO_ORDER_URL}")
    private String TAOTAO_ORDER_URL;

    private static final ObjectMapper MAPPER = new ObjectMapper();

    /**
     * 调用订单系统接口实现下单功能
     * 
     * @param order
     * @return
     */
    public String submitOrder(Order order) {
        String url = TAOTAO_ORDER_URL + "/order/create";
        try {
            HttpResult httpResult = this.apiService.doPostJson(url, MAPPER.writeValueAsString(order));
            if (httpResult.getCode() == 200) {
                String jsonData = httpResult.getContent();
                JsonNode jsonNode = MAPPER.readTree(jsonData);
                if (jsonNode.has("status") && jsonNode.get("status").asInt() == 200) {
                    // 订单创建成功
                    return jsonNode.get("data").asText();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        // 订单创建失败
        return null;
    }

    public Order queryOrderById(String orderId) {
        try {
            String url = TAOTAO_ORDER_URL + "/order/query/" + orderId;
            String jsonData = this.apiService.doGet(url);
            if (StringUtils.isNotBlank(jsonData)) {
                return MAPPER.readValue(jsonData, Order.class);
            }
        } catch (Exception e) {
            // TODO
            e.printStackTrace();
        }
        return null;
    }

}
