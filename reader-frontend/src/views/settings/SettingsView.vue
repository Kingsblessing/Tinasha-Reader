<template>
  <div class="settings-view">
    <h2 class="page-title">设置</h2>

    <el-tabs v-model="activeTab" tab-position="left" class="settings-tabs">
      <!-- General -->
      <el-tab-pane label="通用" name="general">
        <div class="settings-section">
          <h3>外观</h3>
          <div class="setting-item">
            <div class="setting-label">主题</div>
            <div class="setting-control">
              <el-radio-group v-model="settings.theme" @change="onThemeChange">
                <el-radio-button value="light">亮色</el-radio-button>
                <el-radio-button value="dark">暗色</el-radio-button>
              </el-radio-group>
            </div>
          </div>
          <div class="setting-item">
            <div class="setting-label">语言</div>
            <div class="setting-control">
              <el-select v-model="settings.language" @change="saveSetting('language', settings.language)">
                <el-option label="简体中文" value="zh-CN" />
                <el-option label="English" value="en-US" />
              </el-select>
            </div>
          </div>
        </div>
      </el-tab-pane>

      <!-- Reader -->
      <el-tab-pane label="阅读器" name="reader">
        <div class="settings-section">
          <h3>阅读设置</h3>
          <div class="setting-item">
            <div class="setting-label">默认阅读模式</div>
            <div class="setting-control">
              <el-select v-model="settings.readingMode" @change="saveSetting('readingMode', settings.readingMode)">
                <el-option v-for="(label, key) in ReadingModeLabels" :key="key" :label="label" :value="key" />
              </el-select>
            </div>
          </div>
          <div class="setting-item">
            <div class="setting-label">预加载页数</div>
            <div class="setting-control">
              <el-slider v-model="settings.preloadPages" :min="1" :max="10" :step="1" show-stops
                @change="saveSetting('preloadPages', settings.preloadPages)" />
            </div>
          </div>
          <div class="setting-item">
            <div class="setting-label">翻页动画</div>
            <div class="setting-control">
              <el-switch v-model="settings.animationEnabled"
                @change="saveSetting('animationEnabled', settings.animationEnabled)" />
            </div>
          </div>
          <div class="setting-item">
            <div class="setting-label">自动保存阅读进度</div>
            <div class="setting-control">
              <el-switch v-model="settings.autoSaveProgress"
                @change="saveSetting('autoSaveProgress', settings.autoSaveProgress)" />
            </div>
          </div>
        </div>
      </el-tab-pane>

      <!-- Shortcuts -->
      <el-tab-pane label="快捷键" name="shortcuts">
        <div class="settings-section">
          <h3>阅读器快捷键</h3>
          <div class="shortcuts-table">
            <div v-for="s in shortcuts" :key="s.key" class="shortcut-row">
              <div class="shortcut-keys">
                <kbd v-for="(k, i) in s.keys" :key="i">{{ k }}</kbd>
              </div>
              <div class="shortcut-action">{{ s.action }}</div>
            </div>
          </div>
        </div>
      </el-tab-pane>

      <!-- Sources (embedded) -->
      <el-tab-pane label="来源" name="sources">
        <div class="settings-section">
          <div class="section-header">
            <h3>来源管理</h3>
            <el-button type="primary" size="small" @click="openSourceAddDialog">
              <el-icon><Plus /></el-icon>
              添加来源
            </el-button>
          </div>

          <el-table :data="sources" v-loading="sourcesLoading" stripe>
            <el-table-column prop="name" label="名称" min-width="120" />
            <el-table-column prop="path" label="路径" min-width="200" show-overflow-tooltip />
            <el-table-column prop="type" label="类型" width="100">
              <template #default="{ row }">
                {{ SourceTypeLabels[row.type] || row.type }}
              </template>
            </el-table-column>
            <el-table-column prop="status" label="状态" width="100">
              <template #default="{ row }">
                <el-tag :type="sourceStatusType(row.status || row.scanStatus)" size="small">
                  {{ SourceStatusLabels[row.status || row.scanStatus] || row.status || row.scanStatus }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="comicCount" label="漫画数" width="80" align="center" />
            <el-table-column label="启用" width="80" align="center">
              <template #default="{ row }">
                <el-switch
                  :model-value="row.enabled"
                  @change="(val) => onSourceToggleEnabled(row, val)"
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
        </div>

        <!-- Add/Edit source dialog -->
        <el-dialog
          v-model="showSourceDialog"
          :title="editingSource ? '编辑来源' : '添加来源'"
          width="580px"
          @close="resetSourceForm"
        >
          <el-form :model="sourceForm" label-width="80px">
            <el-form-item label="名称" required>
              <el-input v-model="sourceForm.name" placeholder="输入来源名称" />
            </el-form-item>
            <el-form-item label="路径" required>
              <div class="path-input-row">
                <el-input v-model="sourceForm.path" placeholder="点击右侧按钮选择目录" readonly />
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
              <el-select v-model="sourceForm.type" style="width: 100%;">
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
            <el-button @click="showSourceDialog = false">取消</el-button>
            <el-button type="primary" @click="submitSourceForm" :loading="sourceSubmitting">
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
      </el-tab-pane>

      <!-- Tags (embedded) -->
      <el-tab-pane label="标签" name="tags">
        <div class="settings-section">
          <div class="section-header">
            <h3>标签管理</h3>
            <el-button type="primary" size="small" @click="openTagAddDialog">
              <el-icon><Plus /></el-icon>
              添加标签
            </el-button>
          </div>

          <div class="tags-content">
            <!-- Group filter sidebar -->
            <div class="group-filter">
              <div
                class="filter-item"
                :class="{ active: selectedTagGroup === null }"
                @click="selectedTagGroup = null"
              >
                全部
              </div>
              <div
                v-for="group in tagGroups"
                :key="group"
                class="filter-item"
                :class="{ active: selectedTagGroup === group }"
                @click="selectedTagGroup = group"
              >
                {{ group }}
              </div>
            </div>

            <!-- Tags table -->
            <div class="tags-table-wrapper">
              <el-table :data="filteredTags" v-loading="tagsLoading" stripe>
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
        </div>

        <!-- Add/Edit tag dialog -->
        <el-dialog
          v-model="showTagDialog"
          :title="editingTag ? '编辑标签' : '添加标签'"
          width="400px"
          @close="resetTagForm"
        >
          <el-form :model="tagForm" label-width="60px">
            <el-form-item label="名称" required>
              <el-input v-model="tagForm.name" placeholder="输入标签名称" />
            </el-form-item>
            <el-form-item label="分组">
              <el-select
                v-model="tagForm.group"
                filterable
                allow-create
                placeholder="选择或创建分组"
                style="width: 100%;"
              >
                <el-option
                  v-for="group in tagGroups"
                  :key="group"
                  :label="group"
                  :value="group"
                />
              </el-select>
            </el-form-item>
            <el-form-item label="颜色">
              <el-color-picker v-model="tagForm.color" />
            </el-form-item>
          </el-form>
          <template #footer>
            <el-button @click="showTagDialog = false">取消</el-button>
            <el-button type="primary" @click="submitTagForm" :loading="tagSubmitting">
              {{ editingTag ? '保存' : '添加' }}
            </el-button>
          </template>
        </el-dialog>
      </el-tab-pane>

      <!-- JM Import -->
      <el-tab-pane label="JM导入" name="jmImport">
        <div class="settings-section">
          <h3>JM漫画导入</h3>
          <p>输入车牌号（作品编号），从 JM 下载并导入漫画。</p>

          <div class="setting-item">
            <div class="setting-label">车牌号</div>
            <div class="setting-control">
              <el-input
                v-model="jmForm.plateNumber"
                placeholder="例如: 12345"
                style="width: 240px;"
                clearable
              />
            </div>
          </div>

          <div class="setting-item">
            <div class="setting-label">下载目录</div>
            <div class="setting-control">
              <div class="path-input-row">
                <el-input
                  v-model="jmForm.downloadDir"
                  placeholder="下载保存路径"
                  readonly
                  style="width: 240px;"
                />
                <el-button @click="showJmBrowser = true">
                  <el-icon><FolderOpened /></el-icon>
                  浏览
                </el-button>
              </div>
            </div>
          </div>

          <div class="setting-item">
            <div class="setting-label">图片格式</div>
            <div class="setting-control">
              <el-select v-model="jmForm.imageSuffix" style="width: 240px;" @change="saveJmSettings">
                <el-option label="JPG (.jpg)" value=".jpg" />
                <el-option label="PNG (.png)" value=".png" />
                <el-option label="WebP (.webp)" value=".webp" />
              </el-select>
            </div>
          </div>

          <div class="setting-item">
            <div class="setting-label">操作</div>
            <div class="setting-control">
              <el-button
                type="primary"
                @click="startJmImport"
                :loading="jmImporting"
                :disabled="!jmForm.plateNumber"
              >
                开始导入
              </el-button>
            </div>
          </div>

          <!-- Import status -->
          <div v-if="jmStatus" class="jm-status-section">
            <h3>导入状态</h3>
            <el-alert
              :type="jmStatusType"
              :title="jmStatusTitle"
              :description="jmStatusDesc"
              show-icon
              :closable="false"
            />
            <div v-if="jmResult" class="jm-result">
              <el-descriptions :column="1" border size="small">
                <el-descriptions-item label="标题">{{ jmResult.title }}</el-descriptions-item>
                <el-descriptions-item label="作者">{{ jmResult.author || '未知' }}</el-descriptions-item>
                <el-descriptions-item label="页数">{{ jmResult.pageCount || '未知' }}</el-descriptions-item>
                <el-descriptions-item label="保存路径">{{ jmResult.savePath || jmForm.downloadDir }}</el-descriptions-item>
              </el-descriptions>
            </div>
          </div>
        </div>

        <!-- Directory browser for JM download dir -->
        <DirectoryBrowser
          v-model="showJmBrowser"
          :multiple="false"
          @confirm="onJmPathSelected"
        />
      </el-tab-pane>

      <!-- Storage & Backup -->
      <el-tab-pane label="存储与备份" name="storage">
        <div class="settings-section">
          <h3>缓存管理</h3>
          <div class="setting-item">
            <div class="setting-label">清除图片缓存</div>
            <div class="setting-control">
              <el-button @click="clearCache">清除缓存</el-button>
            </div>
          </div>
        </div>

        <div class="settings-section">
          <h3>数据备份</h3>
          <p>导出所有漫画数据（来源、标签、收藏、评论、阅读历史、设置）为 JSON 文件</p>
          <div class="setting-item">
            <div class="setting-label">备份信息</div>
            <div class="setting-control">
              <span v-if="backupInfo" class="backup-info">
                {{ backupInfo.comics || 0 }} 部漫画 · {{ backupInfo.tags || 0 }} 个标签
              </span>
              <el-button @click="loadBackupInfo" size="small" :loading="infoLoading">刷新</el-button>
            </div>
          </div>
          <div class="backup-actions">
            <el-button type="primary" @click="exportBackup" :loading="exportLoading">
              导出备份
            </el-button>
            <el-upload
              ref="uploadRef"
              :auto-upload="false"
              :show-file-list="false"
              accept=".json"
              :on-change="onFileSelected"
            >
              <el-button :loading="importLoading">导入备份</el-button>
            </el-upload>
          </div>
        </div>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { Plus, FolderOpened } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useTheme } from '../../composables/useTheme'
