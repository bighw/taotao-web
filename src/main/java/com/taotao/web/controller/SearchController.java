package com.taotao.web.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.taotao.common.bean.HttpResult;
import com.taotao.common.service.ApiService;
import com.taotao.web.bean.Item;

@Controller
public class SearchController {

    @Autowired
    private ApiService apiService;

    private static final Integer PAGE_SIZE = 32;

    private static final ObjectMapper MAPPER = new ObjectMapper();
    
    private static final String URL = "http://search.taotao.com/search";

    @RequestMapping(value = "search", method = RequestMethod.GET)
    public ModelAndView search(@RequestParam("q") String keywords,
            @RequestParam(value = "page", defaultValue = "1") Integer page) {
        ModelAndView mv = new ModelAndView("search");;
        try {
            
            keywords = new String(keywords.getBytes("ISO-8859-1"), "UTF-8");
            
            mv.addObject("query", keywords);

            Map<String, Object> params = new HashMap<String, Object>();
            params.put("keyWords", keywords);
            params.put("page", page);
            params.put("rows", PAGE_SIZE);
            
            HttpResult httpResult = this.apiService.doPost(URL, params);
            if(httpResult.getCode() != 200){
                return mv;//直接返回
            }
            String jsonData = httpResult.getContent();
            JsonNode jsonNode = MAPPER.readTree(jsonData);

            
            mv.addObject("itemList", formatToList(jsonData, Item.class));

            // 计算总页数
            Integer total = jsonNode.get("total").intValue();
            mv.addObject("totalPages", (total + PAGE_SIZE - 1) / PAGE_SIZE);

            // 当前页
            mv.addObject("page", page);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return mv;

    }
    
    public static <T> List<T> formatToList(String jsonData, Class<T> clazz) {
        try {
            JsonNode jsonNode = MAPPER.readTree(jsonData);
            JsonNode data = jsonNode.get("list");
            List<T> list = null;
            if (data.isArray() && data.size() > 0) {
                list = MAPPER.readValue(data.traverse(),
                        MAPPER.getTypeFactory().constructCollectionType(List.class, clazz));
            }
            return list;
        } catch (Exception e) {
            return null;
        }
    }
}
