package com.example.mycoffeedemo.controller;


import com.example.mycoffeedemo.dto.OrderResponseDTO;
import com.example.mycoffeedemo.service.OrderService;
import com.example.mycoffeedemo.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/cart")
public class CartController {

    @Autowired
    private OrderService orderService;

    /**
     * 获取当前用户的购物车
     * @param  userId 用户id
     * @return 购物车信息
     */
    @GetMapping
    public Result<OrderResponseDTO> getUserCart(@RequestParam Long userId) {
        return orderService.getUserCart(userId);
    }

    /**
     * 添加商品到购物车
     * @param userId 用户ID
     * @param productId 商品ID
     * @param quantity 商品数量
     * @return 操作结果
     */
    @PostMapping("/add")
    public Result<String> addToCart(@RequestParam Long userId,
                                    @RequestParam Long productId,
                                    @RequestParam Integer quantity){
        return orderService.addToCart(userId,productId,quantity);
    }

    /**
     * 更新购物车商品数量
     * @param userId 用户ID
     * @param productId 商品ID
     * @param quantity 新数量
     * @return 操作结果
     */
    @PutMapping("/update")
    public Result<String> updateCartItem(@RequestParam Long userId,
                                         @RequestParam Long productId,
                                         @RequestParam Integer quantity){
        return orderService.updateCartItem(userId,productId,quantity);
    }

    /**
     * 从购物车移除商品
     * @param userId 商品ID
     * @param productId 商品ID
     * @return 操作结果
     */
    @DeleteMapping("/remove")
    public Result<String> removeFromCart(@RequestParam Long userId,@RequestParam Long productId) {
        return orderService.removeFromCart(userId,productId);
    }

    /**
     * 清空购物车
     * @param userId 用户id
     * @return 操作结果
     */
    @DeleteMapping("/clear")
    public Result<String> clearCart(@RequestParam Long userId) {
        return orderService.clearCart(userId);
    }
}
