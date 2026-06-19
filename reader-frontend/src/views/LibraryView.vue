<template>
  <div class="library-view">
    <!-- Top toolbar -->
    <div class="library-toolbar">
      <div class="toolbar-left">
        <h2 class="page-title">书架</h2>
      </div>
      <div class="toolbar-center">
        <SearchBar @search="onSearch" />
      </div>
      <div class="toolbar-right">
        <el-select v-model="sortBy" placeholder="排序" style="width: 130px;" @change="onFilterChange">
          <el-option
            v-for="(label, key) in SortOptionLabels"
            :key="key"
            :label="label"
            :value="key"
          />
        </el-select>
        <el-button-group class="view-toggle">
          <el-button
            :type="viewMode === 'grid' ? 'primary' : 'default'"
            :icon="Grid"
            @click="viewMode = 'grid'"
          />
          <el-button
            :type="viewMode === 'list' ? 'primary' : 'default'"
            :icon="List"
            @click="viewMode = 'list'"
          />
        </el-button-group>
      </div>
    </div>

    <!-- Filters section -->
    <div class="library-filters">
      <div class="filter-row">
        <el-select
          v-model="selectedStatus"
          placeholder="状态"
          clearable
          style="width: 130px;"
          @change="onFilterChange"
        >
          <el-option label="全部" :value="null" />
          <el-option label="连载中" value="ONGOING" />
          <el-option label="已完结" value="COMPLETED" />
          <el-option label="休刊" value="HIATUS" />
        </el-select>

        <el-select
          v-model="selectedSourceId"
          placeholder="来源"
          clearable
          style="width: 150px;"
          @change="onFilterChange"
        >
          <el-option
            v-for="source in sources"
            :key="source.id"
            :label="source.name"
            :value="source.id"
          />
        </el-select>

        <el-button
          text
          type="primary"
          @click="showTagFilter = !showTagFilter"
          class="tag-filter-toggle"
        >
          <el-icon><Filter /></el-icon>
          标签筛选
          <el-icon>
            <ArrowDown v-if="!showTagFilter" />
            <ArrowUp v-else />
          </el-icon>
        </el-button>

        <el-button
          v-if="hasActiveFilters"
          text
          type="danger"
          @click="clearAllFilters"
        >
          清除筛选
        </el-button>
      </div>

      <!-- Tag filter (collapsible) -->
      <div v-if="showTagFilter" class="tag-filter-section">
        <TagSelector v-model="selectedTagIds" @update:model-value="onTagFilterChange" />
      </div>
    </div>

    <!-- Active filter chips -->
    <div v-if="hasActiveFilters" class="filter-chips">
      <el-tag v-if="selectedStatus" closable @close="clearStatus" type="warning">
        {{ statusLabelMap[selectedStatus] }}
      </el-tag>
      <el-tag v-if="selectedSourceId" closable @close="clearSource" type="success">
        {{ currentSourceName }}
      </el-tag>
      <el-tag
        v-for="tagId in selectedTagIds"
        :key="tagId"
        closable
        @close="removeTag(tagId)"
        type="primary"
      >
        {{ getTagName(tagId) }}
      </el-tag>
    </div>

    <!-- Comic grid -->
    <ComicGrid :comics="comics" :loading="loading" />

    <!-- Pagination -->
    <div class="library-pagination" v-if="total > 0">
      <el-pagination
        v-model:current-page="currentPage"
        :page-size="pageSize"
        :total="total"
        layout="prev, pager, next, jumper, total"
        @current-change="onPageChange"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, inject } from 'vue'
import { Grid, List, Filter, ArrowDown, ArrowUp } from '@element-plus/icons-vue'
import ComicGrid from '../components/comic/ComicGrid.vue'
import SearchBar from '../components/common/SearchBar.vue'
import TagSelector from '../components/common/TagSelector.vue'
import * as comicApi from '../api/comicApi'
import * as sourceApi from '../api/sourceApi'
import * as tagApi from '../api/tagApi'
import { SortOptionLabels } from '../utils/constants'

// State
const comics = ref([])
const loading = ref(true)
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(20)
const sortBy = ref('lastRead')
const searchQuery = ref('')
const viewMode = ref('grid')

// Filter state
const selectedStatus = ref(null)
const selectedSourceId = ref(null)
const selectedTagIds = ref([])
const showTagFilter = ref(false)

// Reference data
const sources = ref([])
const allTags = ref([])

// Debounce
let searchDebounceTimer = null

const statusLabelMap = {
  ONGOING: '连载中',
  COMPLETED: '已完结',
  HIATUS: '休刊'
}

