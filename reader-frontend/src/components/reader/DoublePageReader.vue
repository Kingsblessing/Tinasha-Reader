<template>
  <div
    class="double-page-reader"
    @mousemove="onMouseMove"
    @touchstart="onTouchStart"
    @touchend="onTouchEnd"
  >
    <!-- Loading spinner -->
    <div v-if="pageLoading" class="page-loading">
      <el-icon class="loading-spinner" :size="40"><Loading /></el-icon>
      <span>加载中...</span>
    </div>

    <!-- Two-page spread -->
    <div class="page-spread" :class="{ 'rtl-mode': isRtl }">
      <!-- Single remaining page (centered) -->
      <template v-if="isLastSingle">
        <img
          v-if="singlePageUrl"
          :src="singlePageUrl"
          :alt="`Page ${currentPage}`"
          class="page-image single"
          :class="{ loaded: !pageLoading }"
          @load="onImageLoad"
          @error="onImageError"
        />
      </template>

      <!-- Two-page spread -->
      <template v-else>
        <img
          v-if="leftPageUrl"
          :src="leftPageUrl"
          :alt="`Page ${leftPageNum}`"
          class="page-image left"
          :class="{ loaded: imagesLoaded.left }"
          @load="onLeftImageLoad"
          @error="onLeftImageError"
        />
        <div v-else class="page-placeholder left">
          <el-icon class="loading-spinner" :size="30"><Loading /></el-icon>
        </div>
        <img
          v-if="rightPageUrl"
          :src="rightPageUrl"
          :alt="`Page ${rightPageNum}`"
          class="page-image right"
          :class="{ loaded: imagesLoaded.right }"
          @load="onRightImageLoad"
          @error="onRightImageError"
        />
        <div v-else class="page-placeholder right">
          <el-icon class="loading-spinner" :size="30"><Loading /></el-icon>
        </div>
      </template>
    </div>

    <!-- Click areas for navigation -->
    <div class="nav-area nav-left" @click="prevPages"></div>
    <div class="nav-area nav-right" @click="nextPages"></div>
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted, onUnmounted } from 'vue'
import { Loading } from '@element-plus/icons-vue'
import { getPageImage } from '../../api/comicApi'
import { usePreload } from '../../composables/usePreload'
import { useReadingSettings } from '../../composables/useReadingSettings'

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

const { settings } = useReadingSettings()
const comicIdRef = computed(() => props.comicId)
const totalPagesRef = computed(() => props.pages.length)
const currentPageRef = computed(() => currentPage.value)
const readingModeRef = computed(() => 'double')

const { preloadAround, getCachedUrl, clearCache, evictDistantPages } = usePreload(
  comicIdRef,
  currentPageRef,
  totalPagesRef,
  readingModeRef
)

const currentPage = ref(props.initialPage)
const pageLoading = ref(true)
const leftPageUrl = ref('')
const rightPageUrl = ref('')
const singlePageUrl = ref('')
const imagesLoaded = ref({ left: false, right: false })

// Touch handling
let touchStartX = 0
let touchStartY = 0

const isRtl = computed(() => settings.value.readingDirection === 'rtl')

// In double-page mode, we display pages as pairs.
// The "current" page represents the right-hand page in LTR (odd page).
// For RTL, the right-hand page is the even page.
const isLastSingle = computed(() => {
  const total = props.pages.length
  if (total % 2 === 1) {
    // Odd total: last page is alone
    return currentPage.value === total
  }
  return false
})

const leftPageNum = computed(() => {
  const total = props.pages.length
  const cur = currentPage.value

  if (isRtl.value) {
    // RTL: right = even, left = odd
    // If current is odd, left = current; if even, left = current - 1
    return cur % 2 === 1 ? cur : cur - 1
  } else {
    // LTR: left = even, right = odd
    // If current is odd, left = current - 1; if even, left = current
    return cur % 2 === 1 ? cur - 1 : cur
  }
})

const rightPageNum = computed(() => {
  const total = props.pages.length
  const cur = currentPage.value

  if (isRtl.value) {
    // RTL: right = even, left = odd
    return cur % 2 === 0 ? cur : cur + 1
  } else {
    // LTR: left = even, right = odd
    return cur % 2 === 0 ? cur + 1 : cur
  }
})

