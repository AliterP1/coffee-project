package com.example.mycoffeedemo.controller;


import com.example.mycoffeedemo.service.OrderService;
import com.example.mycoffeedemo.utils.Result;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


@Slf4j
@RestController
@AllArgsConstructor
public class EasyPayController {


    private OrderService orderService;

    @PostMapping("/pay")
    public Result<String> pay(@RequestParam("orderId") String orderId) throws Exception {
        return orderService.aiPay(orderId);

    }

    @PostMapping("/notify")
    public Result<String> notify(HttpServletRequest request) throws Exception {
        log.info("收到支付成功通知");
        System.out.println("收到支付成功开始修改数据库数据");
        return  orderService.getNotify(request);
    }

    @PostMapping("/paid")
    public Result<String> paid(@RequestParam("orderId") String orderId)  {
        return orderService.getPaid(orderId);
    }
}
