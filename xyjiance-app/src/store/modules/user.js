import { getToken, setToken, removeToken } from '@/utils/auth'
import { login as loginApi, wxLogin as wxLoginApi, logout as logoutApi, getInfo } from '@/api/login'

const useUserStore = defineStore('user', {
  state: () => ({
    token: getToken(),
    id: '',
    name: '',
    nickName: '',
    avatar: '',
    roles: [],
    permissions: []
  }),

  actions: {
    // 账号密码登录
    login(userInfo) {
      const { username, password, code, uuid } = userInfo
      return new Promise((resolve, reject) => {
        loginApi(username, password, code, uuid).then(res => {
          setToken(res.token)
          this.token = res.token
          resolve(res)
        }).catch(error => reject(error))
      })
    },

    // 微信小程序登录
    wxLogin(code) {
      return new Promise((resolve, reject) => {
        wxLoginApi(code).then(res => {
          setToken(res.token)
          this.token = res.token
          resolve(res)
        }).catch(error => reject(error))
      })
    },

    // 获取用户信息
    getInfo() {
      return new Promise((resolve, reject) => {
        getInfo().then(res => {
          const user = res.user
          const avatar = user.avatar || ''
          if (res.roles && res.roles.length > 0) {
            this.roles = res.roles
            this.permissions = res.permissions
          } else {
            this.roles = ['ROLE_DEFAULT']
          }
          this.id = user.userId
          this.name = user.userName
          this.nickName = user.nickName
          this.avatar = avatar
          resolve(res)
        }).catch(error => reject(error))
      })
    },

    // 退出系统
    logOut() {
      return new Promise((resolve, reject) => {
        logoutApi(this.token).then(() => {
          this.token = ''
          this.roles = []
          this.permissions = []
          removeToken()
          resolve()
        }).catch(() => {
          // 即使后端退出失败也清理本地状态
          this.token = ''
          this.roles = []
          this.permissions = []
          removeToken()
          resolve()
        })
      })
    }
  }
})

export default useUserStore
