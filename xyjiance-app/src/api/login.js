import { post, get } from '@/utils/request'

// 账号密码登录
export function login(username, password, code, uuid) {
  return post('/login', { username, password, code, uuid }, { isToken: false })
}

// 微信小程序登录
export function wxLogin(code) {
  return post('/wx/login', { code }, { isToken: false })
}

// 获取用户信息
export function getInfo() {
  return get('/getInfo')
}

// 退出登录
export function logout() {
  return post('/logout')
}

// 获取验证码
export function getCodeImg() {
  return get('/captchaImage', {}, { isToken: false })
}

// 获取路由（小程序端精简权限列表）
export function getRouters() {
  return get('/getRouters')
}