import { useSettingStore } from '../../stores/settingStore'
import { ReadingModeLabels, SourceTypeLabels, SourceStatusLabels, DEFAULT_SETTINGS } from '../../utils/constants'
import * as backupApi from '../../api/backupApi'
import * as sourceApi from '../../api/sourceApi'
import * as tagApi from '../../api/tagApi'
import request from '../../api/request'
import DirectoryBrowser from '../../components/common/DirectoryBrowser.vue'

const { toggleTheme, currentTheme } = useTheme()
const { state: settingState, loadSettings, saveSetting: storeSaveSetting } = useSettingStore()

const activeTab = ref('general')

const shortcuts = [
  { keys: ['←', '→'], action: '上一页 / 下一页' },
  { keys: ['Space'], action: '下一页' },
  { keys: ['Home'], action: '第一页' },
  { keys: ['End'], action: '最后一页' },
  { keys: ['F'], action: '全屏切换' },
  { keys: ['D'], action: '切换阅读模式' },
  { keys: ['R'], action: '切换阅读方向' },
  { keys: ['P'], action: '自动播放开关' },
  { keys: ['[', ']'], action: '减速 / 加速播放' },
  { keys: ['?'], action: '显示快捷键帮助' },
  { keys: ['Esc'], action: '返回 / 关闭面板' },
]

