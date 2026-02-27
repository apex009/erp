import request from '@/utils/request'

export function getOrderPage(params) {
  return request({
    url: '/sales-orders',
    method: 'get',
    params
  })
}

export function getOrder(id) {
  return request({
    url: `/sales-orders/${id}`,
    method: 'get'
  })
}

export function getOrderItems(id) {
  return request({
    url: `/sales-orders/${id}/items`,
    method: 'get'
  })
}

export function createOrder(data) {
  return request({
    url: '/sales-orders',
    method: 'post',
    data
  })
}

export function approveOrder(id) {
  return request({
    url: `/sales-orders/${id}/approve`,
    method: 'post'
  })
}

export function cancelOrder(id) {
  return request({
    url: `/sales-orders/${id}/cancel`,
    method: 'post'
  })
}

export function returnOrder(id) {
  return request({
    url: `/sales-orders/${id}/return`,
    method: 'post'
  })
}

export function outboundOrder(id) {
  return request({
    url: `/sales-orders/${id}/outbound`,
    method: 'post'
  })
}
