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

export function getDashboardSummary() {
  return request({
    url: '/reports/dashboard/summary',
    method: 'get'
  })
}

export function getFinanceReceivable() {
  return request({
    url: '/reports/finance/receivable',
    method: 'get'
  })
}

export function getFinancePayable() {
  return request({
    url: '/reports/finance/payable',
    method: 'get'
  })
}

export function getSalesRank(top = 10) {
  return request({
    url: '/reports/rank/sales',
    method: 'get',
    params: { top }
  })
}
