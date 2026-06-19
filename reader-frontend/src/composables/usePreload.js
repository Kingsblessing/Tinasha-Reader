import { ref, watch } from 'vue'
import { getPageImage } from '../api/comicApi'

/**
 * Composable for intelligent page preloading.
 * Preloads pages around the current page based on reading mode.
 */
export function usePreload(comicId, currentPage, totalPages, readingMode) {
  const preloadedPages = ref(new Map())
  const preloadQueue = ref([])
  const isPreloading = ref(false)

  // Configurable preload counts per mode
  const PRELOAD_CONFIG = {
    single: { before: 2, after: 3 },
    double: { before: 2, after: 4 },
    scroll: { before: 1, after: 8 }
  }

  function getPreloadRange(current, total, mode) {
    const config = PRELOAD_CONFIG[mode] || PRELOAD_CONFIG.single
    const start = Math.max(1, current - config.before)
    const end = Math.min(total, current + config.after)
    return { start, end }
  }

  async function preloadPage(pageNum) {
    if (preloadedPages.value.has(pageNum)) return

    try {
      const url = await getPageImage(comicId.value || comicId, pageNum)
      preloadedPages.value.set(pageNum, url)
    } catch (e) {
      // Silently fail - page will load on demand
    }
  }

  async function preloadAround(current, total, mode) {
    const { start, end } = getPreloadRange(current, total, mode)

    // Build queue of pages to preload (prioritize forward)
    const queue = []
    for (let i = current + 1; i <= end; i++) {
      if (!preloadedPages.value.has(i)) {
        queue.push(i)
      }
    }
    for (let i = current - 1; i >= start; i--) {
      if (!preloadedPages.value.has(i)) {
        queue.push(i)
      }
    }

    // Process queue with concurrency limit
    const CONCURRENT = 2
    const promises = queue.slice(0, CONCURRENT).map(p => preloadPage(p))
    await Promise.allSettled(promises)

    // Continue with remaining pages in background
    for (let i = CONCURRENT; i < queue.length; i++) {
      preloadPage(queue[i])
    }
  }

  function getCachedUrl(pageNum) {
    return preloadedPages.value.get(pageNum) || null
  }

  function isPageCached(pageNum) {
    return preloadedPages.value.has(pageNum)
  }

  function clearCache() {
    preloadedPages.value.forEach(url => {
      if (url && url.startsWith('blob:')) {
        URL.revokeObjectURL(url)
      }
    })
    preloadedPages.value.clear()
  }

  function evictDistantPages(current, mode) {
    const config = PRELOAD_CONFIG[mode] || PRELOAD_CONFIG.single
    const keepStart = Math.max(1, current - config.before - 2)
    const keepEnd = Math.min(totalPages.value || totalPages, current + config.after + 2)

    preloadedPages.value.forEach((url, pageNum) => {
      if (pageNum < keepStart || pageNum > keepEnd) {
        if (url && url.startsWith('blob:')) {
          URL.revokeObjectURL(url)
        }
        preloadedPages.value.delete(pageNum)
      }
    })
  }

  return {
    preloadedPages,
    preloadAround,
    getCachedUrl,
    isPageCached,
    clearCache,
    evictDistantPages
  }
}
