import request from '@/utils/request'

export function getOrderPage(params) {
  return request({
    url: '/purchase-orders',
    method: 'get',
    params
  })
}

export function getOrder(id) {
  return request({
    url: `/purchase-orders/${id}`,
    method: 'get'
  })
}

export function getOrderItems(id) {
  return request({
    url: `/purchase-orders/${id}/items`,
    method: 'get'
  })
}

export function createOrder(data) {
  return request({
    url: '/purchase-orders',
    method: 'post',
    data
  })
}

export function approveOrder(id) {
  return request({
    url: `/purchase-orders/${id}/approve`,
    method: 'post'
  })
}

export function cancelOrder(id) {
  return request({
    url: `/purchase-orders/${id}/cancel`,
    method: 'post'
  })
}

export function returnOrder(id) {
  return request({
    url: `/purchase-orders/${id}/return`,
    method: 'post'
  })
}

export function inboundOrder(id) {
  return request({
    url: `/purchase-orders/${id}/inbound`,
    method: 'post'
  })
}
