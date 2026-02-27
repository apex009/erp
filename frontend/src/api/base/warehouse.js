import request from '@/utils/request'

export function getWarehousePage(params) {
  return request({
    url: '/warehouses',
    method: 'get',
    params
  })
}

export function getWarehouse(id) {
  return request({
    url: `/warehouses/${id}`,
    method: 'get'
  })
}

export function createWarehouse(data) {
  return request({
    url: '/warehouses',
    method: 'post',
    data
  })
}

export function updateWarehouse(id, data) {
  return request({
    url: `/warehouses/${id}`,
    method: 'put',
    data
  })
}

export function deleteWarehouse(id) {
  return request({
    url: `/warehouses/${id}`,
    method: 'delete'
  })
}
