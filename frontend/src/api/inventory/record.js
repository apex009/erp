import request from '@/utils/request'

export function getStockRecordPage(params) {
  return request({
    url: '/stock-records',
    method: 'get',
    params
  })
}
