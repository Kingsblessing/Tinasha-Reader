<template>
  <div class="history-view">
    <div class="history-header">
      <h2 class="page-title">阅读历史</h2>
      <div class="header-actions">
        <el-date-picker
          v-model="clearBeforeDate"
          type="date"
          placeholder="选择清除日期"
          format="YYYY-MM-DD"
          value-format="YYYY-MM-DD"
          size="default"
        />
        <el-button type="danger" @click="clearHistory" :disabled="!clearBeforeDate">
          清除历史
        </el-button>
      </div>
    </div>

    <!-- History list grouped by date -->
    <div v-if="groupedHistory.length > 0" class="history-groups">
      <div v-for="group in groupedHistory" :key="group.date" class="history-group">
        <h3 class="group-date">{{ group.date }}</h3>
        <div class="history-items">
          <div v-for="item in group.items" :key="item.id" class="history-item">
            <div class="item-cover" @click="goToComic(item.comicId)">
              <img
                v-if="item._coverUrl"
                :src="item._coverUrl"
                :alt="item._comicTitle"
                loading="lazy"
              />
              <div v-else class="cover-placeholder">&#128218;</div>
            </div>
            <div class="item-info">
              <h4 class="item-title" @click="goToComic(item.comicId)">
                {{ item._comicTitle || '未知漫画' }}
              </h4>
              <div class="item-meta">
                <span v-if="item.pagesRead">阅读了 {{ item.pagesRead }} 页</span>
                <span v-if="item.durationSec"> · {{ formatDuration(item.durationSec) }}</span>
              </div>
              <div class="item-time">{{ formatTime(item.startedAt) }}</div>
            </div>
            <div class="item-actions">
              <el-button type="primary" size="small" @click="continueReading(item.comicId)">
                继续阅读
              </el-button>
              <el-button text size="small" @click="deleteItem(item.id)">删除</el-button>
            </div>
          </div>
        </div>
      </div>
    </div>

    <div v-else-if="!loading" class="empty-state">
      <div class="empty-icon">&#128336;</div>
      <p>暂无阅读历史</p>
    </div>

    <div v-if="loading" class="loading-container">
      <div class="spinner"></div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import * as historyApi from '../api/historyApi'
import * as comicApi from '../api/comicApi'
import { formatDuration } from '../utils/format'

const router = useRouter()

const historyItems = ref([])
const loading = ref(true)
const clearBeforeDate = ref(null)

const groupedHistory = computed(() => {
  const groups = {}
  historyItems.value.forEach(item => {
    const d = item.startedAt ? new Date(item.startedAt) : new Date()
    const date = `${d.getFullYear()}-${String(d.getMonth()+1).padStart(2,'0')}-${String(d.getDate()).padStart(2,'0')}`
    if (!groups[date]) groups[date] = { date, items: [] }
    groups[date].items.push(item)
  })
  return Object.values(groups).sort((a, b) => b.date.localeCompare(a.date))
})

async function loadHistory() {
  loading.value = true
  try {
    const data = await historyApi.getAll({ size: 100 })
    console.log('[History] raw data:', data)
    const items = data?.content || data?.items || data || []
    console.log('[History] items count:', items.length)

    // Load comic info for each item (resilient - failures don't block)
    const comicCache = {}
    const results = await Promise.allSettled(
      items.map(async (item) => {
        const cid = item.comicId
        if (!comicCache[cid]) {
          try {
            comicCache[cid] = await comicApi.getById(cid)
          } catch (e) {
            comicCache[cid] = null
          }
        }
        const comic = comicCache[cid]
        item._comicTitle = comic?.title || '未知漫画 #' + cid
        try {
          item._coverUrl = comic ? await comicApi.getCover(cid) : ''
        } catch (e) {
          item._coverUrl = ''
        }
        return item
      })
    )

    historyItems.value = results
      .filter(r => r.status === 'fulfilled')
      .map(r => r.value)

    console.log('[History] loaded:', historyItems.value.length)
  } catch (e) {
    console.error('[History] load failed:', e)
    historyItems.value = []
  } finally {
    loading.value = false
  }
}

