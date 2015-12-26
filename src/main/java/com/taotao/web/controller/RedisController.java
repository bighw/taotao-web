package com.taotao.web.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.taotao.common.service.RedisService;
import com.taotao.web.service.IndexService;

@RequestMapping("redis")
@Controller
public class RedisController {

    @Autowired
    private RedisService redisService;

    /**
     * 删除redis中的大广告位数据
     * 
     * @return
     */
    @RequestMapping("index/big/ad")
    public ResponseEntity<Void> deleteIndexAdCache() {
        this.redisService.del(IndexService.REDIS_INDEX_BIG_AD_KEY);
        return ResponseEntity.ok(null);
    }

}
