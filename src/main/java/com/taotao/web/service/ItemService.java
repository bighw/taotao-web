package com.taotao.web.service;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.taotao.common.service.ApiService;
import com.taotao.common.service.RedisService;
import com.taotao.manage.pojo.ItemDesc;
import com.taotao.manage.pojo.ItemParamItem;
import com.taotao.web.bean.Item;

@Service
public class ItemService {

    @Autowired
    private ApiService apiService;

    @Value("${TAOTAO_MANAGE_URL}")
    private String TAOTAO_MANAGE_URL;

    @Autowired
    private RedisService redisService;

    private static final ObjectMapper MAPPER = new ObjectMapper();

    public static final String REDIS_ITEM_KEY = "TAOTAO_WEB_ITEM_";

    public Item queryItemById(Long itemId) {
        // 从缓存中命中
        String key = REDIS_ITEM_KEY + itemId;
        try {
            String cacheData = this.redisService.get(key);
            if (StringUtils.isNotBlank(cacheData)) {
                return MAPPER.readValue(cacheData, Item.class);
            }
        } catch (Exception e1) {
            // TODO
            e1.printStackTrace();
        }
        try {
            String url = TAOTAO_MANAGE_URL + "/rest/item/" + itemId;
            String jsonData = this.apiService.doGet(url);
            Item item = MAPPER.readValue(jsonData, Item.class);

            try {
                // 将结果集写入到缓存中
                this.redisService.set(key, jsonData, 60 * 60 * 24);
            } catch (Exception e) {
                e.printStackTrace();
            }
            
            return item;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 查询商品描述
     * 
     * @param itemId
     * @return
     */
    public ItemDesc queryItemDescByItemId(Long itemId) {
        try {
            String url = TAOTAO_MANAGE_URL + "/rest/item/desc/" + itemId;
            String jsonData = this.apiService.doGet(url);
            ItemDesc itemDesc = MAPPER.readValue(jsonData, ItemDesc.class);
            return itemDesc;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public String queryItemParam(Long itemId) {
        try {
            String url = TAOTAO_MANAGE_URL + "/rest/item/param/item/" + itemId;
            String jsonData = this.apiService.doGet(url);
            ItemParamItem paramItem = MAPPER.readValue(jsonData, ItemParamItem.class);
            String paramData = paramItem.getParamData();// json数据

            // 拼接html
            StringBuilder sb = new StringBuilder();
            sb.append("<table cellpadding=\"0\" cellspacing=\"1\" width=\"100%\" border=\"0\" class=\"Ptable\"><tbody>");

            JsonNode jsonNode = MAPPER.readTree(paramData);
            ArrayNode arrayNode = (ArrayNode) jsonNode;
            for (JsonNode node : arrayNode) {
                sb.append("<tr><th class=\"tdTitle\" colspan=\"2\">" + node.get("group").asText()
                        + "</th></tr>");
                ArrayNode params = (ArrayNode) node.get("params");
                for (JsonNode kv : params) {
                    sb.append("<tr><td class=\"tdTitle\">" + kv.get("k").asText() + "</td><td>"
                            + kv.get("v").asText() + "</td></tr>");
                }
            }
            sb.append("</tbody></table>");
            return sb.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
