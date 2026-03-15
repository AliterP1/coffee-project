package com.example.mycoffeedemo.service.Impl;

import cn.hutool.extra.qrcode.QrCodeUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.alipay.easysdk.factory.Factory;
import com.alipay.easysdk.payment.facetoface.models.AlipayTradePrecreateResponse;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.mycoffeedemo.dto.CreateOrderRequestDTO;
import com.example.mycoffeedemo.dto.OrderResponseDTO;
import com.example.mycoffeedemo.entity.*;
import com.example.mycoffeedemo.mapper.*;
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

    @Autowired
    private MerchantOrderMapper merchantOrderMapper;

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
        if (!PhoneUtil.isValidPhone(address.getPhone())) {
            return Result.fail("手机格式不正确");
        }
        order.setPhone(address.getPhone());
        order.setFullAddress(
                address.getProvince() + " " + address.getCity() + " " + address.getDistrict() + " " + address.getDetailAddress()
        );


        // 先插入订单（此时数据库会生成订单 ID）
        this.save(order);

        // 用于分组商家
        Map<Long, List<OrderItem>> merchantMap = new HashMap<>();

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

            // 按商家分组
            Long merchantId = product.getMerchantId();
            merchantMap
                    .computeIfAbsent(merchantId, k -> new ArrayList<>())
                    .add(orderItem);
        }

        // 4. 创建 merchant_orders
        for (Map.Entry<Long, List<OrderItem>> entry : merchantMap.entrySet()) {

            Long merchantId = entry.getKey();
            List<OrderItem> items = entry.getValue();

            BigDecimal merchantTotal = items.stream()
                    .map(i -> i.getPrice()
                            .multiply(BigDecimal.valueOf(i.getQuantity())))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            MerchantOrder merchantOrder = new MerchantOrder();
            merchantOrder.setOrderId(order.getId());
            merchantOrder.setMerchantId(merchantId);
            merchantOrder.setTotalPrice(merchantTotal);
            merchantOrder.setStatus("pending");

            merchantOrderMapper.insert(merchantOrder);

            // 回写 merchant_order_id
            for (OrderItem item : items) {
                item.setMerchantOrderId(merchantOrder.getId());
                orderItemMapper.updateById(item);
            }
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

    // 用户取消订单
    @Override
    @Transactional
    public Result<Order> cancelOrder(Long orderId) {
        // 查询主订单
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            return Result.fail("订单不存在");
        }

        // 更新主订单状态
        order.setStatus("cancelled");
        order.setImage("");
        this.updateById(order);

        // 更新子订单状态
        merchantOrderMapper.update(
                new MerchantOrder(),
                new LambdaUpdateWrapper<MerchantOrder>()
                        .eq(MerchantOrder::getOrderId, orderId)
                        .set(MerchantOrder::getStatus, "cancelled")
        );

        // 回滚库存
        List<OrderItem> items = orderItemMapper.selectList(
                new LambdaQueryWrapper<OrderItem>().eq(OrderItem::getOrderId, orderId)
        );
        for (OrderItem item : items) {
            Product product = productMapper.selectById(item.getProductId());
            if (product != null) {
                product.setStock(product.getStock() + item.getQuantity());
                productMapper.updateById(product);
            }
        }

        return Result.success("订单取消成功", order);
    }

    //支付宝支付
    @Override
    public Result<String> aiPay(String orderId) throws Exception {
        Order order = orderMapper.selectById(orderId);

        if (order == null) {
            return Result.fail("订单不存在");
        }

        // 判断订单状态和二维码是否已生成
        if (!"pending".equals(order.getStatus()) || (order.getImage() != null && !order.getImage().isEmpty())) {
            return Result.fail("订单已支付或二维码已生成，请勿重复支付");
        }


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
        order.setImage("/uploads/QRCode/"+fileName);
        String pay_image="/uploads/QRCode/"+fileName;
        orderMapper.updateById(order);
        String qrDir = DataUtils.UPLOAD_DIR + "QRCode/";
        File dirFile = new File(qrDir);
        if (!dirFile.exists()) {
            dirFile.mkdirs();
        }
        File qrFile = new File(qrDir + fileName);
        QrCodeUtil.generate(qrUrl, 300, 300, qrFile);

        String localAbsolutePath = DataUtils.WEB_HTTPS_PAY + fileName;
        return Result.success("支付二维码生成成功", pay_image);
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
                    dto.setProductImages(product.getImages());
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
                .ne(Order::getStatus, "cart"));

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
                        // 图片解析：和 ProductResponseDTO 保持一致
                        List<String> images = product.getImages();
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
                    List<String> images = product.getImages();
                    itemDTO.setProductImages(images);
                }
            }
            return itemDTO;
        }).toList();
        dto.setItems(itemDTOs);
        return Result.success("查询订单详情成功", dto);
    }

    @Override
    public Result<IPage<OrderResponseDTO>> getAllOrder(Long page,Long size) {
        Page<Order> pageParam = new Page<>(page, size);
        IPage<Order> orderPage = orderMapper.selectPage(pageParam, new LambdaQueryWrapper<Order>()
                .ne(Order::getStatus, "cart"));
        IPage<OrderResponseDTO> dtoPage=orderPage.convert(order-> {
            OrderResponseDTO dto = new OrderResponseDTO();
            BeanUtils.copyProperties(order, dto);

            // 1、查询订单项
            List<OrderItem> itemList = orderItemMapper.selectList(
                    new LambdaQueryWrapper<OrderItem>()
                            .eq(OrderItem::getOrderId, order.getId())
            );

            List<OrderResponseDTO.OrderItemResponseDTO> itemDTOList =
                    itemList.stream().map(item -> {
                        OrderResponseDTO.OrderItemResponseDTO itemDTO =
                                new OrderResponseDTO.OrderItemResponseDTO();
                        BeanUtils.copyProperties(item, itemDTO);

                        // 查询商品
                        Product product = productMapper.selectById(item.getProductId());
                        if (product != null) {
                            itemDTO.setProductName(product.getName());
                            itemDTO.setProductImages(product.getImages());
                        }
                        return itemDTO;
                    }).toList();

            dto.setItems(itemDTOList);

            // 2、查询地址
            Address address = addressMapper.selectById(order.getAddressId());

            if (address != null) {
                OrderResponseDTO.AddressResponseDTO addressDTO =
                        new OrderResponseDTO.AddressResponseDTO();
                BeanUtils.copyProperties(address, addressDTO);
                addressDTO.setFullAddress(
                        address.getProvince() + address.getCity() + address.getDistrict() + address.getDetailAddress()
                );
                dto.setAddress(List.of(addressDTO));
            }

            return dto;
        });
        return Result.success(dtoPage);
    }

    @Override
    @Transactional
    public Result<String> getPaid(String orderId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            return Result.fail("订单不存在");
        }

        if (!"pending".equals(order.getStatus())) {
            return Result.fail("订单状态不可支付");
        }

        // 1. 更新主订单状态
        order.setStatus("paid");
        orderMapper.updateById(order);

        // 2. 更新所有子订单状态为已支付
        merchantOrderMapper.update(
                null,
                new LambdaUpdateWrapper<MerchantOrder>()
                        .eq(MerchantOrder::getOrderId, orderId)
                        .set(MerchantOrder::getStatus, "paid")
        );

        return Result.success("支付成功", null);
    }

    //商家获取订单
    @Override
    public Result<IPage<OrderResponseDTO>> getMerchantOrder(Long page,
                                                            Long size,
                                                            Long merchantId) {

        Page<MerchantOrder> pageParam = new Page<>(page, size);

        IPage<MerchantOrder> merchantPage =
                merchantOrderMapper.selectPage(
                        pageParam,
                        new LambdaQueryWrapper<MerchantOrder>()
                                .eq(MerchantOrder::getMerchantId, merchantId)
                                .ne(MerchantOrder::getStatus, "cart")
                );

        IPage<OrderResponseDTO> dtoPage = merchantPage.convert(mo -> {

            OrderResponseDTO dto = new OrderResponseDTO();

            // 查询总订单（获取地址）
            Order order = orderMapper.selectById(mo.getOrderId());
            BeanUtils.copyProperties(order, dto);

            // 1、查询该子订单的商品
            List<OrderItem> items = orderItemMapper.selectList(
                    new LambdaQueryWrapper<OrderItem>()
                            .eq(OrderItem::getMerchantOrderId, mo.getId())
            );

            List<OrderResponseDTO.OrderItemResponseDTO> itemDTOList =
                    items.stream().map(item -> {

                        OrderResponseDTO.OrderItemResponseDTO itemDTO =
                                new OrderResponseDTO.OrderItemResponseDTO();
                        BeanUtils.copyProperties(item, itemDTO);

                        // 设置商品对应子订单状态
                        itemDTO.setStatus(mo.getStatus());

                        Product product =
                                productMapper.selectById(item.getProductId());

                        if (product != null) {
                            itemDTO.setProductName(product.getName());
                            itemDTO.setProductImages(product.getImages());
                        }
                        return itemDTO;
                    }).toList();

            dto.setItems(itemDTOList);

            // 2、查询地址
            Address address = addressMapper.selectById(order.getAddressId());

            if (address != null) {
                OrderResponseDTO.AddressResponseDTO addressDTO =
                        new OrderResponseDTO.AddressResponseDTO();
                BeanUtils.copyProperties(address, addressDTO);
                addressDTO.setFullAddress(
                        address.getProvince() + address.getCity() + address.getDistrict() + address.getDetailAddress()
                );
                dto.setAddress(List.of(addressDTO));
            }
            return dto;
        });

        return Result.success("查询商家订单成功", dtoPage);
    }

    @Override
    @Transactional
    public Result<String> ship(Long merchantOrderId) {

        MerchantOrder mo =
                merchantOrderMapper.selectById(merchantOrderId);

        if (mo == null) {
            return Result.fail("子订单不存在");
        }

        if (!"paid".equals(mo.getStatus())) {
            return Result.fail("订单未支付");
        }

        mo.setStatus("shipped");
        merchantOrderMapper.updateById(mo);

        // 检查是否所有子订单都已发货
        Long orderId = mo.getOrderId();

        long count = merchantOrderMapper.selectCount(
                new LambdaQueryWrapper<MerchantOrder>()
                        .eq(MerchantOrder::getOrderId, orderId)
                        .ne(MerchantOrder::getStatus, "shipped")
        );

        if (count == 0) {
            Order order = orderMapper.selectById(orderId);
            order.setStatus("shipped");
            orderMapper.updateById(order);
        }

        return Result.success("发货成功");
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
