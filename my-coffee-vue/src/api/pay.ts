import request from '@/utils/request'
import type  { ApiResponse } from '@/api/page'

// 支付（生成订单二维码进行支付）
export const pay = (orderId: string) =>
  request.post('/pay', null, { params: { orderId } })


//模拟支付成功(点击已支付 默认使用这个接口)
export const paid = (orderId: string)=>
  request.post('/paid', null, { params: { orderId } })

// 支付成功通知（一般后台使用，不一定在前端调用）
// export const payNotify = (formData: any) =>
//   request.post('/notify', formData)
