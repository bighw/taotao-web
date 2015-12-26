package com.taotao.web.mq.handler;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.taotao.common.service.RedisService;
import com.taotao.web.service.ItemService;

@Component
public class ItemHandler {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Autowired
    private RedisService redisService;

    private static final Logger LOGGER = LoggerFactory.getLogger(ItemHandler.class);

    /**
     * 处理商品消息
     * 
     * @param msg
     * @throws Exception
     * @throws
     */
    public void handler(String msg) throws Exception {
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("接收到的消息为：{}", msg);
        }
        JsonNode jsonNode = MAPPER.readTree(msg);
        String type = jsonNode.get("type").asText();
        if (StringUtils.equals(type, "update") || StringUtils.equals(type, "delete")) {
            Long id = jsonNode.get("id").asLong();
            // 删除缓存
            this.redisService.del(ItemService.REDIS_ITEM_KEY + id);
        }
    }

}
