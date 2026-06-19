<template>
  <el-aside :width="collapsed ? '0px' : '220px'" class="app-sidebar">
    <div class="sidebar-content">
      <el-scrollbar>
        <!-- Tag Groups -->
        <div class="sidebar-section">
          <div class="section-header" @click="toggleSection('tags')">
            <el-icon><PriceTag /></el-icon>
            <span>标签</span>
            <el-icon class="arrow" :class="{ expanded: expandedSections.tags }">
              <ArrowDown />
            </el-icon>
          </div>
          <el-collapse-transition>
            <div v-show="expandedSections.tags" class="section-body">
              <div
                v-for="tag in tags"
                :key="tag.id"
                class="group-item"
                @click="$emit('filter-select', { type: 'tag', value: tag.id, label: tag.name })"
              >
                <span class="group-name">{{ tag.name }}</span>
                <span class="count-text" v-if="tag.comicCount">{{ tag.comicCount }}</span>
              </div>
              <div v-if="tags.length === 0" class="empty-hint">暂无标签</div>
            </div>
          </el-collapse-transition>
        </div>

        <!-- Favorite Groups -->
        <div class="sidebar-section">
          <div class="section-header" @click="toggleSection('favorites')">
            <el-icon><Star /></el-icon>
            <span>收藏夹</span>
            <el-icon class="arrow" :class="{ expanded: expandedSections.favorites }">
              <ArrowDown />
            </el-icon>
          </div>
          <el-collapse-transition>
            <div v-show="expandedSections.favorites" class="section-body">
              <div
                v-for="group in favoriteGroups"
                :key="group"
                class="group-item"
                @click="$emit('filter-select', { type: 'favorite-group', value: group, label: group })"
              >
                <span class="group-name">{{ group }}</span>
              </div>
              <div v-if="favoriteGroups.length === 0" class="empty-hint">暂无收藏夹</div>
            </div>
          </el-collapse-transition>
        </div>

        <!-- Sources -->
        <div class="sidebar-section">
          <div class="section-header" @click="toggleSection('sources')">
            <el-icon><FolderOpened /></el-icon>
            <span>来源</span>
            <el-icon class="arrow" :class="{ expanded: expandedSections.sources }">
              <ArrowDown />
            </el-icon>
          </div>
          <el-collapse-transition>
            <div v-show="expandedSections.sources" class="section-body">
              <div
                v-for="source in sources"
                :key="source.id"
                class="group-item"
                @click="$emit('filter-select', { type: 'source', value: source.id, label: source.name })"
              >
                <span class="group-name">{{ source.name }}</span>
                <span class="count-text" v-if="source.comicCount">{{ source.comicCount }}</span>
              </div>
              <div v-if="sources.length === 0" class="empty-hint">暂无来源</div>
            </div>
          </el-collapse-transition>
        </div>
      </el-scrollbar>
    </div>
  </el-aside>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { PriceTag, Star, FolderOpened, ArrowDown } from '@element-plus/icons-vue'
import * as tagApi from '../../api/tagApi'
import * as favoriteApi from '../../api/favoriteApi'
import * as sourceApi from '../../api/sourceApi'

defineProps({
  collapsed: { type: Boolean, default: false }
})

defineEmits(['filter-select'])

const expandedSections = ref({ tags: true, favorites: true, sources: true })
const tags = ref([])
const favoriteGroups = ref([])
const sources = ref([])

function toggleSection(section) {
  expandedSections.value[section] = !expandedSections.value[section]
}

async function loadData() {
  try {
    const [tagList, favGroups, srcList] = await Promise.all([
      tagApi.getAll().catch(() => []),
      favoriteApi.getGroupNames().catch(() => []),
      sourceApi.getAll().catch(() => [])
    ])
    tags.value = Array.isArray(tagList) ? tagList : []
    favoriteGroups.value = Array.isArray(favGroups) ? favGroups : []
    sources.value = Array.isArray(srcList) ? srcList : []
  } catch (e) {
    console.error('Failed to load sidebar data:', e)
  }
}

onMounted(loadData)
defineExpose({ loadData })
</script>

<style scoped lang="scss">
.app-sidebar {
  background-color: var(--sidebar-bg);
  border-right: 1px solid var(--border-color);
  overflow: hidden;
  transition: width 0.3s ease;
}

.sidebar-content {
  height: 100%;
  padding: 8px 0;
}

.sidebar-section {
  margin-bottom: 2px;
}

.section-header {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 14px;
  cursor: pointer;
  color: var(--text-tertiary);
  font-size: 11px;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  user-select: none;
  transition: color 0.2s;

  &:hover {
    color: var(--text-secondary);
  }

  .arrow {
    margin-left: auto;
    transition: transform 0.3s ease;
    &.expanded { transform: rotate(180deg); }
  }
}

.section-body {
  padding: 0 6px;
}

.group-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 7px 12px;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.15s ease;

  &:hover {
    background-color: var(--accent-light);
    color: var(--accent-color);
  }
}

.group-name {
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  font-size: 13px;
  color: var(--text-secondary);
}

.count-text {
  font-size: 11px;
  color: var(--text-tertiary);
  opacity: 0.6;
  min-width: 16px;
  text-align: right;
}

.empty-hint {
  padding: 8px 12px;
  color: var(--text-tertiary);
  font-size: 12px;
  text-align: center;
  opacity: 0.6;
}
</style>