const backupInfo = ref(null)
const infoLoading = ref(false)
const exportLoading = ref(false)
const importLoading = ref(false)
const uploadRef = ref(null)

const settings = reactive({
  theme: currentTheme.value,
  language: DEFAULT_SETTINGS.language,
  readingMode: DEFAULT_SETTINGS.readingMode,
  preloadPages: DEFAULT_SETTINGS.preloadPages,
  animationEnabled: DEFAULT_SETTINGS.animationEnabled,
  autoSaveProgress: DEFAULT_SETTINGS.autoSaveProgress
})

async function saveSetting(key, value) {
  await storeSaveSetting(key, value)
  ElMessage.success('设置已保存')
}

function onThemeChange(theme) {
  if (theme !== currentTheme.value) {
    toggleTheme()
  }
  saveSetting('theme', theme)
}

// ==================== Sources ====================
const sources = ref([])
const sourcesLoading = ref(true)
const showSourceDialog = ref(false)
const showBrowser = ref(false)
const editingSource = ref(null)
const sourceSubmitting = ref(false)
const pendingPaths = ref([])

const sourceForm = reactive({
  name: '',
  path: '',
  type: 'FOLDER'
})

function sourceStatusType(status) {
  switch (status) {
    case 'IDLE': return 'success'
    case 'SCANNING': return 'warning'
    case 'ERROR': return 'danger'
    default: return 'info'
  }
}

