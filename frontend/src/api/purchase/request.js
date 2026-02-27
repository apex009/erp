import request from '@/utils/request'

export function getRequestPage(params) {
  return request({
    url: '/purchase-requests',
    method: 'get',
    params
  })
}

export function getRequest(id) {
  return request({
    url: `/purchase-requests/${id}`,
    method: 'get'
  })
}

export function getRequestItems(id) {
  return request({
    url: `/purchase-requests/${id}/items`,
    method: 'get'
  })
}

export function createRequest(data) {
  return request({
    url: '/purchase-requests',
    method: 'post',
    data
  })
}

export function approveRequest(id) {
  return request({
    url: `/purchase-requests/${id}/approve`,
    method: 'post'
  })
}

export function rejectRequest(id) {
  return request({
    url: `/purchase-requests/${id}/reject`,
    method: 'post'
  })
}

export function convertToOrder(id) {
  return request({
    url: `/purchase-requests/${id}/to-order`,
    method: 'post'
  })
}
