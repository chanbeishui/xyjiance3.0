import { getToken } from './auth'
import useUserStore from '@/store/modules/user'

const BASE_URL = '/dev-api'

// 请求封装（基于 uni.request）
const request = (options) => {
  const { url, method = 'GET', data = {}, header = {} } = options

  // 是否携带 token
  const isToken = header.isToken !== false

  return new Promise((resolve, reject) => {
    uni.request({
      url: BASE_URL + url,
      method: method.toUpperCase(),
      data: data,
      header: {
        'Content-Type': 'application/json;charset=utf-8',
        ...(isToken && getToken() ? { 'Authorization': 'Bearer ' + getToken() } : {})
      },
      success: (res) => {
        const code = res.data.code || 200

        if (res.statusCode === 200) {
          if (code === 401) {
            uni.showModal({
              title: '系统提示',
              content: '登录状态已过期，请重新登录',
              showCancel: false,
              success: () => {
                useUserStore().logOut().then(() => {
                  uni.reLaunch({ url: '/pages/login/index' })
                })
              }
            })
            reject(new Error('登录过期'))
          } else if (code === 500) {
            uni.showToast({ title: res.data.msg || '服务器错误', icon: 'none', duration: 2000 })
            reject(new Error(res.data.msg))
          } else if (code !== 200) {
            uni.showToast({ title: res.data.msg || '请求失败', icon: 'none', duration: 2000 })
            reject(new Error(res.data.msg))
          } else {
            resolve(res.data)
          }
        } else {
          uni.showToast({ title: '网络异常', icon: 'none', duration: 2000 })
          reject(new Error('网络异常'))
        }
      },
      fail: (err) => {
        uni.showToast({ title: '网络连接失败', icon: 'none', duration: 2000 })
        reject(err)
      }
    })
  })
}

// GET 请求
export function get(url, params, header) {
  return request({ url, method: 'GET', data: params, header })
}

// POST 请求
export function post(url, data, header) {
  return request({ url, method: 'POST', data, header })
}

// PUT 请求
export function put(url, data, header) {
  return request({ url, method: 'PUT', data, header })
}

// DELETE 请求
export function del(url, data, header) {
  return request({ url, method: 'DELETE', data, header })
}

export default { get, post, put, del, request }