function formatTime(dateStr) {
  if (!dateStr) return ''
  const d = new Date(dateStr)
  return `${d.getHours().toString().padStart(2, '0')}:${d.getMinutes().toString().padStart(2, '0')}`
}

function goToComic(comicId) {
  router.push(`/library/${comicId}`)
}

function continueReading(comicId) {
  router.push(`/reader/${comicId}`)
}

async function deleteItem(id) {
  try {
    await ElMessageBox.confirm('确定删除此条记录？', '提示', { type: 'warning' })
    await historyApi.deleteHistory(id)
    historyItems.value = historyItems.value.filter(item => item.id !== id)
    ElMessage.success('已删除')
  } catch (e) {}
}

async function clearHistory() {
  if (!clearBeforeDate.value) return
  try {
    await ElMessageBox.confirm(
      `确定清除 ${clearBeforeDate.value} 之前的所有历史记录？`,
      '提示', { type: 'warning' }
    )
    await historyApi.clearOld(clearBeforeDate.value + 'T00:00:00')
    await loadHistory()
    ElMessage.success('历史记录已清除')
  } catch (e) {}
}

onMounted(loadHistory)
</script>

<style scoped lang="scss">
.history-view {
  max-width: 1000px;
  margin: 0 auto;
}

.history-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 24px;
  flex-wrap: wrap;
  gap: 12px;
}

.page-title {
  font-size: 24px;
  font-weight: 600;
  color: var(--text-primary);
}

.header-actions {
  display: flex;
  gap: 8px;
  align-items: center;
}

.history-groups {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.group-date {
  font-size: 13px;
  font-weight: 600;
  color: var(--text-tertiary);
  margin-bottom: 12px;
  padding-bottom: 8px;
  border-bottom: 1px solid var(--border-color);
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.history-items {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.history-item {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 12px;
  background-color: var(--card-bg);
  border-radius: 10px;
  box-shadow: 0 1px 3px var(--shadow-color);
  transition: transform 0.15s ease, box-shadow 0.15s ease;

  &:hover {
    transform: translateY(-1px);
    box-shadow: 0 4px 12px var(--shadow-hover);
  }
}

.item-cover {
  flex-shrink: 0;
  width: 52px;
  height: 70px;
  border-radius: 6px;
  overflow: hidden;
  cursor: pointer;
  background-color: var(--bg-secondary);

  img {
    width: 100%;
    height: 100%;
    object-fit: cover;
  }
}

.cover-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  justify-content: center;
  align-items: center;
  font-size: 24px;
}

.item-info {
  flex: 1;
  min-width: 0;
}

.item-title {
  font-size: 14px;
  font-weight: 500;
  color: var(--text-primary);
  margin-bottom: 4px;
  cursor: pointer;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;

  &:hover { color: var(--accent-color); }
}

.item-meta {
  font-size: 12px;
  color: var(--text-secondary);
  margin-bottom: 2px;
}

.item-time {
  font-size: 11px;
  color: var(--text-tertiary);
}

.item-actions {
  display: flex;
  gap: 6px;
  flex-shrink: 0;
}

.empty-state {
  text-align: center;
  padding: 60px 20px;
  color: var(--text-tertiary);
}

.empty-icon {
  font-size: 48px;
  margin-bottom: 12px;
}

.loading-container {
  display: flex;
  justify-content: center;
  padding: 40px;
}

.spinner {
  width: 32px;
  height: 32px;
  border: 3px solid var(--border-color);
  border-top-color: var(--accent-color);
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

@media (max-width: 768px) {
  .history-item {
    flex-wrap: wrap;
  }

  .item-actions {
    width: 100%;
    justify-content: flex-end;
    margin-top: 4px;
  }

  .header-actions {
    width: 100%;
    flex-wrap: wrap;
  }
}
</style>
