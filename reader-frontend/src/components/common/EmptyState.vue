<template>
  <div class="empty-state">
    <el-icon class="empty-icon" :size="64">
      <component :is="iconComponent" />
    </el-icon>
    <p class="empty-message">{{ message }}</p>
    <slot />
  </div>
</template>

<script setup>
import { computed } from 'vue'
import * as icons from '@element-plus/icons-vue'

const props = defineProps({
  message: {
    type: String,
    default: '暂无数据'
  },
  icon: {
    type: String,
    default: 'FolderOpened'
  }
})

const iconComponent = computed(() => icons[props.icon] || icons.FolderOpened)
</script>

<style scoped lang="scss">
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 80px 20px;
  color: var(--text-tertiary);
}

.empty-icon {
  margin-bottom: 16px;
  opacity: 0.5;
}

.empty-message {
  font-size: 16px;
  color: var(--text-tertiary);
}
</style>
