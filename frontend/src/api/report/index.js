import request from '@/utils/request'

export function getSalesTrend(start, end) {
  return request({
    url: '/reports/sales/day',
    method: 'get',
    params: { start, end }
  })
}

export function getSalesByCustomer(start, end) {
  return request({
    url: '/reports/sales/customer',
    method: 'get',
    params: { start, end }
  })
}

export function getSalesFunnel() {
  return request({
    url: '/reports/sales/funnel',
    method: 'get'
  })
}

export function getLowStock() {
  return request({
    url: '/reports/inventory/low-stock',
    method: 'get'
  })
}
