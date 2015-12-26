package com.taotao.web.controller;

import java.util.HashMap;
import java.util.Map;

import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.taotao.web.bean.Item;
import com.taotao.web.bean.Order;
import com.taotao.web.bean.User;
import com.taotao.web.service.CartService;
import com.taotao.web.service.ItemService;
import com.taotao.web.service.OrderService;
import com.taotao.web.service.UserService;
import com.taotao.web.threadlocal.UserThreadLocal;

@RequestMapping("order")
@Controller
public class OrderController {

    @Autowired
    private ItemService itemService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private UserService userService;

    @Autowired
    private CartService cartService;

    /**
     * 去订单确认页
     * 
     * @param itemId
     * @return
     */
    @RequestMapping(value = "{itemId}", method = RequestMethod.GET)
    public ModelAndView toOrder(@PathVariable("itemId") Long itemId) {
        ModelAndView mv = new ModelAndView();
        mv.setViewName("order");// 视图名

        Item item = this.itemService.queryItemById(itemId);
        // 添加模型数据
        mv.addObject("item", item);
        return mv;
    }

    /**
     * 购物车去订单确认页
     * 
     * @param itemId
     * @return
     */
    @RequestMapping(value = "create", method = RequestMethod.POST)
    public ModelAndView toCartOrder(@RequestParam(value = "itemIds", defaultValue = "0") Long[] itemIds) {
        ModelAndView mv = new ModelAndView();
        mv.setViewName("order-cart");// 视图名

        if(itemIds.length == 1 && itemIds[0].intValue() == 0){
            //全选
            // 添加模型数据
            mv.addObject("carts", this.cartService.queryCartList());
        }else{
            mv.addObject("carts", this.cartService.queryCartListByItemIds(itemIds));
        }
        
        return mv;
    }

    /**
     * 提交订单
     * 
     * @param order
     * @return
     */
    @RequestMapping(value = "submit", method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity<Map<String, Object>> submitOrder(Order order) {
        // 设置当前登录的用户信息
        User user = UserThreadLocal.get();
        order.setUserId(user.getId());
        order.setBuyerNick(user.getUsername());

        Map<String, Object> result = new HashMap<String, Object>();
        String orderId = this.orderService.submitOrder(order);
        if (orderId == null) {
            // 失败
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
        result.put("status", 200);
        result.put("data", orderId);
        return ResponseEntity.ok(result);
    }

    /**
     * 下单成功提示页
     * 
     * @param orderId
     * @return
     */
    @RequestMapping(value = "success", method = RequestMethod.GET)
    public ModelAndView success(@RequestParam("id") String orderId) {
        ModelAndView mv = new ModelAndView("success");
        Order order = this.orderService.queryOrderById(orderId);
        mv.addObject("order", order);
        mv.addObject("date", new DateTime().plusDays(2).toString("MM月dd日"));
        return mv;
    }

}
