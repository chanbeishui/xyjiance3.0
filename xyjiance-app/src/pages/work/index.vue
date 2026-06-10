<template>
  <view class="work-container">
    <view class="page-header">
      <text class="page-title">工作台</text>
    </view>

    <!-- 功能模块 -->
    <view class="module-grid">
      <view class="module-item" v-for="(item, index) in modules" :key="index">
        <u-icon :name="item.icon" :size="44" :color="item.color" />
        <text class="module-label">{{ item.label }}</text>
      </view>
    </view>

    <!-- 最近动态 -->
    <view class="section">
      <view class="section-title">最近动态</view>
      <u-cell-group>
        <u-cell v-for="(item, index) in activities" :key="index" :title="item.title" :label="item.time" icon="checkmark-circle" />
        <u-cell v-if="activities.length === 0" title="暂无动态" icon="info-circle" />
      </u-cell-group>
    </view>
  </view>
</template>

<script setup>
const modules = [
  { label: '用户管理', icon: 'account', color: '#1890FF' },
  { label: '部门管理', icon: 'server-man', color: '#52C41A' },
  { label: '角色管理', icon: 'man-delete', color: '#FAAD14' },
  { label: '菜单管理', icon: 'order', color: '#722ED1' },
  { label: '日志管理', icon: 'file-text', color: '#EB2F96' },
  { label: '更多功能', icon: 'grid', color: '#13C2C2' }
]

const activities = [
  { title: '系统上线运行', time: '2026-06-08 10:00' }
]
</script>

<style lang="scss" scoped>
.work-container {
  padding: 20rpx;
  min-height: 100vh;
  background: #f5f7fa;
}

.page-header {
  padding: 20rpx 10rpx 30rpx;

  .page-title { font-size: 40rpx; font-weight: bold; }
}

.module-grid {
  display: flex;
  flex-wrap: wrap;
  background: #fff;
  border-radius: 16rpx;
  padding: 30rpx 10rpx;
  margin-bottom: 20rpx;
}

.module-item {
  width: 33.33%;
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 20rpx 0;

  .module-label { font-size: 24rpx; color: #333; margin-top: 12rpx; }
}

.section {
  background: #fff;
  border-radius: 16rpx;
  padding: 20rpx 0;
}

.section-title {
  font-size: 30rpx;
  font-weight: bold;
  padding: 10rpx 30rpx 20rpx;
}
</style>
