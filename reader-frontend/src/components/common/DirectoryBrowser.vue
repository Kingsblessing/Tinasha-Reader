<template>
  <el-dialog
    :model-value="modelValue"
    title="选择目录或文件"
    width="680px"
    :close-on-click-modal="false"
    @update:model-value="$emit('update:modelValue', $event)"
  >
    <!-- Breadcrumb / current path -->
    <div class="browser-path">
      <el-icon><FolderOpened /></el-icon>
      <span class="path-text">{{ currentPath || '此电脑' }}</span>
      <el-button
        v-if="parentPath"
        text
        size="small"
        @click="navigateTo(parentPath)"
      >
        <el-icon><Top /></el-icon> 上级目录
      </el-button>
    </div>

    <!-- Quick drives -->
    <div class="browser-drives" v-if="!currentPath">
      <el-button
        v-for="drive in drives"
        :key="drive"
        size="small"
        @click="navigateTo(drive)"
      >
        <el-icon><FolderOpened /></el-icon> {{ drive }}
      </el-button>
    </div>

    <!-- Item list -->
    <div class="browser-list" v-loading="loading">
      <div
        v-for="item in items"
        :key="item.path"
        class="browser-item"
        :class="{
          selected: selectedPaths.has(item.path),
          'is-directory': item.isDirectory
        }"
        @click="onItemClick(item)"
      >
        <el-checkbox
          :model-value="selectedPaths.has(item.path)"
          @change="toggleSelect(item)"
          @click.stop
        />
        <el-icon class="item-icon" :size="18">
          <FolderOpened v-if="item.isDirectory" />
          <Document v-else />
        </el-icon>
        <span class="item-name" :title="item.path">{{ item.name }}</span>
        <span class="item-size" v-if="!item.isDirectory && item.size">
          {{ formatSize(item.size) }}
        </span>
        <el-tag
          v-if="item.type === 'comic'"
          size="small"
          type="success"
          class="item-tag"
        >
          漫画
        </el-tag>
        <el-tag
          v-if="item.type === 'image'"
          size="small"
          type="info"
          class="item-tag"
        >
          图片
        </el-tag>
      </div>

      <div v-if="items.length === 0 && !loading" class="browser-empty">
        空目录
      </div>
    </div>

    <!-- Selected summary -->
    <div class="browser-summary" v-if="selectedPaths.size > 0">
      已选择 <strong>{{ selectedPaths.size }}</strong> 项
    </div>

    <template #footer>
      <el-button @click="$emit('update:modelValue', false)">取消</el-button>
      <el-button type="primary" @click="confirm" :disabled="selectedPaths.size === 0">
        确定选择
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, watch } from 'vue'
import { FolderOpened, Document, Top } from '@element-plus/icons-vue'
import { browse } from '../../api/filesystemApi'

const props = defineProps({
  modelValue: Boolean,
  multiple: { type: Boolean, default: true }
})

const emit = defineEmits(['update:modelValue', 'confirm'])

const currentPath = ref('')
const parentPath = ref(null)
const items = ref([])
const drives = ref([])
const loading = ref(false)
const selectedPaths = ref(new Set())

watch(() => props.modelValue, (val) => {
  if (val) {
    selectedPaths.value.clear()
    navigateTo('')
  }
})

async function navigateTo(path) {
  loading.value = true
  try {
    const data = await browse(path)
    currentPath.value = data.currentPath || ''
    parentPath.value = data.parentPath
    items.value = data.items || []

    // Extract drives on first load
    if (!path && items.value.length > 0) {
      drives.value = items.value.filter(i => i.type === 'drive').map(i => i.path)
    }
  } catch (e) {
    items.value = []
  } finally {
    loading.value = false
  }
}

function onItemClick(item) {
  if (item.isDirectory) {
    navigateTo(item.path)
  } else {
    toggleSelect(item)
  }
}

function toggleSelect(item) {
  const paths = new Set(selectedPaths.value)
  if (paths.has(item.path)) {
    paths.delete(item.path)
  } else {
    if (!props.multiple) {
      paths.clear()
    }
    paths.add(item.path)
  }
  selectedPaths.value = paths
}

function formatSize(bytes) {
  if (bytes < 1024) return bytes + ' B'
  if (bytes < 1048576) return (bytes / 1024).toFixed(1) + ' KB'
  if (bytes < 1073741824) return (bytes / 1048576).toFixed(1) + ' MB'
  return (bytes / 1073741824).toFixed(1) + ' GB'
}

function confirm() {
  emit('confirm', Array.from(selectedPaths.value))
  emit('update:modelValue', false)
}
</script>

<style scoped lang="scss">
.browser-path {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 12px;
  background-color: var(--bg-secondary);
  border-radius: 6px;
  margin-bottom: 12px;
  font-size: 13px;
  color: var(--text-secondary);
}

.path-text {
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  font-family: monospace;
}

.browser-drives {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 12px;
}

.browser-list {
  max-height: 400px;
  overflow-y: auto;
  border: 1px solid var(--border-color);
  border-radius: 6px;
}

.browser-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 12px;
  cursor: pointer;
  transition: background-color 0.15s;
  border-bottom: 1px solid var(--bg-secondary);

  &:last-child {
    border-bottom: none;
  }

  &:hover {
    background-color: var(--accent-light);
  }

  &.selected {
    background-color: rgba(64, 158, 255, 0.1);
  }

  &.is-directory .item-name {
    font-weight: 500;
  }
}

.item-icon {
  color: var(--text-tertiary);
  flex-shrink: 0;

  .is-directory & {
    color: #e6a23c;
  }
}

.item-name {
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  font-size: 13px;
}

.item-size {
  font-size: 12px;
  color: var(--text-tertiary);
  flex-shrink: 0;
}

.item-tag {
  flex-shrink: 0;
}

.browser-empty {
  padding: 32px;
  text-align: center;
  color: var(--text-tertiary);
  font-size: 13px;
}

.browser-summary {
  margin-top: 10px;
  font-size: 13px;
  color: var(--text-secondary);
}
</style>
