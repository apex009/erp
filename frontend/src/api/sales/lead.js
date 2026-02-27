import request from '@/utils/request'

export function getLeadPage(params) {
  return request({
    url: '/leads',
    method: 'get',
    params
  })
}

export function getLead(id) {
  return request({
    url: `/leads/${id}`,
    method: 'get'
  })
}

export function createLead(data) {
  return request({
    url: '/leads',
    method: 'post',
    data
  })
}

export function updateLead(id, data) {
  return request({
    url: `/leads/${id}`,
    method: 'put',
    data
  })
}

export function deleteLead(id) {
  return request({
    url: `/leads/${id}`,
    method: 'delete'
  })
}
