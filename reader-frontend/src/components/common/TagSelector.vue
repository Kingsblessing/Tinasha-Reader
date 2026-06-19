<template>
  <div class="tag-selector">
    <div class="tag-selector-header">
      <el-input
        v-model="filterText"
        placeholder="筛选标签..."
        clearable
        size="small"
        :prefix-icon="Search"
        class="filter-input"
      />
      <span class="selected-count" v-if="selectedIds.length > 0">
        已选 {{ selectedIds.length }} 个标签
      </span>
    </div>

    <div class="tag-selector-body">
      <el-collapse v-model="expandedGroups" class="group-collapse">
        <el-collapse-item
          v-for="(tags, groupName) in groupedTags"
          :key="groupName"
          :name="groupName"
        >
          <template #title>
            <span class="group-title">{{ groupName || '未分组' }}</span>
            <el-tag size="small" type="info" class="group-count">{{ tags.length }}</el-tag>
          </template>
          <div class="tag-list">
            <el-tag
              v-for="tag in tags"
              :key="tag.id"
              :type="isSelected(tag.id) ? 'primary' : 'info'"
              :style="getTagStyle(tag)"
              class="tag-item"
              @click="toggleTag(tag.id)"
            >
              {{ tag.name }}
            </el-tag>
          </div>
        </el-collapse-item>
      </el-collapse>

      <div v-if="Object.keys(groupedTags).length === 0 && filterText" class="empty-tip">
        无匹配标签
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { Search } from '@element-plus/icons-vue'
import * as tagApi from '../../api/tagApi'

const props = defineProps({
  modelValue: {
    type: Array,
    default: () => []
  },
  multiple: {
    type: Boolean,
    default: true
  }
})

const emit = defineEmits(['update:modelValue'])

const allTags = ref([])
const filterText = ref('')
const expandedGroups = ref([])

const selectedIds = computed(() => props.modelValue || [])

const filteredTags = computed(() => {
  if (!filterText.value) return allTags.value
  const keyword = filterText.value.toLowerCase()
  return allTags.value.filter((tag) => tag.name.toLowerCase().includes(keyword))
})

const groupedTags = computed(() => {
  const groups = {}
  for (const tag of filteredTags.value) {
    const key = tag.groupName || ''
    if (!groups[key]) groups[key] = []
    groups[key].push(tag)
  }
  return groups
})

function isSelected(tagId) {
  return selectedIds.value.includes(tagId)
}

function toggleTag(tagId) {
  const current = [...selectedIds.value]
  const index = current.indexOf(tagId)

  if (index >= 0) {
    current.splice(index, 1)
  } else {
    if (props.multiple) {
      current.push(tagId)
    } else {
      emit('update:modelValue', [tagId])
      return
    }
  }

  emit('update:modelValue', current)
}

function getTagStyle(tag) {
  if (tag.color) {
    return {
      '--el-tag-bg-color': tag.color,
      '--el-tag-border-color': tag.color,
      '--el-tag-text-color': '#ffffff'
    }
  }
  return {}
}

async function loadTags() {
  try {
    const data = await tagApi.getAll()
    allTags.value = Array.isArray(data) ? data : (data?.content || data?.items || [])
    // Expand all groups by default
    expandedGroups.value = Object.keys(groupedTags.value)
  } catch (e) {
    console.error('Failed to load tags:', e)
    allTags.value = []
  }
}

watch(groupedTags, (groups) => {
  // Keep expanded groups in sync when tags reload
  const keys = Object.keys(groups)
  if (expandedGroups.value.length === 0 && keys.length > 0) {
    expandedGroups.value = keys
  }
})

onMounted(() => {
  loadTags()
})
</script>

<style scoped lang="scss">
.tag-selector {
  width: 100%;
}

.tag-selector-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 8px;

  .filter-input {
    flex: 1;
    max-width: 240px;
  }

  .selected-count {
    font-size: 12px;
    color: var(--el-color-primary);
    white-space: nowrap;
  }
}

.tag-selector-body {
  max-height: 260px;
  overflow-y: auto;
  border: 1px solid var(--el-border-color-lighter);
  border-radius: 6px;
  padding: 4px 8px;
}

.group-collapse {
  border: none;

  :deep(.el-collapse-item__header) {
    height: 36px;
    font-size: 14px;
    background: transparent;
    border: none;
  }

  :deep(.el-collapse-item__wrap) {
    background: transparent;
    border: none;
  }

  :deep(.el-collapse-item__content) {
    padding-bottom: 8px;
  }
}

.group-title {
  font-weight: 500;
  color: var(--text-primary);
}

.group-count {
  margin-left: 8px;
}

.tag-list {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.tag-item {
  cursor: pointer;
  transition: opacity 0.15s;

  &:hover {
    opacity: 0.85;
  }
}

.empty-tip {
  text-align: center;
  padding: 16px;
  color: var(--el-text-color-secondary);
  font-size: 13px;
}
</style>
