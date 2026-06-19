<template>
  <el-header class="app-header">
    <div class="header-left">
      <el-button
        v-if="showSidebarToggle"
        :icon="Expand"
        text
        class="sidebar-toggle"
        @click="$emit('toggle-sidebar')"
      />
      <router-link to="/library" class="logo">
        <span class="logo-icon">&#128214;</span>
        <span class="logo-text">Reader</span>
      </router-link>
    </div>

    <div class="header-right">
      <el-menu mode="horizontal" :ellipsis="false" :default-active="activeRoute" router class="nav-menu">
        <el-menu-item index="/library" title="书架">
          <el-icon><Collection /></el-icon>
          <span class="nav-label">书架</span>
        </el-menu-item>
        <el-menu-item index="/favorites" title="收藏">
          <el-icon><Star /></el-icon>
          <span class="nav-label">收藏</span>
        </el-menu-item>
        <el-menu-item index="/history" title="历史">
          <el-icon><Clock /></el-icon>
          <span class="nav-label">历史</span>
        </el-menu-item>
        <el-menu-item index="/stats" title="统计">
          <el-icon><DataAnalysis /></el-icon>
          <span class="nav-label">统计</span>
        </el-menu-item>
      </el-menu>

      <div class="header-divider"></div>

      <el-tooltip :content="isDark ? '亮色' : '暗色'" placement="bottom">
        <button class="icon-btn" @click="toggleTheme">
          <span v-if="isDark">&#9728;&#65039;</span>
          <span v-else>&#127769;</span>
        </button>
      </el-tooltip>

      <el-tooltip content="设置" placement="bottom">
        <button class="icon-btn" @click="$router.push('/settings')">
          &#9881;&#65039;
        </button>
      </el-tooltip>
    </div>
  </el-header>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import { Expand, Collection, Star, Clock, DataAnalysis } from '@element-plus/icons-vue'
import { useTheme } from '../../composables/useTheme'

defineProps({
  showSidebarToggle: { type: Boolean, default: false }
})

defineEmits(['toggle-sidebar'])

const route = useRoute()
const { isDark, toggleTheme } = useTheme()

const activeRoute = computed(() => {
  const path = route.path
  if (path.startsWith('/library')) return '/library'
  if (path.startsWith('/favorites')) return '/favorites'
  if (path.startsWith('/history')) return '/history'
  if (path.startsWith('/stats')) return '/stats'
  return path
})
</script>

<style scoped lang="scss">
.app-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 12px;
  height: 50px;
  min-height: 50px;
  background-color: var(--header-bg);
  border-bottom: 1px solid var(--border-color);
  position: sticky;
  top: 0;
  z-index: 100;
  overflow: hidden;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 6px;
  flex-shrink: 0;
}

.sidebar-toggle {
  color: var(--text-tertiary);
  padding: 4px;
  &:hover { color: var(--accent-color); }
}

.logo {
  display: flex;
  align-items: center;
  gap: 6px;
  text-decoration: none;
  &:hover { text-decoration: none; }
}

.logo-icon {
  font-size: 18px;
}

.logo-text {
  font-size: 15px;
  font-weight: 700;
  color: var(--text-primary);
  letter-spacing: -0.3px;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 2px;
  flex-shrink: 0;
}

.header-divider {
  width: 1px;
  height: 18px;
  background-color: var(--border-color);
  margin: 0 6px;
  flex-shrink: 0;
}

.nav-menu {
  border-bottom: none;
  background: transparent;
}

.nav-menu .el-menu-item {
  height: 50px;
  line-height: 50px;
  padding: 0 10px;
  font-size: 13px;
  color: var(--text-secondary) !important;
  display: flex;
  align-items: center;

  .el-icon {
    font-size: 16px;
    vertical-align: middle;
  }

  &.is-active {
    color: var(--accent-color) !important;
    font-weight: 600;
  }
}

.nav-label {
  margin-left: 4px;
  vertical-align: middle;
  line-height: 1;
}

.icon-btn {
  width: 30px;
  height: 30px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: none;
  background: none;
  cursor: pointer;
  border-radius: 6px;
  font-size: 15px;
  transition: background-color 0.15s;
  color: var(--text-secondary);
  flex-shrink: 0;

  &:hover {
    background-color: var(--accent-light);
  }
}

@media (max-width: 768px) {
  .app-header {
    height: 46px;
    min-height: 46px;
    padding: 0 8px;
  }

  .logo-text {
    display: none;
  }

  .logo-icon {
    font-size: 20px;
  }

  .nav-label {
    display: none;
  }

  .nav-menu .el-menu-item {
    height: 46px;
    line-height: 46px;
    padding: 0 8px;

    .el-icon {
      font-size: 18px;
    }
  }

  .header-divider {
    margin: 0 4px;
  }

  .icon-btn {
    width: 28px;
    height: 28px;
    font-size: 14px;
  }
}

@media (max-width: 480px) {
  .header-left {
    gap: 4px;
  }

  .nav-menu .el-menu-item {
    padding: 0 6px;
  }

  .header-divider {
    display: none;
  }
}
</style>
