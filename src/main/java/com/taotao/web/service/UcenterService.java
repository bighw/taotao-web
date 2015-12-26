package com.taotao.web.service;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.taotao.common.bean.HttpResult;
import com.taotao.common.service.ApiService;
import com.taotao.web.threadlocal.UserThreadLocal;
import com.taotao.web.util.JsonUtil;

@Service
public class UcenterService {

    @Value("${TAOTAO_ORDER_URL}")
    private String TAOTAO_ORDER_URL;

    @Value("${TAOTAO_SEARCH_URL}")
    private String TAOTAO_SEARCH_URL;

    @Autowired
    private ApiService apiService;

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @SuppressWarnings("unchecked")
    public Map<String, Object> queryOrders(Integer page, Integer rows) {
        String url = TAOTAO_ORDER_URL + "/order/query/" + UserThreadLocal.get().getUsername() + "/" + page
                + "/" + rows;
        try {
            String jsonData = this.apiService.doGet(url);
            if (jsonData == null) {
                return new HashMap<String, Object>(0);
            }
            return MAPPER.readValue(jsonData, Map.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new HashMap<String, Object>(0);
    }

    public Map<String, Object> search(String keyWords, Integer page, Integer rows) {
        String url = TAOTAO_SEARCH_URL + "/order/search";
        try {
            HttpResult httpResult = this.apiService.doPost(
                    url,
                    JsonUtil.start("keyWords", keyWords).put("userId", UserThreadLocal.get().getId())
                            .put("page", page).put("rows", rows).get());
            if (httpResult.getCode() != 200) {
                return new HashMap<String, Object>(0);
            }
            JsonUtil jsonUtil = JsonUtil.getInstance();
            String jsonData = httpResult.getContent();
            JsonNode jsonNode = MAPPER.readTree(jsonData);
            jsonUtil.put("total", jsonNode.get("total").asLong());

            ArrayNode orderIds = (ArrayNode) jsonNode.get("list");
            Map<String, Object> data = new LinkedHashMap<String, Object>();
            for (JsonNode node : orderIds) {
                String orderId = node.get("orderId").asText();
                if(data.containsKey(orderId)){
                    continue;
                }
                // 通过订单号查询订单
                data.put(orderId,queryOrderByOrderId(orderId));
            }
            jsonUtil.put("data", data.values());

            return jsonUtil.get();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new HashMap<String, Object>(0);
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> queryOrderByOrderId(String orderId) {
        String url = TAOTAO_ORDER_URL + "/order/query/" + orderId;
        try {
            String jsonData = this.apiService.doGet(url);
            if (jsonData == null) {
                return new HashMap<String, Object>(0);
            }
            return MAPPER.readValue(jsonData, Map.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new HashMap<String, Object>(0);
    }
}
