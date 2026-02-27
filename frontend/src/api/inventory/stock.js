import request from '@/utils/request'

export function getStockPage(params) {
  return request({
    url: '/stocks',
    method: 'get',
    params
  })
}

export function updateStock(id, data) {
  return request({
    url: `/stocks/${id}`,
    method: 'put',
    data
  })
}
