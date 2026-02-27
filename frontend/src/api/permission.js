import request from '@/utils/request'

export function getPermissionPage(params) {
  return request({
    url: '/permissions',
    method: 'get',
    params
  })
}

export function createPermission(data) {
  return request({
    url: '/permissions',
    method: 'post',
    data
  })
}

export function updatePermission(id, data) {
  return request({
    url: `/permissions/${id}`,
    method: 'put',
    data
  })
}

export function deletePermission(id) {
  return request({
    url: `/permissions/${id}`,
    method: 'delete'
  })
}

export function syncPermissions() {
  return request({
    url: '/permissions/sync',
    method: 'post'
  })
}
