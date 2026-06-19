<template>
  <div
    ref="containerRef"
    class="scroll-reader"
    @scroll="onScroll"
  >
    <div class="scroll-content">
      <div
        v-for="pageNum in pages.length"
        :key="pageNum"
        :ref="(el) => setPageRef(pageNum, el)"
        class="page-wrapper"
        :data-page="pageNum"
      >
        <!-- Loaded image -->
        <img
          v-if="isPageLoaded(pageNum)"
          :src="getPageUrl(pageNum)"
          :alt="`Page ${pageNum}`"
          class="page-image"
          :class="{ loaded: imagesLoaded[pageNum] }"
          @load="onImageLoad(pageNum)"
          @error="onImageError(pageNum)"
        />

        <!-- Loading state -->
        <div v-else-if="isPageNearViewport(pageNum)" class="page-loading">
          <el-icon class="loading-spinner" :size="30"><Loading /></el-icon>
        </div>

        <!-- Placeholder for distant pages -->
        <div v-else class="page-placeholder"></div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, watch, onMounted, onUnmounted, nextTick } from 'vue'
import { Loading } from '@element-plus/icons-vue'
import { getPageImage } from '../../api/comicApi'
import { usePreload } from '../../composables/usePreload'

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

const containerRef = ref(null)
const pageRefs = reactive({})
const imagesLoaded = reactive({})
const pageUrls = reactive({})
const currentPage = ref(props.initialPage)
const loadedPages = new Set()

const viewportPages = reactive(new Set())
const LOAD_RANGE = 5

const comicIdRef = ref(props.comicId)
const totalPagesRef = ref(props.pages.length)
const currentPageRef = computed(() => currentPage.value)
const readingModeRef = ref('scroll')

const { getCachedUrl, preloadAround, clearCache, evictDistantPages } = usePreload(
  comicIdRef,
  currentPageRef,
  totalPagesRef,
  readingModeRef
)

function setPageRef(pageNum, el) {
  if (el) {
    pageRefs[pageNum] = el
  }
}

function isPageLoaded(pageNum) {
  return !!pageUrls[pageNum]
}

function getPageUrl(pageNum) {
  return pageUrls[pageNum] || ''
}

function isPageNearViewport(pageNum) {
  return viewportPages.has(pageNum)
}

async function loadPage(pageNum) {
  if (loadedPages.has(pageNum) || pageNum < 1 || pageNum > props.pages.length) return

  loadedPages.add(pageNum)

  const cached = getCachedUrl(pageNum)
  if (cached) {
    pageUrls[pageNum] = cached
    return
  }

  try {
    const url = await getPageImage(props.comicId, pageNum)
    pageUrls[pageNum] = url
  } catch (e) {
    console.error(`Failed to load page ${pageNum}:`, e)
    loadedPages.delete(pageNum)
  }
}

function loadPagesNearViewport(visiblePage) {
  const start = Math.max(1, visiblePage - LOAD_RANGE)
  const end = Math.min(props.pages.length, visiblePage + LOAD_RANGE)

  viewportPages.clear()
  for (let i = start; i <= end; i++) {
    viewportPages.add(i)
  }

  for (let i = start; i <= end; i++) {
    loadPage(i)
  }

  preloadAround(visiblePage, props.pages.length, 'scroll')
  evictDistantPages(visiblePage, 'scroll')
}

function onImageLoad(pageNum) {
  imagesLoaded[pageNum] = true
}

function onImageError(pageNum) {
  imagesLoaded[pageNum] = true
}

let observer = null

function setupIntersectionObserver() {
  if (!containerRef.value) return

  observer = new IntersectionObserver(
    (entries) => {
      let maxRatio = 0
      let mostVisiblePage = null

      for (const entry of entries) {
        const pageNum = parseInt(entry.target.dataset.page, 10)
        if (entry.intersectionRatio > maxRatio) {
          maxRatio = entry.intersectionRatio
          mostVisiblePage = pageNum
        }
      }

      if (mostVisiblePage !== null && maxRatio >= 0.5 && mostVisiblePage !== currentPage.value) {
        currentPage.value = mostVisiblePage
        emit('page-change', mostVisiblePage)
        loadPagesNearViewport(mostVisiblePage)
      }
    },
    {
      root: containerRef.value,
      threshold: [0, 0.25, 0.5, 0.75, 1]
    }
  )

  for (let pageNum = 1; pageNum <= props.pages.length; pageNum++) {
    if (pageRefs[pageNum]) {
      observer.observe(pageRefs[pageNum])
    }
  }
}

function onScroll() {}

function scrollToPage(pageNum) {
  const el = pageRefs[pageNum]
  if (el && containerRef.value) {
    el.scrollIntoView({ behavior: 'smooth', block: 'start' })
  }
}

watch(() => props.initialPage, (newPage) => {
  if (newPage !== currentPage.value) {
    scrollToPage(newPage)
  }
})

onMounted(async () => {
  loadPagesNearViewport(props.initialPage)

  await nextTick()
  setupIntersectionObserver()

  if (props.initialPage > 1) {
    setTimeout(() => {
      scrollToPage(props.initialPage)
    }, 100)
  }
})

onUnmounted(() => {
  if (observer) {
    observer.disconnect()
    observer = null
  }
  clearCache()
})

defineExpose({
  scrollToPage
})
</script>

<style scoped lang="scss">
.scroll-reader {
  width: 100%;
  height: 100%;
  overflow-y: auto;
  background-color: #000;
  scroll-behavior: smooth;
  -webkit-overflow-scrolling: touch;
}

.scroll-content {
  display: flex;
  flex-direction: column;
  gap: 4px;
  padding-bottom: 20px;
}

.page-wrapper {
  width: 100%;
  display: flex;
  justify-content: center;
  align-items: flex-start;
  min-height: 200px;
}

.page-image {
  width: 100%;
  max-width: 800px;
  height: auto;
  display: block;
  opacity: 0;
  transition: opacity 0.3s ease;

  &.loaded {
    opacity: 1;
  }
}

.page-loading {
  width: 100%;
  max-width: 800px;
  aspect-ratio: 3 / 4;
  display: flex;
  justify-content: center;
  align-items: center;
  color: rgba(255, 255, 255, 0.5);
  background-color: rgba(255, 255, 255, 0.03);
  border-radius: 4px;
}

.page-placeholder {
  width: 100%;
  max-width: 800px;
  aspect-ratio: 3 / 4;
  background-color: rgba(255, 255, 255, 0.03);
  border-radius: 4px;
}

.loading-spinner {
  animation: spin 1s linear infinite;
}

@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}
</style>