async function loadSources() {
  sourcesLoading.value = true
  try {
    sources.value = await sourceApi.getAll() || []
  } catch (e) {
    sources.value = []
  } finally {
    sourcesLoading.value = false
  }
}

function openSourceAddDialog() {
  editingSource.value = null
  resetSourceForm()
  showSourceDialog.value = true
}

function editSource(source) {
  editingSource.value = source
  sourceForm.name = source.name
  sourceForm.path = source.path
  sourceForm.type = source.type || 'FOLDER'
  pendingPaths.value = []
  showSourceDialog.value = true
}

function resetSourceForm() {
  editingSource.value = null
  sourceForm.name = ''
  sourceForm.path = ''
  sourceForm.type = 'FOLDER'
  pendingPaths.value = []
}

function onPathsSelected(paths) {
  if (paths.length === 0) return
  pendingPaths.value = paths
  sourceForm.path = paths[0]
  if (!sourceForm.name) {
    const parts = paths[0].replace(/\\/g, '/').split('/')
    sourceForm.name = parts[parts.length - 1] || paths[0]
  }
}

async function submitSourceForm() {
  if (!sourceForm.path) {
    ElMessage.warning('请选择路径')
    return
  }

  sourceSubmitting.value = true
  try {
    if (editingSource.value) {
      if (!sourceForm.name) {
        ElMessage.warning('请填写名称')
        return
      }
      await sourceApi.update(editingSource.value.id, { ...sourceForm })
      ElMessage.success('来源已更新')
    } else {
      const paths = pendingPaths.value.length > 0 ? pendingPaths.value : [sourceForm.path]
      let added = 0
      for (const p of paths) {
        const name = paths.length === 1 && sourceForm.name
          ? sourceForm.name
          : p.replace(/\\/g, '/').split('/').filter(Boolean).pop() || p
        try {
          await sourceApi.create({ name, path: p, type: sourceForm.type })
          added++
        } catch (e) {
          console.error('Failed to add source:', p, e)
        }
      }
      ElMessage.success(`成功添加 ${added} 个来源`)
    }
    showSourceDialog.value = false
    await loadSources()
  } catch (e) {
    ElMessage.error('操作失败')
  } finally {
    sourceSubmitting.value = false
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

async function onSourceToggleEnabled(source, enabled) {
  try {
    await sourceApi.toggleEnabled(source.id)
    source.enabled = enabled
    ElMessage.success(enabled ? '已启用' : '已禁用')
  } catch (e) {
    ElMessage.error('操作失败')
  }
}

// ==================== Tags ====================
const tags = ref([])
const tagGroups = ref([])
const tagsLoading = ref(true)
const selectedTagGroup = ref(null)
const showTagDialog = ref(false)
const editingTag = ref(null)
const tagSubmitting = ref(false)

const tagForm = reactive({
  name: '',
  group: '',
  color: '#409eff'
})

const filteredTags = computed(() => {
  if (!selectedTagGroup.value) return tags.value
  return tags.value.filter(t => t.group === selectedTagGroup.value)
})

async function loadTags() {
  tagsLoading.value = true
  try {
    tags.value = await tagApi.getAll() || []
    const groupSet = new Set(tags.value.map(t => t.group).filter(Boolean))
    tagGroups.value = Array.from(groupSet).sort()
  } catch (e) {
    tags.value = []
    tagGroups.value = []
  } finally {
    tagsLoading.value = false
  }
}

function openTagAddDialog() {
  editingTag.value = null
  resetTagForm()
  showTagDialog.value = true
}

function editTag(tag) {
  editingTag.value = tag
  tagForm.name = tag.name
  tagForm.group = tag.group || ''
  tagForm.color = tag.color || '#409eff'
  showTagDialog.value = true
}

function resetTagForm() {
  editingTag.value = null
  tagForm.name = ''
  tagForm.group = ''
  tagForm.color = '#409eff'
}

async function submitTagForm() {
  if (!tagForm.name) {
    ElMessage.warning('请输入标签名称')
    return
  }

  tagSubmitting.value = true
  try {
    if (editingTag.value) {
      await tagApi.update(editingTag.value.id, { ...tagForm })
      ElMessage.success('标签已更新')
    } else {
      await tagApi.create({ ...tagForm })
      ElMessage.success('标签已添加')
    }
    showTagDialog.value = false
    await loadTags()
  } catch (e) {
    ElMessage.error('操作失败')
  } finally {
    tagSubmitting.value = false
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

// ==================== JM Import ====================
const jmForm = reactive({
  plateNumber: '',
  downloadDir: '',
  imageSuffix: '.jpg'
})
const jmImporting = ref(false)
const jmStatus = ref('')
const jmStatusType = ref('info')
const jmStatusTitle = ref('')
const jmStatusDesc = ref('')
const jmResult = ref(null)
const showJmBrowser = ref(false)

function onJmPathSelected(paths) {
  if (paths.length > 0) {
    jmForm.downloadDir = paths[0]
    saveJmSettings()
  }
}

async function loadJmStatus() {
  try {
    const res = await request.get('/jm/status')
    if (res) {
      if (res.downloadDir) jmForm.downloadDir = res.downloadDir
      if (res.imageSuffix) jmForm.imageSuffix = res.imageSuffix
    }
  } catch (e) {
    console.error('Failed to load JM status:', e)
  }
}

async function saveJmSettings() {
  try {
    await request.put('/jm/settings', {
      downloadDir: jmForm.downloadDir,
      imageSuffix: jmForm.imageSuffix
    })
  } catch (e) {
    console.error('Failed to save JM settings:', e)
  }
}

async function startJmImport() {
  if (!jmForm.plateNumber) {
    ElMessage.warning('请输入车牌号')
    return
  }

  jmImporting.value = true
  jmStatus.value = 'importing'
  jmStatusType.value = 'info'
  jmStatusTitle.value = '正在导入...'
  jmStatusDesc.value = `正在下载车牌号 ${jmForm.plateNumber} 的漫画，请稍候。`
  jmResult.value = null

  try {
    const res = await request.post('/jm/import', {
      plateNumber: jmForm.plateNumber,
      downloadDir: jmForm.downloadDir || undefined
    })
    jmStatus.value = 'success'
    jmStatusType.value = 'success'
    jmStatusTitle.value = '导入成功'
    jmStatusDesc.value = '漫画已成功下载并导入到库中。'
    jmResult.value = res || null
  } catch (e) {
    jmStatus.value = 'error'
    jmStatusType.value = 'error'
    jmStatusTitle.value = '导入失败'
    jmStatusDesc.value = e?.response?.data?.message || e.message || '导入过程中发生错误，请检查车牌号是否正确。'
  } finally {
    jmImporting.value = false
  }
}

// ==================== Storage & Backup ====================
function clearCache() {
  if ('caches' in window) {
    caches.keys().then(names => {
      names.forEach(name => caches.delete(name))
    })
  }
  ElMessage.success('缓存已清除')
}

async function loadBackupInfo() {
  infoLoading.value = true
  try {
    backupInfo.value = await backupApi.getInfo()
  } catch (e) {
    backupInfo.value = null
  } finally {
    infoLoading.value = false
  }
}

async function exportBackup() {
  exportLoading.value = true
  try {
    const data = await backupApi.exportData()
    const json = JSON.stringify(data, null, 2)
    const blob = new Blob([json], { type: 'application/json' })
    const url = URL.createObjectURL(blob)
    const a = document.createElement('a')
    const now = new Date()
    const dateStr = `${now.getFullYear()}${String(now.getMonth()+1).padStart(2,'0')}${String(now.getDate()).padStart(2,'0')}`
    a.href = url
    a.download = `reader-backup-${dateStr}.json`
    a.click()
    URL.revokeObjectURL(url)
    ElMessage.success('备份已导出')
  } catch (e) {
    ElMessage.error('导出失败')
  } finally {
    exportLoading.value = false
  }
}

async function onFileSelected(file) {
  if (!file || !file.raw) return

  try {
    await ElMessageBox.confirm(
      '导入将覆盖现有数据，确定继续？',
      '导入备份',
      { type: 'warning', confirmButtonText: '确定导入', cancelButtonText: '取消' }
    )
  } catch {
    return
  }

  importLoading.value = true
  try {
    const text = await file.raw.text()
    const data = JSON.parse(text)
    await backupApi.importData(data)
    ElMessage.success('备份已导入，请刷新页面')
  } catch (e) {
    ElMessage.error('导入失败: ' + (e.message || '格式错误'))
  } finally {
    importLoading.value = false
  }
}

// ==================== Init ====================
onMounted(async () => {
  await loadSettings()
  Object.keys(settings).forEach(key => {
    if (settingState.settings[key] !== undefined) {
      settings[key] = settingState.settings[key]
    }
  })
  // Load sources and tags when their tabs are first viewed
})

// Lazy-load data when switching tabs
watch(activeTab, (tab) => {
  if (tab === 'sources' && sources.value.length === 0 && sourcesLoading.value) {
    loadSources()
  }
  if (tab === 'tags' && tags.value.length === 0 && tagsLoading.value) {
    loadTags()
  }
}, { immediate: false })

// Eagerly load sources, tags, and JM status
onMounted(() => {
  loadSources()
  loadTags()
  loadJmStatus()
})
</script>

<style scoped lang="scss">
.settings-view {
  max-width: 1200px;
  margin: 0 auto;
}

.page-title {
  font-size: 24px;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 24px;
}

.settings-tabs {
  min-height: 500px;
}

.settings-section {
  padding: 0 20px;

  h3 {
    font-size: 16px;
    font-weight: 500;
    color: var(--text-primary);
    margin-bottom: 20px;
    padding-bottom: 12px;
    border-bottom: 1px solid var(--border-color);
  }

  p {
    color: var(--text-secondary);
    margin-bottom: 16px;
    font-size: 13px;
  }
}

.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20px;
  padding-bottom: 12px;
  border-bottom: 1px solid var(--border-color);

  h3 {
    margin-bottom: 0;
    padding-bottom: 0;
    border-bottom: none;
  }
}

.setting-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 0;
  border-bottom: 1px solid var(--border-color);

  &:last-child {
    border-bottom: none;
  }
}

.setting-label {
  font-size: 14px;
  color: var(--text-primary);
}

.setting-control {
  min-width: 200px;
  display: flex;
  justify-content: flex-end;
  align-items: center;
  gap: 8px;
}

.backup-info {
  font-size: 13px;
  color: var(--text-secondary);
}

.shortcuts-table {
  display: flex;
  flex-direction: column;
  gap: 0;
}

.shortcut-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 10px 0;
  border-bottom: 1px solid var(--border-color);

  &:last-child { border-bottom: none; }
}

.shortcut-keys {
  display: flex;
  gap: 4px;

  kbd {
    display: inline-block;
    padding: 2px 10px;
    background-color: var(--bg-tertiary);
    border: 1px solid var(--border-color);
    border-radius: 4px;
    font-family: 'SF Mono', 'Menlo', 'Monaco', monospace;
    font-size: 12px;
    color: var(--text-primary);
    min-width: 28px;
    text-align: center;
  }
}

.shortcut-action {
  font-size: 13px;
  color: var(--text-secondary);
}

.backup-actions {
  display: flex;
  gap: 12px;
  margin-top: 16px;
}

// Sources
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

// Tags
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

// JM Import
.jm-status-section {
  margin-top: 24px;
}

.jm-result {
  margin-top: 16px;
}

@media (max-width: 768px) {
  .settings-tabs {
    :deep(.el-tabs__header) {
      .el-tabs__nav-wrap {
        overflow-x: auto;
      }
    }
  }

  .setting-item {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }

  .setting-control {
    min-width: unset;
    width: 100%;
    justify-content: flex-start;
  }

  .settings-section {
    padding: 0 8px;
  }

  .backup-actions {
    flex-direction: column;
  }

  .section-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }

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

  .path-input-row {
    flex-direction: column;
  }
}
</style>