const currentSourceName = computed(() => {
  const source = sources.value.find((s) => s.id === selectedSourceId.value)
  return source ? source.name : ''
})

const hasActiveFilters = computed(() => {
  return selectedStatus.value || selectedSourceId.value || selectedTagIds.value.length > 0
})

function getTagName(tagId) {
  const tag = allTags.value.find((t) => t.id === tagId)
  return tag ? tag.name : tagId
}

// API calls
async function loadComics() {
  loading.value = true
  try {
    const params = {
      page: currentPage.value,
      size: pageSize.value,
      sort: sortBy.value
    }
    if (searchQuery.value) params.search = searchQuery.value
    if (selectedStatus.value) params.status = selectedStatus.value
    if (selectedSourceId.value) params.sourceId = selectedSourceId.value
    if (selectedTagIds.value.length > 0) params.tagIds = selectedTagIds.value.join(',')

    const data = await comicApi.getAll(params)
    if (data) {
      comics.value = data.content || data.items || data || []
      total.value = data.totalElements || data.total || comics.value.length
    }
  } catch (e) {
    console.error('Failed to load comics:', e)
    comics.value = []
  } finally {
    loading.value = false
  }
}

async function loadSources() {
  try {
    const data = await sourceApi.getAll()
    sources.value = Array.isArray(data) ? data : (data?.content || data?.items || [])
  } catch (e) {
    console.error('Failed to load sources:', e)
  }
}

async function loadTags() {
  try {
    const data = await tagApi.getAll()
    allTags.value = Array.isArray(data) ? data : (data?.content || data?.items || [])
  } catch (e) {
    console.error('Failed to load tags:', e)
  }
}

// Event handlers
function onSearch(query) {
  if (searchDebounceTimer) clearTimeout(searchDebounceTimer)
  searchDebounceTimer = setTimeout(() => {
    searchQuery.value = query
    currentPage.value = 1
    loadComics()
  }, 300)
}

function onFilterChange() {
  currentPage.value = 1
  loadComics()
}

function onTagFilterChange() {
  currentPage.value = 1
  loadComics()
}

function onPageChange(page) {
  currentPage.value = page
  loadComics()
}

function clearStatus() {
  selectedStatus.value = null
  onFilterChange()
}

function clearSource() {
  selectedSourceId.value = null
  onFilterChange()
}

function removeTag(tagId) {
  selectedTagIds.value = selectedTagIds.value.filter((id) => id !== tagId)
  onTagFilterChange()
}

function clearAllFilters() {
  selectedStatus.value = null
  selectedSourceId.value = null
  selectedTagIds.value = []
  onFilterChange()
}

// Sidebar filter integration
const registerSidebarFilter = inject('onSidebarFilter', null)
if (registerSidebarFilter) {
  registerSidebarFilter((filter) => {
    console.log('[Library] sidebar filter:', filter)
    if (filter.type === 'source') {
      selectedSourceId.value = filter.value
      currentPage.value = 1
      loadComics()
    } else if (filter.type === 'tag') {
      // Add tag to selected tags if not already present
      if (!selectedTagIds.value.includes(filter.value)) {
        selectedTagIds.value = [...selectedTagIds.value, filter.value]
      }
      showTagFilter.value = true
      currentPage.value = 1
      loadComics()
    } else if (filter.type === 'favorite-group') {
      // Could filter by favorite group - for now just show a message
      console.log('[Library] favorite group filter:', filter.value)
    }
  })
}

// Init
onMounted(() => {
  loadSources()
  loadTags()
  loadComics()
})
</script>

<style scoped lang="scss">
.library-view {
  max-width: 1400px;
  margin: 0 auto;
  padding: 0 16px;
}

.library-toolbar {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 16px;
  flex-wrap: wrap;
}

.toolbar-left {
  flex-shrink: 0;
}

.page-title {
  font-size: 24px;
  font-weight: 600;
  color: var(--text-primary);
  margin: 0;
}

.toolbar-center {
  flex: 1;
  max-width: 400px;
  min-width: 200px;
}

.toolbar-right {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-shrink: 0;
}

.view-toggle {
  margin-left: 4px;
}

.library-filters {
  margin-bottom: 12px;
}

.filter-row {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
  margin-bottom: 8px;
}

.tag-filter-toggle {
  font-size: 14px;
}

.tag-filter-section {
  margin-top: 12px;
  margin-bottom: 8px;
}

.filter-chips {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 16px;
}

.library-pagination {
  display: flex;
  justify-content: center;
  margin-top: 32px;
  padding-bottom: 20px;
}
</style>
