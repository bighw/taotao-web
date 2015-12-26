package com.taotao.web.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.taotao.common.bean.EasyUIResult;
import com.taotao.common.service.ApiService;
import com.taotao.common.service.RedisService;
import com.taotao.manage.pojo.Content;
import com.taotao.web.util.JsonUtil;

@Service
public class IndexService {

    private static final Logger LOGGER = LoggerFactory.getLogger(IndexService.class);

    @Autowired
    private ApiService apiService;

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Value("${TAOTAO_MANAGE_URL}")
    private String TAOTAO_MANAGE_URL;

    @Value("${INDEX_BIG_AD}")
    private String INDEX_BIG_AD;

    @Autowired
    private RedisService redisService;

    public static final String REDIS_INDEX_BIG_AD_KEY = "TAOTAO_WEB_INDEX_BIG_AD";
    
    public static final String REDIS_INDEX_DATA_KEY = "INDEX_DATA_";

    private static final Integer REDIS_INDEX_BIG_AD_TIME = 60 * 60 * 24;

    /**
     * 获取大广告位数据
     * 
     * @return json数据
     */
    @SuppressWarnings("unchecked")
    public String getIndexBigAd() {
        try {
            // 从缓存中命中
            String value = this.redisService.get(REDIS_INDEX_BIG_AD_KEY);
            if (StringUtils.isNotBlank(value)) {
                return value;
            }
        } catch (Exception e1) {
            e1.printStackTrace();
        }

        // 从后台获取数据，使用Httpclient
        String url = TAOTAO_MANAGE_URL + INDEX_BIG_AD;
        String jsonData;
        try {
            jsonData = this.apiService.doGet(url);
        } catch (Exception e) {
            LOGGER.error("获取首页大广告位数据出错! url = " + url, e);
            return null;
        }

        EasyUIResult easyUIResult = EasyUIResult.formatToList(jsonData, Content.class);
        List<Content> contents = (List<Content>) easyUIResult.getRows();

        // 遍历集合，封装数据前台所需要的数据
        List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
        for (Content content : contents) {
            Map<String, Object> map = new HashMap<String, Object>();

            map.put("srcB", content.getPic());
            map.put("height", 240);
            map.put("alt", "");
            map.put("width", 670);
            map.put("src", content.getPic());
            map.put("widthB", 550);
            map.put("href", content.getUrl());
            map.put("heightB", 240);

            result.add(map);
        }
        String resultJson = null;
        try {
            resultJson = MAPPER.writeValueAsString(result);
        } catch (JsonProcessingException e) {
            LOGGER.error("将result转化为json出错! result = " + result, e);
            return null;
        }

        try {
            // 将结果写入到缓存中
            this.redisService.set(REDIS_INDEX_BIG_AD_KEY, resultJson, REDIS_INDEX_BIG_AD_TIME);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return resultJson;
    }

    /**
     * 首页楼数据
     * 
     * @return
     */
    public String getIndexData() {
        JsonUtil result = JsonUtil.getInstance();
        // 封装楼数据
        createIndexData(result, "20");
        createIndexData(result, "21");
        createIndexData(result, "22");
        createIndexData(result, "23");
        createIndexData(result, "24");
        createIndexData(result, "25");
        createIndexData(result, "26");
        createIndexData(result, "27");
        createIndexData(result, "28");
        createIndexData(result, "29");
        return result.toJson();
    }

    @SuppressWarnings("unchecked")
    private void createIndexData(JsonUtil result, String dataId) {
        // 从后台获取数据，使用Httpclient
        String url = TAOTAO_MANAGE_URL + "/rest/content?categoryId=" + dataId + "&page=1&rows=10";
        String jsonData = null;
        // 添加缓存
        String key = REDIS_INDEX_DATA_KEY + DigestUtils.md5Hex(url);
        try {
            String cacheData = this.redisService.get(key);
            if(StringUtils.isNotBlank(cacheData)){
                jsonData = cacheData;
            }
        } catch (Exception e1) {
            e1.printStackTrace();
        }
        
        if(null == jsonData){
            try {
                jsonData = this.apiService.doGet(url);
            } catch (Exception e) {
                LOGGER.error("获取首页大广告位数据出错! url = " + url, e);
                return;
            }
            
            //将结果数据写入到缓存中
            this.redisService.set(key, jsonData, 60 * 60 * 24);
        }

        EasyUIResult easyUIResult = EasyUIResult.formatToList(jsonData, Content.class);
        List<Content> contents = (List<Content>) easyUIResult.getRows();
        if (contents == null) {
            return;
        }
        
        JsonUtil jsonUtil = JsonUtil.getInstance();
        for (int i = 0; i < contents.size(); i++) {
            Content content = contents.get(i);
            jsonUtil.put(
                    String.valueOf(i + 1),
                    JsonUtil.start("d", content.getPic()).put("e", content.getUrl())
                            .put("c", content.getSubTitle()).put("a", content.getTitleDesc())
                            .put("b", content.getTitle()).put("f", "1").get());
        }
        result.put(dataId, jsonUtil.get());
    }

}
