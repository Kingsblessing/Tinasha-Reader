<template>
  <div
    class="single-page-reader"
    @mousemove="onMouseMove"
    @touchstart="onTouchStart"
    @touchend="onTouchEnd"
  >
    <!-- Loading spinner -->
    <div v-if="pageLoading" class="page-loading">
      <el-icon class="loading-spinner" :size="40"><Loading /></el-icon>
      <span>加载中...</span>
    </div>

    <!-- Page image -->
    <img
      v-if="currentPageUrl"
      :src="currentPageUrl"
      :alt="`Page ${currentPage}`"
      class="page-image"
      :class="{ loaded: !pageLoading }"
      @load="onImageLoad"
      @error="onImageError"
    />

    <!-- Click areas for navigation -->
    <div class="nav-area nav-left" @click="prevPage"></div>
    <div class="nav-area nav-right" @click="nextPage"></div>
  </div>
</template>

<script setup>
import { ref, watch, onMounted, onUnmounted } from 'vue'
import { Loading } from '@element-plus/icons-vue'
import { getPageImage } from '../../api/comicApi'

const props = defineProps({
  pages: {
    type: Array,
    default: () => []
  },
  comicId: {
    type: [Number, String],
    required: true
  },
  initialPage: {
    type: Number,
    default: 1
  }
})

const emit = defineEmits(['page-change'])

const currentPage = ref(props.initialPage)
const currentPageUrl = ref('')
const pageLoading = ref(true)
const pageCache = new Map()

// Touch handling
let touchStartX = 0
let touchStartY = 0

async function loadPage(pageNum) {
  if (pageNum < 1 || pageNum > props.pages.length) return

  pageLoading.value = true

  // Check cache
  if (pageCache.has(pageNum)) {
    currentPageUrl.value = pageCache.get(pageNum)
    pageLoading.value = false
    return
  }

  try {
    const url = await getPageImage(props.comicId, pageNum)
    pageCache.set(pageNum, url)
    currentPageUrl.value = url
  } catch (e) {
    console.error('Failed to load page:', e)
    currentPageUrl.value = ''
  } finally {
    pageLoading.value = false
  }
}

function preloadPages(pageNum) {
  const preloadRange = 3
  for (let i = pageNum - preloadRange; i <= pageNum + preloadRange; i++) {
    if (i >= 1 && i <= props.pages.length && !pageCache.has(i)) {
      getPageImage(props.comicId, i)
        .then(url => pageCache.set(i, url))
        .catch(() => {})
    }
  }
}

function goToPage(pageNum) {
  if (pageNum < 1 || pageNum > props.pages.length) return
  currentPage.value = pageNum
  loadPage(pageNum)
  preloadPages(pageNum)
  emit('page-change', pageNum)
}

function nextPage() {
  goToPage(currentPage.value + 1)
}

function prevPage() {
  goToPage(currentPage.value - 1)
}

function onImageLoad() {
  pageLoading.value = false
}

function onImageError() {
  pageLoading.value = false
}

// Keyboard navigation
function onKeyDown(e) {
  switch (e.key) {
    case 'ArrowLeft':
      prevPage()
      break
    case 'ArrowRight':
    case ' ':
      e.preventDefault()
      nextPage()
      break
  }
}

// Touch navigation
function onTouchStart(e) {
  touchStartX = e.touches[0].clientX
  touchStartY = e.touches[0].clientY
}

function onTouchEnd(e) {
  const touchEndX = e.changedTouches[0].clientX
  const touchEndY = e.changedTouches[0].clientY
  const diffX = touchStartX - touchEndX
  const diffY = touchStartY - touchEndY

  // Only handle horizontal swipes
  if (Math.abs(diffX) > Math.abs(diffY) && Math.abs(diffX) > 50) {
    if (diffX > 0) {
      nextPage()
    } else {
      prevPage()
    }
  }
}

function onMouseMove() {
  // Handled by parent for toolbar visibility
}

watch(() => props.initialPage, (newPage) => {
  if (newPage !== currentPage.value) {
    goToPage(newPage)
  }
})

onMounted(() => {
  goToPage(props.initialPage)
  document.addEventListener('keydown', onKeyDown)
})

onUnmounted(() => {
  document.removeEventListener('keydown', onKeyDown)
  // Revoke all blob URLs
  pageCache.forEach(url => {
    if (url.startsWith('blob:')) {
      URL.revokeObjectURL(url)
    }
  })
  pageCache.clear()
})
</script>

<style scoped lang="scss">
.single-page-reader {
  position: relative;
  width: 100%;
  height: 100%;
  display: flex;
  justify-content: center;
  align-items: center;
  background-color: #000;
  overflow: hidden;
  user-select: none;
}

.page-image {
  max-width: 100%;
  max-height: 100%;
  object-fit: contain;
  opacity: 0;
  transition: opacity 0.3s ease;

  &.loaded {
    opacity: 1;
  }
}

.page-loading {
  position: absolute;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
  color: #fff;
  font-size: 14px;
}

.loading-spinner {
  animation: spin 1s linear infinite;
}

@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

.nav-area {
  position: absolute;
  top: 0;
  bottom: 0;
  cursor: pointer;
  z-index: 10;

  &:hover {
    background: linear-gradient(to right, rgba(255, 255, 255, 0.05), transparent);
  }
}

.nav-left {
  left: 0;
  width: 30%;
}

.nav-right {
  right: 0;
  width: 30%;

  &:hover {
    background: linear-gradient(to left, rgba(255, 255, 255, 0.05), transparent);
  }
}
</style>
