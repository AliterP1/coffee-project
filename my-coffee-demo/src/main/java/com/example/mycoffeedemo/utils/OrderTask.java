package com.example.mycoffeedemo.utils;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.example.mycoffeedemo.entity.MerchantOrder;
import com.example.mycoffeedemo.entity.Order;
import com.example.mycoffeedemo.entity.OrderItem;
import com.example.mycoffeedemo.entity.Product;
import com.example.mycoffeedemo.mapper.MerchantOrderMapper;
import com.example.mycoffeedemo.mapper.OrderItemMapper;
import com.example.mycoffeedemo.mapper.OrderMapper;
import com.example.mycoffeedemo.mapper.ProductMapper;
import com.example.mycoffeedemo.service.OrderItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class OrderTask {
    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private OrderItemMapper orderItemMapper;

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private MerchantOrderMapper  merchantOrderMapper;

    /**
     * 每分钟扫描一次订单表，取消过期未支付的订单
     */
    @Transactional
    @Scheduled(fixedRate = 60000)
    public void cancelExpiredOrders() {
        LocalDateTime now = LocalDateTime.now();

        // 查找过期未支付订单
        List<Order> expiredOrders = orderMapper.selectList(
                new QueryWrapper<Order>()
                        .eq("status", "pending")
                        .lt("expire_time", now)
        );

        for (Order order : expiredOrders) {
            // 1. 更新主订单状态为 expired
            order.setStatus("expired");
            orderMapper.updateById(order);
            // 2. 更新对应子订单状态为 expired
            merchantOrderMapper.update(
                    null,
                    new LambdaUpdateWrapper<MerchantOrder>()
                            .eq(MerchantOrder::getOrderId, order.getId())
                            .set(MerchantOrder::getStatus, "expired")
            );
            // 回滚库存
            List<OrderItem> items = orderItemMapper.selectList(
                    new QueryWrapper<OrderItem>().eq("order_id", order.getId())
            );
            for (OrderItem item : items) {
                Product product = productMapper.selectById(item.getProductId());
                if (product != null) {
                    product.setStock(product.getStock() + item.getQuantity());
                    productMapper.updateById(product);
                }
            }
        }
    }
}
