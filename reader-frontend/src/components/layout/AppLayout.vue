<template>
  <el-container class="app-layout" direction="vertical">
    <AppHeader
      :show-sidebar-toggle="showSidebar"
      @toggle-sidebar="sidebarCollapsed = !sidebarCollapsed"
    />
    <el-container class="main-container">
      <AppSidebar
        v-if="showSidebar"
        :collapsed="sidebarCollapsed"
        @filter-select="onFilterSelect"
        ref="sidebarRef"
      />
      <el-main class="main-content">
        <slot />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, provide } from 'vue'
import { useRoute } from 'vue-router'
import AppHeader from './AppHeader.vue'
import AppSidebar from './AppSidebar.vue'

const route = useRoute()
const sidebarRef = ref(null)
const windowWidth = ref(window.innerWidth)
const sidebarCollapsed = ref(window.innerWidth < 768)

const showSidebar = computed(() => {
  const hiddenRoutes = ['/reader/', '/settings']
  return !hiddenRoutes.some(r => route.path.startsWith(r))
})

// Provide sidebar filter events to child views
const filterCallbacks = []
provide('onSidebarFilter', (cb) => {
  filterCallbacks.push(cb)
})

function onFilterSelect(filter) {
  filterCallbacks.forEach(cb => cb(filter))
}

function handleResize() {
  windowWidth.value = window.innerWidth
  if (window.innerWidth < 768) {
    sidebarCollapsed.value = true
  }
}

onMounted(() => {
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
})

defineExpose({ sidebarRef })
</script>

<style scoped lang="scss">
.app-layout {
  height: 100vh;
  overflow: hidden;
}

.main-container {
  flex: 1;
  overflow: hidden;
}

.main-content {
  padding: 20px;
  overflow-y: auto;
  background-color: var(--bg-primary);
}
</style>
