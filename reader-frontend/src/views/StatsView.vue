<template>
  <div class="stats-view">
    <h2 class="page-title">阅读统计</h2>

    <!-- Stats cards -->
    <div class="stats-cards">
      <div class="stat-card">
        <div class="stat-icon-wrap" style="color: #409eff;">&#128218;</div>
        <div class="stat-value">{{ stats.totalComics || 0 }}</div>
        <div class="stat-label">总漫画数</div>
      </div>
      <div class="stat-card">
        <div class="stat-icon-wrap" style="color: #67c23a;">&#128197;</div>
        <div class="stat-value">{{ stats.todayPages || 0 }}</div>
        <div class="stat-label">今日阅读</div>
      </div>
      <div class="stat-card">
        <div class="stat-icon-wrap" style="color: #e6a23c;">&#128198;</div>
        <div class="stat-value">{{ stats.weekPages || 0 }}</div>
        <div class="stat-label">本周阅读</div>
      </div>
      <div class="stat-card">
        <div class="stat-icon-wrap" style="color: #f56c6c;">&#9200;</div>
        <div class="stat-value">{{ formatDuration(stats.totalTimeSec || 0) }}</div>
        <div class="stat-label">总阅读时间</div>
      </div>
    </div>

    <!-- Daily reading chart -->
    <div class="chart-section">
      <h3>每日阅读页数</h3>
      <div class="bar-chart" v-if="dailyData.length > 0">
        <div v-for="day in dailyData" :key="day.date" class="bar-item">
          <div class="bar-wrapper">
            <div class="bar-fill" :style="{ height: barHeight(day.pages) + '%' }"></div>
          </div>
          <div class="bar-label">{{ day.label }}</div>
          <div class="bar-value" v-if="day.pages > 0">{{ day.pages }}</div>
        </div>
      </div>
      <div v-else class="chart-empty">暂无阅读数据</div>
    </div>

    <!-- Recent reads -->
    <div class="recent-section" v-if="recentComics.length > 0">
      <h3>最近阅读</h3>
      <div class="recent-grid">
        <div
          v-for="comic in recentComics"
          :key="comic.id"
          class="recent-item"
          @click="$router.push(`/library/${comic.id}`)"
        >
          <div class="recent-cover">
            <img v-if="comic._coverUrl" :src="comic._coverUrl" :alt="comic.title" loading="lazy" />
            <div v-else class="cover-ph">&#128218;</div>
          </div>
          <div class="recent-info">
            <div class="recent-title">{{ comic.title }}</div>
            <div class="recent-meta">{{ comic.lastReadPage || 0 }}/{{ comic.pageCount || 0 }} 页</div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import * as historyApi from '../api/historyApi'
import * as comicApi from '../api/comicApi'
import { formatDuration } from '../utils/format'

const stats = ref({
  totalComics: 0,
  todayPages: 0,
  weekPages: 0,
  monthPages: 0,
  totalTimeSec: 0
})

const dailyData = ref([])
const recentComics = ref([])

function barHeight(pages) {
  const max = Math.max(...dailyData.value.map(d => d.pages), 1)
  return Math.max((pages / max) * 100, pages > 0 ? 3 : 0)
}

async function loadStats() {
  try {
    const data = await historyApi.getStats()
    if (data) {
      stats.value = {
        totalComics: data.totalComics || 0,
        todayPages: data.todayPages || 0,
        weekPages: data.weekPages || 0,
        monthPages: data.monthPages || 0,
        totalTimeSec: data.totalTimeSec || 0
      }

      // Process daily stats from the same response
      const daily = data.dailyStats || []
      const dayNames = ['周日', '周一', '周二', '周三', '周四', '周五', '周六']
      dailyData.value = daily.map(d => {
        const date = new Date(d.date + 'T00:00:00')
        return {
          date: d.date,
          label: dayNames[date.getDay()] || d.date.slice(5),
          pages: d.pages || 0
        }
      }).reverse()
    }
  } catch (e) {
    console.error('Failed to load stats:', e)
  }
}

