<template>
  <div class="favorites-view">
    <h2 class="page-title">我的收藏</h2>

    <div class="favorites-content">
      <!-- Group tabs -->
      <div class="group-tabs">
        <div
          class="group-tab"
          :class="{ active: selectedGroup === null }"
          @click="selectGroup(null)"
        >
          全部
        </div>
        <div
          v-for="group in groups"
          :key="group"
          class="group-tab"
          :class="{ active: selectedGroup === group }"
          @click="selectGroup(group)"
        >
          {{ group }}
        </div>
      </div>

      <!-- Comics grid -->
      <div class="favorites-grid-wrapper">
        <div v-if="loading" class="fav-grid">
          <div v-for="n in 8" :key="n" class="fav-card-skeleton">
            <el-skeleton :loading="true" animated>
              <template #template>
                <el-skeleton-item variant="image" style="width: 100%; aspect-ratio: 3/4;" />
                <div style="padding: 8px;">
                  <el-skeleton-item variant="text" style="width: 80%; height: 14px;" />
                </div>
              </template>
            </el-skeleton>
          </div>
        </div>

        <div v-else-if="favoriteComics.length > 0" class="fav-grid">
          <div
            v-for="comic in favoriteComics"
            :key="comic.id"
            class="fav-card"
            @click="$router.push(`/library/${comic.id}`)"
          >
            <div class="fav-cover">
              <img
                v-if="comic._coverUrl"
                :src="comic._coverUrl"
                :alt="comic.title"
                loading="lazy"
              />
              <div v-else class="fav-cover-ph">&#128218;</div>
              <!-- Remove button -->
              <button
                class="fav-remove-btn"
                @click.stop="removeFavorite(comic)"
                title="取消收藏"
              >
                &#10005;
              </button>
            </div>
            <div class="fav-info">
              <div class="fav-title">{{ comic.title }}</div>
              <div class="fav-meta" v-if="comic.pageCount">{{ comic.pageCount }} 页</div>
            </div>
          </div>
        </div>

        <EmptyState v-else message="暂无收藏漫画" />

        <div class="favorites-pagination" v-if="total > pageSize">
          <el-pagination
            v-model:current-page="currentPage"
            :page-size="pageSize"
            :total="total"
            layout="prev, pager, next"
            @current-change="loadFavorites"
          />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import EmptyState from '../components/common/EmptyState.vue'
import * as favoriteApi from '../api/favoriteApi'
import * as comicApi from '../api/comicApi'

const favoriteComics = ref([])
const groups = ref([])
const loading = ref(true)
const selectedGroup = ref(null)
const currentPage = ref(1)
const pageSize = ref(20)
const total = ref(0)

// Map comicId -> favoriteId for deletion
const comicFavMap = ref({})

async function loadGroups() {
  try {
    const data = await favoriteApi.getGroupNames()
    groups.value = Array.isArray(data) ? data : []
  } catch (e) {
    groups.value = []
  }
}

async function loadFavorites() {
  loading.value = true
  try {
    const params = selectedGroup.value ? { groupName: selectedGroup.value } : {}
    const favs = await favoriteApi.getAll(params)
    const favList = Array.isArray(favs) ? favs : []

    total.value = favList.length

    // Build comicId -> favoriteId map
    const favMap = {}
    favList.forEach(f => { favMap[f.comicId] = f.id })
    comicFavMap.value = favMap

    // Paginate locally
    const start = (currentPage.value - 1) * pageSize.value
    const paged = favList.slice(start, start + pageSize.value)

    // Load comic data for each favorite
    const comics = await Promise.all(
      paged.map(async (fav) => {
        try {
          const comic = await comicApi.getById(fav.comicId)
          if (comic) {
            comic._favoriteId = fav.id
            try {
              comic._coverUrl = await comicApi.getCover(comic.id)
            } catch (e) {
              comic._coverUrl = ''
            }
          }
          return comic
        } catch (e) {
          return { id: fav.comicId, title: '未知漫画', _favoriteId: fav.id, _coverUrl: '' }
        }
      })
    )
    favoriteComics.value = comics.filter(Boolean)
  } catch (e) {
    favoriteComics.value = []
  } finally {
    loading.value = false
  }
}

