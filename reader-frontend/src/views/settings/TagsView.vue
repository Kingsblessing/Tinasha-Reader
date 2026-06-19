<template>
  <div class="tags-view">
    <div class="tags-header">
      <el-button :icon="Back" text @click="$router.push('/settings')">返回设置</el-button>
      <h2 class="page-title">标签管理</h2>
      <el-button type="primary" @click="showAddDialog = true">
        <el-icon><Plus /></el-icon>
        添加标签
      </el-button>
    </div>

    <div class="tags-content">
      <!-- Group filter sidebar -->
      <div class="group-filter">
        <div
          class="filter-item"
          :class="{ active: selectedGroup === null }"
          @click="selectedGroup = null"
        >
          全部
        </div>
        <div
          v-for="group in groups"
          :key="group"
          class="filter-item"
          :class="{ active: selectedGroup === group }"
          @click="selectedGroup = group"
        >
          {{ group }}
        </div>
      </div>

      <!-- Tags table -->
      <div class="tags-table-wrapper">
        <el-table :data="filteredTags" v-loading="loading" stripe>
          <el-table-column prop="name" label="名称" min-width="120" />
          <el-table-column prop="group" label="分组" width="120">
            <template #default="{ row }">
              <el-tag size="small" v-if="row.group">{{ row.group }}</el-tag>
              <span v-else class="text-muted">未分组</span>
            </template>
          </el-table-column>
          <el-table-column prop="color" label="颜色" width="80" align="center">
            <template #default="{ row }">
              <div
                class="color-preview"
                :style="{ backgroundColor: row.color || '#409eff' }"
              ></div>
            </template>
          </el-table-column>
          <el-table-column prop="comicCount" label="漫画数" width="80" align="center" />
          <el-table-column label="操作" width="160" align="center">
            <template #default="{ row }">
              <el-button size="small" @click="editTag(row)">编辑</el-button>
              <el-button size="small" type="danger" @click="deleteTag(row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </div>

    <!-- Add/Edit dialog -->
    <el-dialog
      v-model="showAddDialog"
      :title="editingTag ? '编辑标签' : '添加标签'"
      width="400px"
      @close="resetForm"
    >
      <el-form :model="form" label-width="60px">
        <el-form-item label="名称" required>
          <el-input v-model="form.name" placeholder="输入标签名称" />
        </el-form-item>
        <el-form-item label="分组">
          <el-select
            v-model="form.group"
            filterable
            allow-create
            placeholder="选择或创建分组"
            style="width: 100%;"
          >
            <el-option
              v-for="group in groups"
              :key="group"
              :label="group"
              :value="group"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="颜色">
          <el-color-picker v-model="form.color" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showAddDialog = false">取消</el-button>
        <el-button type="primary" @click="submitForm" :loading="submitting">
          {{ editingTag ? '保存' : '添加' }}
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { Back, Plus } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import * as tagApi from '../../api/tagApi'

const tags = ref([])
const groups = ref([])
const loading = ref(true)
const selectedGroup = ref(null)
const showAddDialog = ref(false)
const editingTag = ref(null)
const submitting = ref(false)

const form = reactive({
  name: '',
  group: '',
  color: '#409eff'
})

const filteredTags = computed(() => {
  if (!selectedGroup.value) return tags.value
  return tags.value.filter(t => t.group === selectedGroup.value)
})

async function loadTags() {
  loading.value = true
  try {
    tags.value = await tagApi.getAll() || []
    // Extract unique groups
    const groupSet = new Set(tags.value.map(t => t.group).filter(Boolean))
    groups.value = Array.from(groupSet).sort()
  } catch (e) {
    tags.value = []
    groups.value = []
  } finally {
    loading.value = false
  }
}

function editTag(tag) {
  editingTag.value = tag
  form.name = tag.name
  form.group = tag.group || ''
  form.color = tag.color || '#409eff'
  showAddDialog.value = true
}

function resetForm() {
  editingTag.value = null
  form.name = ''
  form.group = ''
  form.color = '#409eff'
}

async function submitForm() {
  if (!form.name) {
    ElMessage.warning('请输入标签名称')
    return
  }

  submitting.value = true
  try {
    if (editingTag.value) {
      await tagApi.update(editingTag.value.id, { ...form })
      ElMessage.success('标签已更新')
    } else {
      await tagApi.create({ ...form })
      ElMessage.success('标签已添加')
    }
    showAddDialog.value = false
    await loadTags()
  } catch (e) {
    ElMessage.error('操作失败')
  } finally {
    submitting.value = false
  }
}

async function deleteTag(tag) {
  try {
    await ElMessageBox.confirm(`确定删除标签 "${tag.name}"？`, '提示', { type: 'warning' })
    await tagApi.deleteTag(tag.id)
    await loadTags()
    ElMessage.success('标签已删除')
  } catch (e) {
    // User cancelled
  }
}

onMounted(loadTags)
</script>

<style scoped lang="scss">
.tags-view {
  max-width: 1200px;
  margin: 0 auto;
}

.tags-header {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 24px;
}

.page-title {
  font-size: 24px;
  font-weight: 600;
  color: var(--text-primary);
  flex: 1;
}

.tags-content {
  display: flex;
  gap: 24px;
}

.group-filter {
  width: 160px;
  flex-shrink: 0;
}

.filter-item {
  padding: 10px 16px;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
  color: var(--text-secondary);
  transition: all 0.2s ease;
  margin-bottom: 4px;

  &:hover {
    background-color: var(--bg-secondary);
    color: var(--text-primary);
  }

  &.active {
    background-color: var(--accent-light);
    color: var(--accent-color);
    font-weight: 500;
  }
}

.tags-table-wrapper {
  flex: 1;
  min-width: 0;
}

.color-preview {
  width: 24px;
  height: 24px;
  border-radius: 4px;
  margin: 0 auto;
}

.text-muted {
  color: var(--text-tertiary);
  font-size: 12px;
}

@media (max-width: 768px) {
  .tags-content {
    flex-direction: column;
  }

  .group-filter {
    width: 100%;
    display: flex;
    overflow-x: auto;
    gap: 8px;
    padding-bottom: 8px;
  }

  .filter-item {
    white-space: nowrap;
    margin-bottom: 0;
  }
}
</style>
