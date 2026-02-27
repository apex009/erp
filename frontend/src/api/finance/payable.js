import request from '@/utils/request'

export function getPayablePage(params) {
  return request({
    url: '/payables',
    method: 'get',
    params
  })
}

export function getPayable(id) {
  return request({
    url: `/payables/${id}`,
    method: 'get'
  })
}

export function getPayments(id) {
  return request({
    url: `/payables/${id}/payments`,
    method: 'get'
  })
}

export function createPayment(id, data) {
  return request({
    url: `/payables/${id}/payment`,
    method: 'post',
    data
  })
}
