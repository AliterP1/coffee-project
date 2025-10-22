package com.example.mycoffeedemo.service.Impl;

import cn.hutool.extra.qrcode.QrCodeUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.alipay.easysdk.factory.Factory;
import com.alipay.easysdk.payment.facetoface.models.AlipayTradePrecreateResponse;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.mycoffeedemo.dto.CreateOrderRequestDTO;
import com.example.mycoffeedemo.dto.OrderResponseDTO;
import com.example.mycoffeedemo.entity.Address;
import com.example.mycoffeedemo.entity.Order;
import com.example.mycoffeedemo.entity.OrderItem;
import com.example.mycoffeedemo.entity.Product;
import com.example.mycoffeedemo.mapper.AddressMapper;
import com.example.mycoffeedemo.mapper.OrderItemMapper;
import com.example.mycoffeedemo.mapper.OrderMapper;
import com.example.mycoffeedemo.mapper.ProductMapper;
import com.example.mycoffeedemo.service.OrderService;
import com.example.mycoffeedemo.utils.DataUtils;
import com.example.mycoffeedemo.utils.PhoneUtil;
import com.example.mycoffeedemo.utils.Result;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order> implements OrderService {

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private OrderItemMapper orderItemMapper;

    @Autowired
    private AddressMapper addressMapper;

    //创建订单
    @Transactional
    @Override
    public Result<Order> createOrder(CreateOrderRequestDTO dto) {
        BigDecimal totalPrice = BigDecimal.ZERO;

        // 1. 查询收货地址
        Address address = addressMapper.selectById(dto.getAddressId());
        if (address == null || !address.getUserId().equals(dto.getUserId())) {
            return Result.fail("收货地址不存在或不属于该用户");
        }

        // 2. 创建订单对象
        Order order = new Order();
        order.setUserId(dto.getUserId());
        order.setStatus("pending");
        order.setTotalPrice(BigDecimal.ZERO);
        //设置30分钟支付时间
        order.setExpireTime(LocalDateTime.now().plusMinutes(30));

        // 保存收货地址快照
        order.setAddressId(address.getId());
        order.setRecipientName(address.getRecipientName());
        if (!PhoneUtil.isValidPhone(address.getPhone())) {
            return Result.fail("手机格式不正确");
        }
        order.setPhone(address.getPhone());
        order.setFullAddress(
                address.getProvince() + " " + address.getCity() + " " + address.getDistrict() + " " + address.getDetailAddress()
        );


        // 先插入订单（此时数据库会生成订单 ID）
        this.save(order);

        // 3. 遍历购物项，生成 OrderItem
        for (CreateOrderRequestDTO.OrderItemDTO itemDto : dto.getItems()) {
            Product product = productMapper.selectById(itemDto.getProductId());
            if (product == null || product.getStock() <= itemDto.getQuantity()) {
                return Result.fail("商品存库不足或不存在，请联系商家_" + itemDto.getProductId());
            }

            // 扣减库存
            product.setStock(product.getStock() - itemDto.getQuantity());
            productMapper.updateById(product);

            BigDecimal itemTotal = product.getPrice().multiply(BigDecimal.valueOf(itemDto.getQuantity()));
            totalPrice = totalPrice.add(itemTotal);

            OrderItem orderItem = new OrderItem();
            orderItem.setOrderId(order.getId());
            orderItem.setProductId(product.getId());
            orderItem.setPrice(product.getPrice());
            orderItem.setQuantity(itemDto.getQuantity());
            orderItemMapper.insert(orderItem);
        }

        // 4. 更新订单总价
        order.setTotalPrice(totalPrice);
        this.updateById(order);

        return Result.success("订单生成成功！", order);
    }

    //订单支付后修改商品状态
    @Override
    public Result<String> getNotify(HttpServletRequest request) throws Exception {
        Map<String, String> params = new HashMap<>();
        Map<String, String[]> requestParams = request.getParameterMap();
        for (String name : requestParams.keySet()) {
            params.put(name, String.join(",", requestParams.get(name)));
        }

        // 验签
        boolean signVerified = Factory.Payment.Common().verifyNotify(params);
        if (signVerified) {
            String outTradeNo = params.get("out_trade_no");  // 商户订单号
            String tradeStatus = params.get("trade_status"); // 支付状态
            String tradeNo = params.get("trade_no");         // 支付宝交易号

            if ("TRADE_SUCCESS".equals(tradeStatus) || "TRADE_FINISHED".equals(tradeStatus)) {
                // 更新数据库，修改订单状态为 "paid"
                Order order = orderMapper.selectById(outTradeNo);
                // orderService.markAsPaid(outTradeNo, tradeNo);
                if (order != null && "pending".equals(order.getStatus())) {
                    order.setStatus("paid");
                    order.setUpdatedAt(LocalDateTime.now());
                    // 你也可以加一个字段保存 tradeNo
                    order.setTradeNo(tradeNo);
                    orderMapper.updateById(order);
                }
            }
            return Result.success("支付回调成功");
        }
        return Result.fail("验签失败");
    }

    //用户取消订单
    @Override
    public Result<Order> cancelOrder(Long orderId) {
        Order order = orderMapper.selectById(orderId);
        order.setStatus("cancelled");
        this.updateById(order);
        return Result.success("订单取消成功", order);
    }

    //支付宝支付
    @Override
    public Result<String> aiPay(String orderId) throws Exception {
        Order order = orderMapper.selectById(orderId);
        String totalPrice = String.format("%.2f", order.getTotalPrice());
        //调用支付宝接口
        AlipayTradePrecreateResponse response = Factory.Payment.FaceToFace().preCreate("成都精品咖啡支付中心", orderId, totalPrice);
        if (response == null || !"10000".equals(response.getCode())) {
            return Result.fail("支付宝下单失败：" + (response != null ? response.getMsg() : "响应为空"));
        }

        //获取二维码支付链接

        String qrUrl = response.getQrCode();
        // 生成二维码
        String fileName = UUID.randomUUID() + ".png";
        String qrDir = DataUtils.UPLOAD_DIR + "QRCode/";
        File dirFile = new File(qrDir);
        if (!dirFile.exists()) {
            dirFile.mkdirs();
        }
        File qrFile = new File(qrDir + fileName);
        QrCodeUtil.generate(qrUrl, 300, 300, qrFile);

        String localAbsolutePath = DataUtils.WEB_HTTPS_PAY + fileName;
        return Result.success("支付二维码生成成功", localAbsolutePath);
    }

    //获取用户的商店
    @Override
    public Result<OrderResponseDTO> getUserCart(Long userId) {
        // 查询用户的购物车订单
        LambdaQueryWrapper<Order> orderWrapper = new LambdaQueryWrapper<>();
        orderWrapper.eq(Order::getUserId, userId)
                .eq(Order::getStatus, "cart");
        Order cartOrder = orderMapper.selectOne(orderWrapper);

        // 如果没有购物车订单，创建一个新的
        if (cartOrder == null) {
            cartOrder = new Order();
            cartOrder.setUserId(userId);
            cartOrder.setStatus("cart");
            cartOrder.setTotalPrice(BigDecimal.ZERO);
            orderMapper.insert(cartOrder);
        }

        // 查询购物车中的商品
        LambdaQueryWrapper<OrderItem> itemWrapper = new LambdaQueryWrapper<>();
        itemWrapper.eq(OrderItem::getOrderId, cartOrder.getId());
        List<OrderItem> items = orderItemMapper.selectList(itemWrapper);

        // 构建响应DTO
        OrderResponseDTO responseDTO = new OrderResponseDTO();
        responseDTO.setId(cartOrder.getId());
        responseDTO.setUserId(userId);
        responseDTO.setTotalPrice(cartOrder.getTotalPrice());
        responseDTO.setStatus("cart");

        List<OrderResponseDTO.OrderItemResponseDTO> itemDTOs = new ArrayList<>();
        for (OrderItem item : items) {
            Product product = productMapper.selectById(item.getProductId());
            if (product != null) {
                OrderResponseDTO.OrderItemResponseDTO dto = new OrderResponseDTO.OrderItemResponseDTO();
                dto.setProductId(item.getProductId());
                dto.setQuantity(item.getQuantity());
                dto.setPrice(item.getPrice());
                dto.setProductName(product.getName());
                // 处理商品图片
                if (product.getImages() != null) {
                    dto.setProductImages(Arrays.stream(product.getImages()
                                    .replace("[", "")
                                    .replace("]", "")
                                    .replace("\"", "")
                                    .split(","))
                            .map(String::trim).collect(Collectors.toList()));
                }
                itemDTOs.add(dto);
            }
        }
        responseDTO.setItems(itemDTOs);
        return Result.success("获取购物车成功", responseDTO);
    }

    //添加用户的商店
    @Override
    @Transactional
    public Result<String> addToCart(Long userId, Long productId, Integer quantity) {
        // 检查商品是否存在
        Product product = productMapper.selectById(productId);
        if (product == null) {
            return Result.fail("商品不存在");
        }
        // 检查库存
        if (product.getStock() < quantity) {
            return Result.fail("商品库存不足");
        }
        // 查询用户的购物车订单
        LambdaQueryWrapper<Order> orderWrapper = new LambdaQueryWrapper<>();
        orderWrapper.eq(Order::getUserId, userId)
                .eq(Order::getStatus, "cart");
        Order cartOrder = orderMapper.selectOne(orderWrapper);

        // 如果没有购物车订单，创建一个新的
        if (cartOrder == null) {
            cartOrder = new Order();
            cartOrder.setUserId(userId);
            cartOrder.setStatus("cart");
            cartOrder.setTotalPrice(BigDecimal.ZERO);
            orderMapper.insert(cartOrder);
        }
        // 检查购物车中是否已有该商品
        LambdaQueryWrapper<OrderItem> itemWrapper = new LambdaQueryWrapper<>();
        itemWrapper.eq(OrderItem::getOrderId, cartOrder.getId())
                .eq(OrderItem::getProductId, productId);
        OrderItem existingItem = orderItemMapper.selectOne(itemWrapper);

        if (existingItem != null) {
            // 更新数量
            existingItem.setQuantity(existingItem.getQuantity() + quantity);
            orderItemMapper.updateById(existingItem);
        } else {
            // 添加新商品到购物车
            OrderItem newItem = new OrderItem();
            newItem.setOrderId(cartOrder.getId());
            newItem.setProductId(productId);
            newItem.setQuantity(quantity);
            newItem.setPrice(product.getPrice());
            orderItemMapper.insert(newItem);
        }
        // 更新购物车总价
        updateCartTotalPrice(cartOrder.getId());
        return Result.success("商品已添加到购物车", null);
    }

    //更新用户的商店
    @Override
    @Transactional
    public Result<String> updateCartItem(Long userId, Long productId, Integer quantity) {
        // 查询用户的购物车订单
        LambdaQueryWrapper<Order> orderWrapper = new LambdaQueryWrapper<>();
        orderWrapper.eq(Order::getUserId, userId)
                .eq(Order::getStatus, "cart");
        Order cartOrder = orderMapper.selectOne(orderWrapper);

        if (cartOrder == null) {
            return Result.fail("购物车不存在");
        }

        // 查询购物车中的商品
        LambdaQueryWrapper<OrderItem> itemWrapper = new LambdaQueryWrapper<>();
        itemWrapper.eq(OrderItem::getOrderId, cartOrder.getId())
                .eq(OrderItem::getProductId, productId);
        OrderItem item = orderItemMapper.selectOne(itemWrapper);

        if (item == null) {
            return Result.fail("购物车中没有该商品");
        }

        if (quantity <= 0) {
            // 如果数量为0或负数，从购物车中移除
            orderItemMapper.deleteById(item.getId());
        } else {
            // 检查库存
            Product product = productMapper.selectById(productId);
            if (product == null || product.getStock() < quantity) {
                return Result.fail("商品库存不足");
            }

            // 更新数量
            item.setQuantity(quantity);
            orderItemMapper.updateById(item);
        }

        // 更新购物车总价
        updateCartTotalPrice(cartOrder.getId());

        return Result.success("购物车已更新");
    }

    //移除用户的商店
    @Override
    @Transactional
    public Result<String> removeFromCart(Long userId, Long productId) {
        // 查询用户的购物车订单
        LambdaQueryWrapper<Order> orderWrapper = new LambdaQueryWrapper<>();
        orderWrapper.eq(Order::getUserId, userId)
                .eq(Order::getStatus, "cart");
        Order cartOrder = orderMapper.selectOne(orderWrapper);

        if (cartOrder == null) {
            return Result.fail("购物车不存在");
        }

        // 查询购物车中的商品
        LambdaQueryWrapper<OrderItem> itemWrapper = new LambdaQueryWrapper<>();
        itemWrapper.eq(OrderItem::getOrderId, cartOrder.getId())
                .eq(OrderItem::getProductId, productId);
        OrderItem item = orderItemMapper.selectOne(itemWrapper);

        if (item == null) {
            return Result.fail("购物车中没有该商品");
        }

        // 从购物车中移除
        orderItemMapper.deleteById(item.getId());

        // 更新购物车总价
        updateCartTotalPrice(cartOrder.getId());

        return Result.success("商品已从购物车移除", null);
    }

    //清楚用户的商店
    @Override
    @Transactional
    public Result<String> clearCart(Long userId) {
        // 查询用户的购物车订单
        LambdaQueryWrapper<Order> orderWrapper = new LambdaQueryWrapper<>();
        orderWrapper.eq(Order::getUserId, userId)
                .eq(Order::getStatus, "cart");
        Order cartOrder = orderMapper.selectOne(orderWrapper);
        if (cartOrder == null) {
            return Result.success("购物车已清空", null);
        }

        // 删除购物车中的所有商品
        LambdaQueryWrapper<OrderItem> itemWrapper = new LambdaQueryWrapper<>();
        itemWrapper.eq(OrderItem::getOrderId, cartOrder.getId());
        orderItemMapper.delete(itemWrapper);

        // 更新购物车总价
        cartOrder.setTotalPrice(BigDecimal.ZERO);
        orderMapper.updateById(cartOrder);

        return Result.success("购物车已清空", null);

    }

    //获取用户订单信息
    @Override
    public Result<IPage<OrderResponseDTO>> getUserOrders(Long page,Long size,Long userId) {
        Page<Order> pageParam = new Page<>(page, size);
        IPage<Order> orderPage = orderMapper.selectPage(pageParam, new LambdaQueryWrapper<Order>()
                .eq(Order::getUserId, userId)
                .ne(Order::getStatus, "cart")
                .orderByDesc(Order::getRecipientName));

        IPage<OrderResponseDTO> dtoPage=orderPage.convert(order-> {
            OrderResponseDTO dto = new OrderResponseDTO();
            BeanUtils.copyProperties(order, dto);

            // 查询订单项
            List<OrderItem> orderItems = orderItemMapper.selectList(
                    new LambdaQueryWrapper<OrderItem>()
                            .eq(OrderItem::getOrderId, order.getId())
            );
            List<OrderResponseDTO.OrderItemResponseDTO> itemDTOs = orderItems.stream().map(item -> {
                OrderResponseDTO.OrderItemResponseDTO itemDTO = new OrderResponseDTO.OrderItemResponseDTO();
                BeanUtils.copyProperties(item, itemDTO);

                // 查询商品
                Product product = productMapper.selectById(item.getProductId());
                if (product != null) {
                    itemDTO.setProductName(product.getName());
                    if (product.getImages() != null) {
                        // 🔑 图片解析：和 ProductResponseDTO 保持一致
                        List<String> images = Arrays.stream(product.getImages()
                                        .replace("[", "")
                                        .replace("]", "")
                                        .replace("\"", "")
                                        .split(","))
                                .map(String::trim)
                                .collect(Collectors.toList());
                        itemDTO.setProductImages(images);
                    }
                }
                return itemDTO;
            }).collect(Collectors.toList());
            dto.setItems(itemDTOs);
            return dto;
        });
        return  Result.success("分页查询用户订单成功", dtoPage);
    }

    //获取订单详情
    @Override
    public Result<OrderResponseDTO> getOrderDetails(Long orderId) {
        // 1. 查询订单
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            return Result.fail("订单不存在");
        }

        // 2. 转换订单信息到 DTO
        OrderResponseDTO dto = new OrderResponseDTO();
        BeanUtils.copyProperties(order, dto);

        // 3. 收货地址封装
        Address address = addressMapper.selectById(order.getAddressId());
        List<OrderResponseDTO.AddressResponseDTO> addressList;
        OrderResponseDTO.AddressResponseDTO addressDTO = new OrderResponseDTO.AddressResponseDTO();
        BeanUtils.copyProperties(address, addressDTO);
        addressDTO.setFullAddress(
                address.getProvince() + " " + address.getCity() + " " + address.getDistrict() + " " + address.getDetailAddress()
        );
        addressList = List.of(addressDTO);
        dto.setAddress(addressList);
        // 4. 查询订单项
        List<OrderItem> orderItems = orderItemMapper.selectList(
                new LambdaQueryWrapper<OrderItem>()
                        .eq(OrderItem::getOrderId, order.getId())
        );
        List<OrderResponseDTO.OrderItemResponseDTO> itemDTOs = orderItems.stream().map(item -> {
                    OrderResponseDTO.OrderItemResponseDTO itemDTO = new OrderResponseDTO.OrderItemResponseDTO();
                    BeanUtils.copyProperties(item, itemDTO);

            Product product = productMapper.selectById(item.getProductId());
            if (product != null) {
                itemDTO.setProductName(product.getName());
                if (product.getImages() != null) {
                    List<String> images = Arrays.stream(product.getImages()
                                    .replace("[", "")
                                    .replace("]", "")
                                    .replace("\"", "")
                                    .split(","))
                            .map(String::trim)
                            .map(String::trim)
                            .collect(Collectors.toList());
                    itemDTO.setProductImages(images);
                }
            }
            return itemDTO;
        }).toList();
        dto.setItems(itemDTOs);
        return Result.success("查询订单详情成功", dto);
    }

    /**
     * 更新购物车总价
     *
     * @param cartId 购物车ID
     */
    private void updateCartTotalPrice(Long cartId) {
        // 查询购物车中的所有商品
        LambdaQueryWrapper<OrderItem> itemWrapper = new LambdaQueryWrapper<>();
        itemWrapper.eq(OrderItem::getOrderId, cartId);
        List<OrderItem> items = orderItemMapper.selectList(itemWrapper);

        // 计算总价
        BigDecimal totalPrice = items.stream()
                .filter(item -> item.getPrice() != null && item.getQuantity() != null)
                .map(item -> item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 更新购物车总价（直接更新，避免多查一次）
        Order updateOrder = new Order();
        updateOrder.setId(cartId);
        updateOrder.setTotalPrice(totalPrice);
        orderMapper.updateById(updateOrder);
    }
}