async function loadRecent() {
  try {
    const data = await historyApi.getRecent({ limit: 6 })
    const comics = Array.isArray(data) ? data : []
    for (const comic of comics) {
      try { comic._coverUrl = await comicApi.getCover(comic.id) }
      catch (e) { comic._coverUrl = '' }
    }
    recentComics.value = comics
  } catch (e) {
    recentComics.value = []
  }
}

onMounted(async () => {
  await Promise.all([loadStats(), loadRecent()])
})
</script>

<style scoped lang="scss">
.stats-view {
  max-width: 1000px;
  margin: 0 auto;
}

.page-title {
  font-size: 24px;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 24px;
}

.stats-cards {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
  gap: 14px;
  margin-bottom: 28px;
}

.stat-card {
  background-color: var(--card-bg);
  border-radius: 12px;
  padding: 20px;
  text-align: center;
  box-shadow: 0 1px 4px var(--shadow-color);
  transition: transform 0.15s ease;
  &:hover { transform: translateY(-2px); }
}

.stat-icon-wrap {
  font-size: 26px;
  margin-bottom: 6px;
}

.stat-value {
  font-size: 28px;
  font-weight: 700;
  color: var(--text-primary);
  margin-bottom: 2px;
}

.stat-label {
  font-size: 12px;
  color: var(--text-tertiary);
}

.chart-section {
  background-color: var(--card-bg);
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 1px 4px var(--shadow-color);
  margin-bottom: 28px;

  h3 {
    font-size: 15px;
    font-weight: 500;
    color: var(--text-primary);
    margin-bottom: 16px;
  }
}

.chart-empty {
  text-align: center;
  color: var(--text-tertiary);
  padding: 32px;
  font-size: 13px;
}

.bar-chart {
  display: flex;
  align-items: flex-end;
  height: 160px;
  gap: 2px;
  overflow-x: auto;
}

.bar-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  flex: 1;
  min-width: 16px;
}

.bar-wrapper {
  width: 100%;
  height: 120px;
  background-color: var(--bg-secondary);
  border-radius: 3px 3px 0 0;
  display: flex;
  align-items: flex-end;
  overflow: hidden;
}

.bar-fill {
  width: 100%;
  background: linear-gradient(to top, var(--accent-color), #66b1ff);
  border-radius: 3px 3px 0 0;
  transition: height 0.5s ease;
}

.bar-label {
  margin-top: 4px;
  font-size: 9px;
  color: var(--text-secondary);
}

.bar-value {
  font-size: 9px;
  color: var(--text-tertiary);
}

.recent-section {
  h3 {
    font-size: 15px;
    font-weight: 500;
    color: var(--text-primary);
    margin-bottom: 14px;
  }
}

.recent-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(140px, 1fr));
  gap: 14px;
}

.recent-item {
  cursor: pointer;
  border-radius: 8px;
  overflow: hidden;
  background-color: var(--card-bg);
  box-shadow: 0 1px 4px var(--shadow-color);
  transition: transform 0.15s ease;
  &:hover { transform: translateY(-2px); }
}

.recent-cover {
  width: 100%;
  aspect-ratio: 3 / 4;
  overflow: hidden;
  background-color: var(--bg-secondary);
  img { width: 100%; height: 100%; object-fit: cover; }
}

.cover-ph {
  width: 100%;
  height: 100%;
  display: flex;
  justify-content: center;
  align-items: center;
  font-size: 28px;
}

.recent-info { padding: 8px; }

.recent-title {
  font-size: 12px;
  font-weight: 500;
  color: var(--text-primary);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.recent-meta {
  font-size: 11px;
  color: var(--text-tertiary);
  margin-top: 2px;
}

@media (max-width: 768px) {
  .stats-cards { grid-template-columns: repeat(2, 1fr); }
  .recent-grid { grid-template-columns: repeat(2, 1fr); }
}
</style>
