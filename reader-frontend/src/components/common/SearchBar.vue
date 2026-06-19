<template>
  <div class="search-bar">
    <el-input
      v-model="query"
      placeholder="搜索漫画..."
      clearable
      :prefix-icon="Search"
      @input="onInput"
      @clear="onClear"
      @keyup.enter="onSearch"
    />
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { Search } from '@element-plus/icons-vue'

const emit = defineEmits(['search'])
const query = ref('')
let debounceTimer = null

function onInput() {
  if (debounceTimer) clearTimeout(debounceTimer)
  debounceTimer = setTimeout(() => {
    emit('search', query.value)
  }, 300)
}

function onClear() {
  if (debounceTimer) clearTimeout(debounceTimer)
  emit('search', '')
}

function onSearch() {
  if (debounceTimer) clearTimeout(debounceTimer)
  emit('search', query.value)
}
</script>

<style scoped lang="scss">
.search-bar {
  width: 100%;

  :deep(.el-input__wrapper) {
    border-radius: 20px;
    padding: 0 16px;
  }
}
</style>
