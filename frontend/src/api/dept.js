import request from '@/utils/request'

export function getDeptPage(params) {
  return request({
    url: '/depts',
    method: 'get',
    params
  })
}

export function createDept(data) {
  return request({
    url: '/depts',
    method: 'post',
    data
  })
}

export function updateDept(id, data) {
  return request({
    url: `/depts/${id}`,
    method: 'put',
    data
  })
}

export function deleteDept(id) {
  return request({
    url: `/depts/${id}`,
    method: 'delete'
  })
}
