<template>
  <div class="comic-card" @click="goToDetail">
    <div class="card-cover">
      <img
        v-if="coverUrl"
        :src="coverUrl"
        :alt="comic.title"
        loading="lazy"
        @error="onImageError"
      />
      <div v-else class="cover-placeholder">
        <el-icon :size="40"><Picture /></el-icon>
      </div>
      <div v-if="hasProgress" class="progress-overlay">
        <div class="progress-bar">
          <div class="progress-fill" :style="{ width: progressPercent + '%' }"></div>
        </div>
        <span class="progress-text">{{ comic.lastReadPage }}/{{ comic.pageCount }}</span>
      </div>
    </div>

    <div class="card-info">
      <h3 class="card-title" :title="comic.title">{{ truncatedTitle }}</h3>
      <p class="card-author" v-if="comic.author">{{ comic.author }}</p>
      <div class="card-rating" v-if="comic.rating">
        <StarRating :model-value="comic.rating" :readonly="true" :size="'small'" />
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Picture } from '@element-plus/icons-vue'
import { truncateText } from '../../utils/format'
import { getCover } from '../../api/comicApi'
import StarRating from '../common/StarRating.vue'

const props = defineProps({
  comic: {
    type: Object,
    required: true
  }
})

const router = useRouter()
const coverUrl = ref('')

const truncatedTitle = computed(() => truncateText(props.comic.title, 30))

const hasProgress = computed(() => {
  return props.comic.lastReadPage > 0 && props.comic.pageCount > 0
})

const progressPercent = computed(() => {
  if (!props.comic.pageCount) return 0
  return Math.round((props.comic.lastReadPage / props.comic.pageCount) * 100)
})

function goToDetail() {
  router.push(`/library/${props.comic.id}`)
}

function onImageError() {
  coverUrl.value = ''
}

onMounted(async () => {
  try {
    coverUrl.value = await getCover(props.comic.id)
  } catch (e) {
    coverUrl.value = ''
  }
})
</script>

<style scoped lang="scss">
.comic-card {
  cursor: pointer;
  border-radius: 8px;
  overflow: hidden;
  background-color: var(--card-bg);
  box-shadow: 0 2px 8px var(--shadow-color);
  transition: transform 0.2s ease, box-shadow 0.2s ease;

  &:hover {
    transform: translateY(-4px);
    box-shadow: 0 8px 24px var(--shadow-hover);
  }
}

.card-cover {
  position: relative;
  width: 100%;
  padding-top: 140%;
  overflow: hidden;
  background-color: var(--bg-secondary);

  img {
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    object-fit: cover;
  }
}

.cover-placeholder {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  display: flex;
  justify-content: center;
  align-items: center;
  color: var(--text-tertiary);
}

.progress-overlay {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  padding: 4px 8px;
  background: linear-gradient(transparent, rgba(0, 0, 0, 0.7));
}

.progress-bar {
  height: 3px;
  background-color: rgba(255, 255, 255, 0.3);
  border-radius: 2px;
  overflow: hidden;
  margin-bottom: 2px;
}

.progress-fill {
  height: 100%;
  background-color: #409eff;
  border-radius: 2px;
  transition: width 0.3s ease;
}

.progress-text {
  font-size: 11px;
  color: rgba(255, 255, 255, 0.9);
}

.card-info {
  padding: 10px;
}

.card-title {
  font-size: 14px;
  font-weight: 500;
  color: var(--text-primary);
  margin-bottom: 4px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.card-author {
  font-size: 12px;
  color: var(--text-tertiary);
  margin-bottom: 4px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.card-rating {
  display: flex;
  align-items: center;
}
</style>
