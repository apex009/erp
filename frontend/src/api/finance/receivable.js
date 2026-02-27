import request from '@/utils/request'

export function getReceivablePage(params) {
  return request({
    url: '/receivables',
    method: 'get',
    params
  })
}

export function getReceivable(id) {
  return request({
    url: `/receivables/${id}`,
    method: 'get'
  })
}

export function getReceipts(id) {
  return request({
    url: `/receivables/${id}/receipts`,
    method: 'get'
  })
}

export function createReceipt(id, data) {
  return request({
    url: `/receivables/${id}/receipt`,
    method: 'post',
    data
  })
}
