<template>
  <view class="mine-container">
    <!-- 用户信息 -->
    <view class="user-card" @click="goProfile">
      <u-avatar :src="userStore.avatar" size="80" />
      <view class="user-detail">
        <text class="name">{{ userStore.nickName || userStore.name || '未登录' }}</text>
        <text class="dept">{{ userStore.roles.length > 0 ? userStore.roles[0] : '' }}</text>
      </view>
      <u-icon name="arrow-right" color="#ccc" size="20" />
    </view>

    <!-- 菜单 -->
    <view class="menu-section">
      <u-cell-group>
        <u-cell title="个人信息" icon="account" :isLink="true" />
        <u-cell title="修改密码" icon="lock" :isLink="true" />
        <u-cell title="系统设置" icon="setting" :isLink="true" />
        <u-cell title="关于我们" icon="info-circle" :isLink="true" />
      </u-cell-group>
    </view>

    <!-- 退出登录 -->
    <view class="logout-section">
      <u-button
        v-if="userStore.token"
        text="退出登录"
        type="error"
        plain
        shape="circle"
        @click="handleLogout"
      />
    </view>
  </view>
</template>

<script setup>
import useUserStore from '@/store/modules/user'

const userStore = useUserStore()

const goProfile = () => {
  // 跳转个人信息页
}

const handleLogout = () => {
  uni.showModal({
    title: '提示',
    content: '确定要退出登录吗？',
    success: async (res) => {
      if (res.confirm) {
        await userStore.logOut()
        uni.showToast({ title: '已退出', icon: 'success' })
        setTimeout(() => {
          uni.reLaunch({ url: '/pages/login/index' })
        }, 500)
      }
    }
  })
}
</script>

<style lang="scss" scoped>
.mine-container {
  padding: 20rpx;
  min-height: 100vh;
  background: #f5f7fa;
}

.user-card {
  display: flex;
  align-items: center;
  background: #fff;
  border-radius: 16rpx;
  padding: 40rpx 30rpx;
  margin-bottom: 20rpx;
}

.user-detail {
  flex: 1;
  margin-left: 24rpx;

  .name { font-size: 36rpx; font-weight: bold; display: block; }
  .dept { font-size: 26rpx; color: #999; margin-top: 6rpx; display: block; }
}

.menu-section { margin-bottom: 20rpx; border-radius: 16rpx; overflow: hidden; }

.logout-section { padding: 40rpx 0; }
</style>
