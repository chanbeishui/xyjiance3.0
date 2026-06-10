<template>
  <view class="login-container">
    <view class="login-header">
      <image class="logo" src="/static/images/logo.png" mode="aspectFit" />
      <text class="title">集信管理系统</text>
    </view>

    <view class="login-form">
      <u-form :model="loginForm" ref="loginFormRef">
        <u-form-item>
          <u-input
            v-model="loginForm.username"
            placeholder="请输入账号"
            prefixIcon="account"
            clearable
          />
        </u-form-item>
        <u-form-item>
          <u-input
            v-model="loginForm.password"
            type="password"
            placeholder="请输入密码"
            prefixIcon="lock"
          />
        </u-form-item>
      </u-form>

      <u-button
        type="primary"
        :loading="loading"
        @click="handleLogin"
        text="登 录"
        shape="circle"
        size="large"
        class="login-btn"
      />
    </view>

    <!-- 微信登录 -->
    <view class="wx-login">
      <u-divider text="其他登录方式" />
      <u-button
        type="success"
        :loading="wxLoading"
        @click="handleWxLogin"
        text="微信一键登录"
        shape="circle"
        size="large"
        plain
        class="wx-btn"
      />
    </view>

    <view class="login-footer">
      <text class="copyright">Copyright © 2026 集信</text>
    </view>
  </view>
</template>

<script setup>
import { ref, reactive } from 'vue'
import useUserStore from '@/store/modules/user'

const userStore = useUserStore()
const loading = ref(false)
const wxLoading = ref(false)

const loginForm = reactive({
  username: '',
  password: ''
})

// 账号密码登录
const handleLogin = async () => {
  if (!loginForm.username) {
    uni.showToast({ title: '请输入账号', icon: 'none' })
    return
  }
  if (!loginForm.password) {
    uni.showToast({ title: '请输入密码', icon: 'none' })
    return
  }
  loading.value = true
  try {
    await userStore.login({ username: loginForm.username, password: loginForm.password })
    await userStore.getInfo()
    uni.showToast({ title: '登录成功', icon: 'success' })
    setTimeout(() => {
      uni.switchTab({ url: '/pages/index/index' })
    }, 500)
  } catch (e) {
    console.error('登录失败:', e)
  } finally {
    loading.value = false
  }
}

// 微信登录
const handleWxLogin = async () => {
  wxLoading.value = true
  try {
    // 1. 调用微信登录获取 code
    const loginRes = await uni.login({ provider: 'weixin' })
    // 2. 发送 code 到后端换取 token
    await userStore.wxLogin(loginRes.code)
    // 3. 获取用户信息
    await userStore.getInfo()
    uni.showToast({ title: '登录成功', icon: 'success' })
    setTimeout(() => {
      uni.switchTab({ url: '/pages/index/index' })
    }, 500)
  } catch (e) {
    console.error('微信登录失败:', e)
    uni.showToast({ title: '登录失败，请重试', icon: 'none' })
  } finally {
    wxLoading.value = false
  }
}
</script>

<style lang="scss" scoped>
.login-container {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40rpx;
  background: linear-gradient(135deg, #f5f7fa 0%, #e4e9f2 100%);
}

.login-header {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-bottom: 60rpx;

  .logo { width: 160rpx; height: 160rpx; margin-bottom: 20rpx; }
  .title { font-size: 40rpx; font-weight: bold; color: #303133; }
}

.login-form {
  width: 100%;
  background: #fff;
  border-radius: 20rpx;
  padding: 40rpx 30rpx;
  box-shadow: 0 4rpx 20rpx rgba(0, 0, 0, 0.06);
}

.login-btn {
  margin-top: 40rpx;
  width: 100%;
}

.wx-login {
  width: 100%;
  margin-top: 40rpx;
}

.wx-btn {
  width: 100%;
  margin-top: 20rpx;
}

.login-footer {
  position: absolute;
  bottom: 60rpx;

  .copyright {
    font-size: 24rpx;
    color: #999;
  }
}
</style>
