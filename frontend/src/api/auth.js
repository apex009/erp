import request from '@/utils/request'

export function login(data) {
  return request({
    url: '/auth/login',
    method: 'post',
    data
  })
}

export function getInfo() {
  return request({
    url: '/auth/me',
    method: 'get'
  })
}

export function logout() {
  return request({
    url: '/auth/logout',
    method: 'post'
  })
}

// 发送短信验证码
export function sendSmsCode(phone) {
  return request({
    url: '/auth/sms/code',
    method: 'post',
    params: { phone }
  })
}

// 短信登录
export function loginBySms(data) {
  return request({
    url: '/auth/login/sms',
    method: 'post',
    data
  })
}

// 获取当前用户菜单权限
export function getMenus() {
  return request({
    url: '/auth/menus',
    method: 'get'
  })
}
