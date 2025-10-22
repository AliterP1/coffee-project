import request from '@/utils/request'
import type { AxiosRequestConfig } from 'axios'


export interface ApiResponse<T> {
  code: number
  message: string
  data: T
}

export interface IPage<T> {
  records: T[]
  total: number
  size: number
  current: number
  pages: number
}

/**
 * 通用分页请求
 * @param url 接口地址
 * @param params 查询参数（会自动带 page 和 size）
 * @param config axios 配置
 * @returns IPage<T>
 */
export async function getPage<T>(
  url: string,
  params: Record<string, any> = {},
  config?: AxiosRequestConfig
) {
  // 确保有正确的分页参数，避免默认参数覆盖用户传入的值
  const requestParams = {
    page: 1,
    size: 10,
    ...params
  };
  const res = await request.get<ApiResponse<IPage<T>>>(url, { params: requestParams, ...config })
  return res.data.data  // 返回真正的分页对象
}

export async function postPage<T>(
  url: string,
  params: Record<string, any> = {},
  config?: AxiosRequestConfig
) {
  // 确保有正确的分页参数，避免默认参数覆盖用户传入的值
  const requestParams = {
    page: 1,
    size: 10,
    ...params
  };
  const res = await request.post<ApiResponse<IPage<T>>>(url, requestParams, { ...config })
  return res.data.data  // 返回真正的分页对象
}