async function removeFavorite(comic) {
  try {
    await ElMessageBox.confirm(
      `确定取消收藏「${comic.title}」？`,
      '取消收藏',
      { type: 'warning', confirmButtonText: '确定', cancelButtonText: '取消' }
    )
    const favId = comic._favoriteId || comicFavMap.value[comic.id]
    if (favId) {
      await favoriteApi.deleteById(favId)
      ElMessage.success('已取消收藏')
      await loadFavorites()
    }
  } catch (e) {
    // User cancelled
  }
}

function selectGroup(groupName) {
  selectedGroup.value = groupName
  currentPage.value = 1
  loadFavorites()
}

onMounted(async () => {
  await Promise.all([loadGroups(), loadFavorites()])
})
</script>

<style scoped lang="scss">
.favorites-view {
  max-width: 1400px;
  margin: 0 auto;
}

.page-title {
  font-size: 24px;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 24px;
}

.favorites-content {
  display: flex;
  gap: 24px;
}

.group-tabs {
  width: 160px;
  flex-shrink: 0;
}

.group-tab {
  padding: 8px 14px;
  border-radius: 6px;
  cursor: pointer;
  font-size: 13px;
  color: var(--text-secondary);
  transition: all 0.15s;
  margin-bottom: 2px;

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

.favorites-grid-wrapper {
  flex: 1;
  min-width: 0;
}

.fav-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(140px, 1fr));
  gap: 14px;
}

.fav-card {
  cursor: pointer;
  border-radius: 8px;
  overflow: hidden;
  background-color: var(--card-bg);
  box-shadow: 0 1px 4px var(--shadow-color);
  transition: transform 0.15s ease, box-shadow 0.15s ease;

  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 4px 12px var(--shadow-hover);

    .fav-remove-btn {
      opacity: 1;
    }
  }
}

.fav-cover {
  position: relative;
  width: 100%;
  aspect-ratio: 3 / 4;
  overflow: hidden;
  background-color: var(--bg-secondary);

  img {
    width: 100%;
    height: 100%;
    object-fit: cover;
  }
}

.fav-cover-ph {
  width: 100%;
  height: 100%;
  display: flex;
  justify-content: center;
  align-items: center;
  font-size: 32px;
  color: var(--text-tertiary);
}

.fav-remove-btn {
  position: absolute;
  top: 6px;
  right: 6px;
  width: 24px;
  height: 24px;
  border-radius: 50%;
  background: rgba(0, 0, 0, 0.6);
  color: #fff;
  border: none;
  cursor: pointer;
  font-size: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: opacity 0.15s, background-color 0.15s;
  z-index: 2;

  &:hover {
    background: #f56c6c;
  }
}

.fav-info {
  padding: 8px;
}

.fav-title {
  font-size: 12px;
  font-weight: 500;
  color: var(--text-primary);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.fav-meta {
  font-size: 11px;
  color: var(--text-tertiary);
  margin-top: 2px;
}

.favorites-pagination {
  display: flex;
  justify-content: center;
  margin-top: 28px;
}

.fav-card-skeleton {
  border-radius: 8px;
  overflow: hidden;
  background-color: var(--card-bg);
}

@media (max-width: 768px) {
  .favorites-content {
    flex-direction: column;
  }

  .group-tabs {
    width: 100%;
    display: flex;
    overflow-x: auto;
    gap: 6px;
    padding-bottom: 6px;
  }

  .group-tab {
    white-space: nowrap;
    margin-bottom: 0;
    font-size: 12px;
    padding: 6px 10px;
  }

  .fav-grid {
    grid-template-columns: repeat(auto-fill, minmax(100px, 1fr));
    gap: 10px;
  }

  .fav-title { font-size: 11px; }
}
</style>
