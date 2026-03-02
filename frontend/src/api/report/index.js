import request from '@/utils/request'

export function getSalesTrend(start, end) {
  return request({
    url: '/reports/sales/day',
    method: 'get',
    params: { start, end },
    ignore403: true
  })
}

export function getSalesByCustomer(start, end) {
  return request({
    url: '/reports/sales/customer',
    method: 'get',
    params: { start, end },
    ignore403: true
  })
}

export function getSalesFunnel() {
  return request({
    url: '/reports/sales/funnel',
    method: 'get',
    ignore403: true
  })
}

export function getLowStock() {
  return request({
    url: '/reports/inventory/low-stock',
    method: 'get',
    ignore403: true
  })
}

export function getDashboardSummary() {
  return request({
    url: '/reports/dashboard/summary',
    method: 'get',
    ignore403: true
  })
}

export function getFinanceReceivable() {
  return request({
    url: '/reports/finance/receivable',
    method: 'get',
    ignore403: true
  })
}

export function getFinancePayable() {
  return request({
    url: '/reports/finance/payable',
    method: 'get',
    ignore403: true
  })
}

export function getSalesRank(top = 10) {
  return request({
    url: '/reports/rank/sales',
    method: 'get',
    params: { top },
    ignore403: true
  })
}
