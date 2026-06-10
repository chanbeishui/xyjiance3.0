<template>
  <view class="home-container">
    <!-- 头部 -->
    <view class="header">
      <view class="user-info" v-if="userStore.token">
        <u-avatar :src="userStore.avatar" size="56" />
        <view class="user-text">
          <text class="greeting">{{ greeting }}</text>
          <text class="nickname">{{ userStore.nickName || userStore.name }}</text>
        </view>
      </view>
      <view class="header-action" v-else>
        <u-button text="去登录" type="primary" size="small" @click="goLogin" />
      </view>
    </view>

    <!-- 快捷入口 -->
    <view class="shortcuts">
      <u-grid :col="4" :border="false">
        <u-grid-item v-for="(item, index) in shortcuts" :key="index" @click="navigateTo(item.path)">
          <u-icon :name="item.icon" :size="40" :color="item.color" />
          <text class="grid-text">{{ item.label }}</text>
        </u-grid-item>
      </u-grid>
    </view>

    <!-- 公告 -->
    <view class="notice-card">
      <u-notice-bar :text="noticeText" mode="horizontal" />
    </view>

    <!-- 待办 -->
    <view class="section">
      <view class="section-title">
        <text>待办事项</text>
        <text class="more">查看全部 ></text>
      </view>
      <u-cell-group>
        <u-cell title="暂无待办事项" icon="file-text" />
      </u-cell-group>
    </view>
  </view>
</template>

<script setup>
import { ref, computed } from 'vue'
import useUserStore from '@/store/modules/user'

const userStore = useUserStore()

const noticeText = '欢迎使用集信管理系统，如有问题请联系管理员。'

const greeting = computed(() => {
  const hour = new Date().getHours()
  if (hour < 6) return '夜深了'
  if (hour < 12) return '早上好'
  if (hour < 14) return '中午好'
  if (hour < 18) return '下午好'
  return '晚上好'
})

const shortcuts = [
  { label: '公告通知', icon: 'bell', color: '#1890FF', path: '/pages/work/index' },
  { label: '我的任务', icon: 'file-text', color: '#52C41A', path: '/pages/work/index' },
  { label: '审批', icon: 'checkmark-circle', color: '#FAAD14', path: '/pages/work/index' },
  { label: '更多', icon: 'grid', color: '#722ED1', path: '/pages/work/index' }
]

const navigateTo = (path) => {
  uni.switchTab({ url: path })
}

const goLogin = () => {
  uni.navigateTo({ url: '/pages/login/index' })
}
</script>

<style lang="scss" scoped>
.home-container {
  padding: 20rpx;
  min-height: 100vh;
  background: #f5f7fa;
}

.header {
  background: linear-gradient(135deg, #1890FF, #36CFC9);
  border-radius: 16rpx;
  padding: 40rpx 30rpx;
  margin-bottom: 20rpx;

  .user-info {
    display: flex;
    align-items: center;
  }

  .user-text {
    margin-left: 20rpx;
    display: flex;
    flex-direction: column;

    .greeting { font-size: 28rpx; color: rgba(255,255,255,0.8); }
    .nickname { font-size: 36rpx; color: #fff; font-weight: bold; }
  }
}

.shortcuts {
  background: #fff;
  border-radius: 16rpx;
  padding: 20rpx 0;
  margin-bottom: 20rpx;

  .grid-text { font-size: 24rpx; color: #666; margin-top: 8rpx; }
}

.notice-card { margin-bottom: 20rpx; }

.section {
  background: #fff;
  border-radius: 16rpx;
  padding: 20rpx 0;
}

.section-title {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10rpx 30rpx 20rpx;
  font-size: 30rpx;
  font-weight: bold;

  .more { font-size: 26rpx; color: #1890FF; font-weight: normal; }
}
</style>
