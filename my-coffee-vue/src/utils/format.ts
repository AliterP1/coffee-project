/**
 * 格式化价格，保留两位小数
 * @param price number | string
 */
export function formatPrice(price: number | string): string {
  return `￥${Number(price).toFixed(2)}`
}

/**
 * 格式化日期 yyyy-MM-dd HH:mm
 */
export function formatDateTime(date: string | Date): string {
  const d = new Date(date)
  const year = d.getFullYear()
  const month = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  const hours = String(d.getHours()).padStart(2, '0')
  const minutes = String(d.getMinutes()).padStart(2, '0')
  return `${year}-${month}-${day} ${hours}:${minutes}`
}

/**
 * 截断字符串，超出部分用...
 */
export function truncate(str: string, length = 20): string {
  if (!str) return ''
  return str.length > length ? str.slice(0, length) + '...' : str
}
