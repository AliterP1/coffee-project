import request from '@/utils/request'

// 支付
export const pay = (orderId: string) =>
  request.post('/pay', null, { params: { orderId } })

// 支付成功通知（一般后台使用，不一定在前端调用）
// export const payNotify = (formData: any) =>
//   request.post('/notify', formData)
