<template>
  <div class="comic-grid-wrapper">
    <!-- Loading skeleton -->
    <div v-if="loading" class="comic-grid">
      <div v-for="n in 8" :key="n" class="skeleton-card">
        <el-skeleton :loading="true" animated>
          <template #template>
            <el-skeleton-item variant="image" style="width: 100%; height: 240px;" />
            <div style="padding: 10px;">
              <el-skeleton-item variant="h3" style="width: 80%; height: 16px; margin-bottom: 8px;" />
              <el-skeleton-item variant="text" style="width: 50%; height: 14px;" />
            </div>
          </template>
        </el-skeleton>
      </div>
    </div>

    <!-- Comic grid -->
    <div v-else-if="comics.length > 0" class="comic-grid">
      <ComicCard
        v-for="comic in comics"
        :key="comic.id"
        :comic="comic"
      />
    </div>

    <!-- Empty state -->
    <EmptyState
      v-else
      message="暂无漫画"
      icon="Collection"
    />
  </div>
</template>

<script setup>
import ComicCard from './ComicCard.vue'
import EmptyState from '../common/EmptyState.vue'

defineProps({
  comics: {
    type: Array,
    default: () => []
  },
  loading: {
    type: Boolean,
    default: false
  }
})
</script>

<style scoped lang="scss">
.comic-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
}

@media (min-width: 640px) {
  .comic-grid {
    grid-template-columns: repeat(3, 1fr);
  }
}

@media (min-width: 1024px) {
  .comic-grid {
    grid-template-columns: repeat(4, 1fr);
  }
}

@media (min-width: 1440px) {
  .comic-grid {
    grid-template-columns: repeat(5, 1fr);
  }
}

.skeleton-card {
  border-radius: 8px;
  overflow: hidden;
  background-color: var(--card-bg);
}
</style>