async function loadPageImage(pageNum) {
  if (pageNum < 1 || pageNum > props.pages.length) return null
  const cached = getCachedUrl(pageNum)
  if (cached) return cached
  try {
    const url = await getPageImage(props.comicId, pageNum)
    return url
  } catch (e) {
    console.error('Failed to load page:', e)
    return null
  }
}

async function loadCurrentSpread() {
  pageLoading.value = true
  imagesLoaded.value = { left: false, right: false }
  leftPageUrl.value = ''
  rightPageUrl.value = ''
  singlePageUrl.value = ''

  if (isLastSingle.value) {
    // Only one page to show
    const url = await loadPageImage(currentPage.value)
    singlePageUrl.value = url || ''
    pageLoading.value = false
    return
  }

  const leftNum = leftPageNum.value
  const rightNum = rightPageNum.value

  const [leftUrl, rightUrl] = await Promise.all([
    loadPageImage(leftNum),
    loadPageImage(rightNum)
  ])

  leftPageUrl.value = leftUrl || ''
  rightPageUrl.value = rightUrl || ''

  // If both loaded immediately from cache, mark as loaded
  if (leftUrl && rightUrl) {
    imagesLoaded.value = { left: true, right: true }
    pageLoading.value = false
  } else if (leftUrl) {
    imagesLoaded.value.left = true
  } else if (rightUrl) {
    imagesLoaded.value.right = true
  }

  // If neither loaded (both null), stop loading
  if (!leftUrl && !rightUrl) {
    pageLoading.value = false
  }
}

function goToPage(pageNum) {
  const total = props.pages.length
  if (pageNum < 1 || pageNum > total) return
  currentPage.value = pageNum
  loadCurrentSpread()
  preloadAround(pageNum, total, 'double')
  evictDistantPages(pageNum, 'double')
  emit('page-change', pageNum)
}

function nextPages() {
  const total = props.pages.length
  const next = currentPage.value + 2
  if (next <= total) {
    goToPage(next)
  } else if (currentPage.value < total) {
    goToPage(total)
  }
}

function prevPages() {
  const prev = currentPage.value - 2
  if (prev >= 1) {
    goToPage(prev)
  } else if (currentPage.value > 1) {
    goToPage(1)
  }
}

function onImageLoad() {
  pageLoading.value = false
}

function onImageError() {
  pageLoading.value = false
}

function onLeftImageLoad() {
  imagesLoaded.value.left = true
  if (imagesLoaded.value.right) {
    pageLoading.value = false
  }
}

function onLeftImageError() {
  imagesLoaded.value.left = true
  if (imagesLoaded.value.right) {
    pageLoading.value = false
  }
}

function onRightImageLoad() {
  imagesLoaded.value.right = true
  if (imagesLoaded.value.left) {
    pageLoading.value = false
  }
}

function onRightImageError() {
  imagesLoaded.value.right = true
  if (imagesLoaded.value.left) {
    pageLoading.value = false
  }
}

// Keyboard navigation
function onKeyDown(e) {
  switch (e.key) {
    case 'ArrowLeft':
      prevPages()
      break
    case 'ArrowRight':
    case ' ':
      e.preventDefault()
      nextPages()
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
      nextPages()
    } else {
      prevPages()
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
  clearCache()
})
</script>

<style scoped lang="scss">
.double-page-reader {
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

.page-spread {
  display: flex;
  justify-content: center;
  align-items: center;
  width: 100%;
  height: 100%;
  gap: 2px;

  &.rtl-mode {
    flex-direction: row-reverse;
  }
}

.page-image {
  max-height: 100%;
  object-fit: contain;
  opacity: 0;
  transition: opacity 0.3s ease;
  flex: 1;
  min-width: 0;

  &.loaded {
    opacity: 1;
  }

  &.single {
    flex: 0 1 auto;
    max-width: 50%;
  }

  &.left,
  &.right {
    width: 50%;
  }
}

.page-placeholder {
  display: flex;
  justify-content: center;
  align-items: center;
  flex: 1;
  height: 100%;
  color: rgba(255, 255, 255, 0.5);

  &.left,
  &.right {
    width: 50%;
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
  z-index: 20;
}

.loading-spinner {
  animation: spin 1s linear infinite;
}

@keyframes spin {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
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
