// Token 存储（适配 uni-app Storage API）
const TokenKey = 'Admin-Token'

export function getToken() {
  return uni.getStorageSync(TokenKey) || ''
}

export function setToken(token) {
  return uni.setStorageSync(TokenKey, token)
}

export function removeToken() {
  return uni.removeStorageSync(TokenKey)
}
