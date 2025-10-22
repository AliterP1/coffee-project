import request from '@/utils/request'

export interface Address {
  id?: number
  userId?: number
  recipientName: string
  phone: string
  province?: string
  city?: string
  district?: string
  detailAddress?: string
  fullAddress?: string
  isDefault?: boolean
}

// 获取用户地址
export const getUserAddresses = (userId: number) =>
  request.get(`/address/${userId}`)

// 添加地址
export const addAddress = (address: Address) =>
  request.post('/address/add', address)

// 更新地址
export const updateAddress = (address: Address) =>
  request.put('/address/update', address)

// 删除地址
export const deleteAddress = (id: number) =>
  request.delete(`/address/delete/${id}`)

// 设置默认地址
export const setDefaultAddress = (id: number) =>
  request.put(`/address/${id}/default`)
    