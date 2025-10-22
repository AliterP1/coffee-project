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
  addressId: number
  totalPrice: number
  status: string
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
  request.post<Order>(`/order/${orderId}/cancel`)

// 获取用户订单列表
export const getUserOrders = (page = 1, size = 5, userId: number) =>
  postPage<OrderResponseDTO>('order/orders', undefined, { params: { page, size, userId } })

// 获取订单详情
export const getOrderDetails = (orderId: number) =>
  request.post<ApiResponse<OrderResponseDTO>>('order/details', null, { 
    params: { orderId } 
  })

