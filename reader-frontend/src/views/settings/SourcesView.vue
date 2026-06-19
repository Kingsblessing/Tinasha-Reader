<template>
  <div class="sources-view">
    <div class="sources-header">
      <el-button :icon="Back" text @click="$router.push('/settings')">返回设置</el-button>
      <h2 class="page-title">来源管理</h2>
      <el-button type="primary" @click="openAddDialog">
        <el-icon><Plus /></el-icon>
        添加来源
      </el-button>
    </div>

    <!-- Sources table -->
    <el-table :data="sources" v-loading="loading" stripe>
      <el-table-column prop="name" label="名称" min-width="120" />
      <el-table-column prop="path" label="路径" min-width="200" show-overflow-tooltip />
      <el-table-column prop="type" label="类型" width="100">
        <template #default="{ row }">
          {{ SourceTypeLabels[row.type] || row.type }}
        </template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="statusType(row.status || row.scanStatus)" size="small">
            {{ SourceStatusLabels[row.status || row.scanStatus] || row.status || row.scanStatus }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="comicCount" label="漫画数" width="80" align="center" />
      <el-table-column label="启用" width="80" align="center">
        <template #default="{ row }">
          <el-switch
            :model-value="row.enabled"
            @change="(val) => onToggleEnabled(row, val)"
          />
        </template>
      </el-table-column>
      <el-table-column label="操作" width="240" align="center">
        <template #default="{ row }">
          <el-button size="small" @click="scanSource(row)" :loading="row.scanning">
            扫描
          </el-button>
          <el-button size="small" @click="editSource(row)">编辑</el-button>
          <el-button size="small" type="danger" @click="deleteSource(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- Add dialog with directory browser -->
    <el-dialog
      v-model="showAddDialog"
      :title="editingSource ? '编辑来源' : '添加来源'"
      width="580px"
      @close="resetForm"
    >
      <el-form :model="form" label-width="80px">
        <el-form-item label="名称" required>
          <el-input v-model="form.name" placeholder="输入来源名称" />
        </el-form-item>
        <el-form-item label="路径" required>
          <div class="path-input-row">
            <el-input v-model="form.path" placeholder="点击右侧按钮选择目录" readonly />
            <el-button @click="showBrowser = true">
              <el-icon><FolderOpened /></el-icon>
              浏览
            </el-button>
          </div>
          <div class="path-hint" v-if="pendingPaths.length > 1">
            已选择 {{ pendingPaths.length }} 个路径，将逐个添加
          </div>
        </el-form-item>
        <el-form-item label="类型">
          <el-select v-model="form.type" style="width: 100%;">
            <el-option
              v-for="(label, key) in SourceTypeLabels"
              :key="key"
              :label="label"
              :value="key"
            />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showAddDialog = false">取消</el-button>
        <el-button type="primary" @click="submitForm" :loading="submitting">
          {{ editingSource ? '保存' : '添加' }}
        </el-button>
      </template>
    </el-dialog>

    <!-- Directory browser dialog -->
    <DirectoryBrowser
      v-model="showBrowser"
      :multiple="!editingSource"
      @confirm="onPathsSelected"
    />
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { Back, Plus, FolderOpened } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import DirectoryBrowser from '../../components/common/DirectoryBrowser.vue'
import * as sourceApi from '../../api/sourceApi'
import { SourceTypeLabels, SourceStatusLabels } from '../../utils/constants'

const sources = ref([])
const loading = ref(true)
const showAddDialog = ref(false)
const showBrowser = ref(false)
const editingSource = ref(null)
const submitting = ref(false)
const pendingPaths = ref([])

const form = reactive({
  name: '',
  path: '',
  type: 'FOLDER'
})

function statusType(status) {
  switch (status) {
    case 'IDLE': return 'success'
    case 'SCANNING': return 'warning'
    case 'ERROR': return 'danger'
    default: return 'info'
  }
}

async function loadSources() {
  loading.value = true
  try {
    sources.value = await sourceApi.getAll() || []
  } catch (e) {
    sources.value = []
  } finally {
    loading.value = false
  }
}

function openAddDialog() {
  editingSource.value = null
  resetForm()
  showAddDialog.value = true
}

function editSource(source) {
  editingSource.value = source
  form.name = source.name
  form.path = source.path
  form.type = source.type || 'FOLDER'
  pendingPaths.value = []
  showAddDialog.value = true
}

function resetForm() {
  editingSource.value = null
  form.name = ''
  form.path = ''
  form.type = 'FOLDER'
  pendingPaths.value = []
}

function onPathsSelected(paths) {
  if (paths.length === 0) return
  pendingPaths.value = paths
  form.path = paths[0]
  // Auto-generate name from path if empty
  if (!form.name) {
    const parts = paths[0].replace(/\\/g, '/').split('/')
    form.name = parts[parts.length - 1] || paths[0]
  }
}

async function submitForm() {
  if (!form.path) {
    ElMessage.warning('请选择路径')
    return
  }

  submitting.value = true
  try {
    if (editingSource.value) {
      // Edit mode: update single source
      if (!form.name) {
        ElMessage.warning('请填写名称')
        return
      }
      await sourceApi.update(editingSource.value.id, { ...form })
      ElMessage.success('来源已更新')
    } else {
      // Add mode: add one source per selected path
      const paths = pendingPaths.value.length > 0 ? pendingPaths.value : [form.path]
      let added = 0
      for (const p of paths) {
        const name = paths.length === 1 && form.name
          ? form.name
          : p.replace(/\\/g, '/').split('/').filter(Boolean).pop() || p
        try {
          await sourceApi.create({ name, path: p, type: form.type })
          added++
        } catch (e) {
          console.error('Failed to add source:', p, e)
        }
      }
      ElMessage.success(`成功添加 ${added} 个来源`)
    }
    showAddDialog.value = false
    await loadSources()
  } catch (e) {
    ElMessage.error('操作失败')
  } finally {
    submitting.value = false
  }
}

async function deleteSource(source) {
  try {
    await ElMessageBox.confirm(`确定删除来源 "${source.name}"？`, '提示', { type: 'warning' })
    await sourceApi.deleteSource(source.id)
    await loadSources()
    ElMessage.success('来源已删除')
  } catch (e) {
    // User cancelled
  }
}

async function scanSource(source) {
  source.scanning = true
  try {
    await sourceApi.triggerScan(source.id)
    ElMessage.success('扫描已启动')
    pollScanStatus(source)
  } catch (e) {
    ElMessage.error('扫描启动失败')
    source.scanning = false
  }
}

async function pollScanStatus(source) {
  const poll = async () => {
    try {
      const status = await sourceApi.getScanStatus(source.id)
      if (status === 'SCANNING') {
        setTimeout(poll, 2000)
      } else {
        source.scanning = false
        source.status = status
        await loadSources()
      }
    } catch (e) {
      source.scanning = false
    }
  }
  setTimeout(poll, 2000)
}

async function onToggleEnabled(source, enabled) {
  try {
    await sourceApi.toggleEnabled(source.id, enabled)
    source.enabled = enabled
    ElMessage.success(enabled ? '已启用' : '已禁用')
  } catch (e) {
    ElMessage.error('操作失败')
  }
}

onMounted(loadSources)
</script>

<style scoped lang="scss">
.sources-view {
  max-width: 1200px;
  margin: 0 auto;
}

.sources-header {
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

.path-input-row {
  display: flex;
  gap: 8px;
  width: 100%;

  .el-input {
    flex: 1;
  }
}

.path-hint {
  font-size: 12px;
  color: var(--accent-color);
  margin-top: 4px;
}
</style>
