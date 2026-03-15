import request from '@/utils/request'
import  { postPage } from '@/api/page'
import type  { ApiResponse } from '@/api/page'


export interface CreateOrderRequestDTO{
  userId: number
  addressId: number
  items: CreateOrderRequestDTO.OrderItemDTO[]
}

export namespace CreateOrderRequestDTO {
  export interface OrderItemDTO {
    productId: number
    quantity: number
  }
}

export interface Order {
  id: number      
  userId: number
  addressId: number
  totalPrice: number
  status: string
  createdAt: string
  updatedAt: string
  // 其他字段根据后端返回补充
}
// 订单响应DTO接口
export interface OrderResponseDTO {
  id: number
  userId: number
  fullAddress: string
  totalPrice: number
  status: string
  createdAt: string
  expireTime: string
  updatedAt: string
  items: OrderResponseDTO.OrderItemDTO[]
  address: OrderResponseDTO.AddressDTO[]
  // 其他字段根据后端返回补充
}

// 订单响应数据传输对象命名空间
export namespace OrderResponseDTO {
  // 订单项数据传输对象
  export interface OrderItemDTO {
    id: number;
    orderId: number;
    productId: number;
    productName: string;
    price: number;
    quantity: number;
    productImage?: string;
  }
  
  // 地址数据传输对象
  export interface AddressDTO {
    id: number;
    userId: number;
    recipientName: string;
    phone: string;
    fullAddress: string;
  }
}
// 创建订单
export const createOrder = (data: CreateOrderRequestDTO) =>
  request.post<Order>('/order', data)

// 取消订单
export const cancelOrder = (orderId: number) =>
  request.post<ApiResponse<Order>>(`/order/${orderId}/cancel`)

// 获取用户订单列表
export const getUserOrders = (page = 1, size = 5, userId: number) =>
  postPage<OrderResponseDTO>('order/orders', undefined, { params: { page, size, userId } })

// 获取订单详情
export const getOrderDetails = (orderId: number) =>
  request.post<ApiResponse<OrderResponseDTO>>('order/details', null, { 
    params: { orderId } 
  })

// 获取所有订单
export const getAllOrder = (page = 1, size = 10) =>
  postPage<OrderResponseDTO>('order/allOrder', undefined, { params: { page, size } })

// 获取商家订单
export const getMerchantOrder = (page = 1, size = 10, userId: number) =>
  postPage<OrderResponseDTO>('order/merchantOrder', undefined, { params: { page, size, userId } })

// 更新订单状态
export const updateOrderStatus = (orderId: number, status: string) =>
  request.put<ApiResponse<Order>>(`/order/${orderId}/status`, null, { params: { status } })

// 商家发货
export const shipMerchantOrder = (merchantOrderId: number) =>
  request.post<ApiResponse<string>>(`/order/${merchantOrderId}/ship`)
