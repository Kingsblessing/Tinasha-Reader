<template>
  <div class="reader-view" ref="readerContainer" @mousemove="onMouseMove">

    <!-- Top Toolbar (auto-hides) -->
    <transition name="toolbar-fade">
      <div v-if="showToolbar && pages.length > 0" class="top-toolbar">
        <button class="tb-btn" @click="goBack">&#8592; 返回</button>
        <span class="tb-title">{{ comic?.title || '' }}</span>
        <span class="tb-page">{{ currentPage }} / {{ totalPages }}</span>
        <button class="tb-btn" :class="{ active: currentMode === 'single' }" @click="onModeChange('single')">单页</button>
        <button class="tb-btn" :class="{ active: currentMode === 'double' }" @click="onModeChange('double')">双页</button>
        <button class="tb-btn" :class="{ active: currentMode === 'scroll' }" @click="onModeChange('scroll')">滚动</button>
        <button class="tb-btn" :class="{ active: autoPlaying }" @click="toggleAutoPlay">
          {{ autoPlaying ? '&#9646;&#9646;' : '&#9654;' }}
        </button>
        <button class="tb-btn" @click="toggleFullscreen">&#9974;</button>
        <button class="tb-btn" @click="showSettings = !showSettings">&#9881;</button>
      </div>
    </transition>

    <!-- Single Page Reader -->
    <SinglePageReader
      v-if="pages.length > 0 && currentMode === 'single'"
      :pages="pages"
      :comic-id="comicId"
      :initial-page="currentPage"
      @page-change="onPageChange"
    />

    <!-- Double Page Reader -->
    <DoublePageReader
      v-else-if="pages.length > 0 && currentMode === 'double'"
      :pages="pages"
      :comic-id="comicId"
      :initial-page="currentPage"
      @page-change="onPageChange"
    />

    <!-- Scroll Reader -->
    <ScrollReader
      v-else-if="pages.length > 0 && currentMode === 'scroll'"
      :pages="pages"
      :comic-id="comicId"
      :initial-page="currentPage"
      @page-change="onPageChange"
    />

    <!-- Error state -->
    <div v-else-if="loadError" class="reader-loading">
      <p style="font-size: 48px; margin: 0;">&#9888;</p>
      <p>加载失败</p>
      <p style="font-size: 12px; color: #999;">{{ errorMsg }}</p>
      <button class="back-btn" @click="goBack">返回</button>
    </div>

    <!-- Loading -->
    <div v-else class="reader-loading">
      <div class="spinner"></div>
      <p>加载漫画中...</p>
    </div>

    <!-- Bottom Progress Bar (always visible when pages loaded) -->
    <div v-if="pages.length > 0" class="bottom-bar" :class="{ show: true }">
      <span class="bb-page">{{ currentPage }}</span>
      <input
        type="range"
        class="bb-slider"
        :min="1"
        :max="totalPages"
        :value="currentPage"
        @input="onSliderInput($event)"
        @change="onSliderChange($event)"
      />
      <span class="bb-page">{{ totalPages }}</span>
      <div class="bb-input-group">
        <input
          type="number"
          class="bb-input"
          :min="1"
          :max="totalPages"
          :value="currentPage"
          @change="onPageInput($event)"
        />
        <span class="bb-label">页</span>
      </div>
    </div>

    <!-- Auto-play indicator -->
    <div v-if="autoPlaying" class="autoplay-indicator">
      <span class="autoplay-dot"></span>
      {{ autoPlayInterval }}s
    </div>

    <!-- Settings Panel -->
    <transition name="slide-right">
      <div v-if="showSettings" class="settings-panel">
        <div class="settings-header">
          <h3>阅读设置</h3>
          <button class="close-btn" @click="showSettings = false">&times;</button>
        </div>
        <div class="settings-body">
          <div class="setting-item">
            <span class="setting-label">阅读模式</span>
            <div class="mode-btns">
              <button :class="{ active: currentMode === 'single' }" @click="onModeChange('single')">单页</button>
              <button :class="{ active: currentMode === 'double' }" @click="onModeChange('double')">双页</button>
              <button :class="{ active: currentMode === 'scroll' }" @click="onModeChange('scroll')">滚动</button>
            </div>
          </div>
          <div class="setting-item">
            <span class="setting-label">阅读方向</span>
            <button class="toggle-btn" @click="readingSettings.toggleDirection()">
              {{ readingSettings.settings.value.readingDirection === 'ltr' ? '左→右' : '右→左' }}
            </button>
          </div>
          <div class="setting-item">
            <span class="setting-label">自动播放间隔</span>
            <div class="interval-control">
              <button @click="adjustInterval(-1)" :disabled="autoPlayInterval <= 2">-</button>
              <span>{{ autoPlayInterval }}秒</span>
              <button @click="adjustInterval(1)" :disabled="autoPlayInterval >= 30">+</button>
            </div>
          </div>
        </div>
      </div>
    </transition>

    <!-- Shortcuts Help -->
    <transition name="fade">
      <div v-if="showShortcuts" class="shortcuts-overlay" @click.self="showShortcuts = false">
        <div class="shortcuts-dialog">
          <div class="shortcuts-header">
            <h3>快捷键</h3>
            <button class="close-btn" @click="showShortcuts = false">&times;</button>
          </div>
          <div class="shortcuts-body">
            <div class="shortcut-row"><kbd>&larr;</kbd><kbd>&rarr;</kbd><span>翻页</span></div>
            <div class="shortcut-row"><kbd>Space</kbd><span>下一页</span></div>
            <div class="shortcut-row"><kbd>F</kbd><span>全屏</span></div>
            <div class="shortcut-row"><kbd>D</kbd><span>切换模式</span></div>
            <div class="shortcut-row"><kbd>P</kbd><span>自动播放</span></div>
            <div class="shortcut-row"><kbd>?</kbd><span>快捷键</span></div>
            <div class="shortcut-row"><kbd>Esc</kbd><span>返回</span></div>
          </div>
        </div>
      </div>
    </transition>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import SinglePageReader from '../components/reader/SinglePageReader.vue'
import DoublePageReader from '../components/reader/DoublePageReader.vue'
import ScrollReader from '../components/reader/ScrollReader.vue'
import * as comicApi from '../api/comicApi'
import * as historyApi from '../api/historyApi'
import { useReadingSettings } from '../composables/useReadingSettings'

const route = useRoute()
const router = useRouter()
const readingSettings = useReadingSettings()

const comicId = computed(() => parseInt(route.params.id))
const readerContainer = ref(null)
const comic = ref(null)
const pages = ref([])
const currentPage = ref(1)
const totalPages = ref(0)
const showToolbar = ref(true)
const showSettings = ref(false)
const showShortcuts = ref(false)
const loadError = ref(false)
const errorMsg = ref('')
const currentMode = ref(readingSettings.settings.value.readingMode || 'single')

const autoPlaying = ref(false)
const autoPlayInterval = ref(5)
let autoPlayTimer = null

let toolbarTimer = null
let saveProgressTimer = null
let readStartTime = null

async function loadComic() {
  try {
    const data = await comicApi.getById(route.params.id)
    console.log('[Reader] comic:', data)
    comic.value = data
    if (data) currentPage.value = data.lastReadPage || 1
  } catch (e) {
    console.error('[Reader] loadComic error:', e)
    errorMsg.value = '漫画加载失败: ' + (e.message || e)
    loadError.value = true
  }
}

async function loadPages() {
  try {
    const data = await comicApi.getPages(route.params.id)
    console.log('[Reader] pages raw:', data)
    const list = Array.isArray(data) ? data : (data?.content || data?.pages || [])
    pages.value = list
    totalPages.value = list.length
    console.log('[Reader] pages count:', list.length)
    if (list.length === 0) {
      errorMsg.value = '没有页面数据'
      loadError.value = true
    }
  } catch (e) {
    console.error('[Reader] loadPages error:', e)
    pages.value = []
    errorMsg.value = '页面加载失败: ' + (e.message || e)
    loadError.value = true
  }
}

function onPageChange(page) {
  if (page < 1 || page > totalPages.value) return
  console.log('[Reader] page change:', page)
  currentPage.value = page
  saveProgress(page)
  if (autoPlaying.value) resetAutoPlayTimer()
}

function onModeChange(mode) {
  console.log('[Reader] MODE CHANGE from', currentMode.value, 'to', mode)
  currentMode.value = mode
  readingSettings.setReadingMode(mode)
}

// Slider handlers - native range input
function onSliderInput(e) {
  const val = parseInt(e.target.value)
  if (val >= 1 && val <= totalPages.value) {
    currentPage.value = val
  }
}

function onSliderChange(e) {
  const val = parseInt(e.target.value)
  console.log('[Reader] slider change:', val)
  onPageChange(val)
}

function onPageInput(e) {
  const val = parseInt(e.target.value)
  if (val >= 1 && val <= totalPages.value) {
    onPageChange(val)
  }
}

// Auto-play
function toggleAutoPlay() {
  autoPlaying.value = !autoPlaying.value
  if (autoPlaying.value) resetAutoPlayTimer()
  else stopAutoPlay()
}

function resetAutoPlayTimer() {
  stopAutoPlay()
  autoPlayTimer = setInterval(() => {
    if (currentPage.value < totalPages.value) onPageChange(currentPage.value + 1)
    else { autoPlaying.value = false; stopAutoPlay() }
  }, autoPlayInterval.value * 1000)
}

function stopAutoPlay() {
  if (autoPlayTimer) { clearInterval(autoPlayTimer); autoPlayTimer = null }
}

function adjustInterval(delta) {
  const v = autoPlayInterval.value + delta
  if (v >= 2 && v <= 30) { autoPlayInterval.value = v; if (autoPlaying.value) resetAutoPlayTimer() }
}

function resetToolbarTimer() {
  if (toolbarTimer) clearTimeout(toolbarTimer)
  showToolbar.value = true
  toolbarTimer = setTimeout(() => { showToolbar.value = false }, 3000)
}

function onMouseMove() { resetToolbarTimer() }

function toggleFullscreen() {
  if (!document.fullscreenElement) readerContainer.value?.requestFullscreen()
  else document.exitFullscreen()
}

function goBack() { router.push(`/library/${route.params.id}`) }

function saveProgress(page) {
  if (saveProgressTimer) clearTimeout(saveProgressTimer)
  saveProgressTimer = setTimeout(async () => {
    try { await comicApi.updateProgress(route.params.id, page) }
    catch (e) { console.error('[Reader] save progress error:', e) }
  }, 1000)
}

async function recordHistory() {
  if (!readStartTime) return
  const duration = Math.floor((Date.now() - readStartTime) / 1000)
  if (duration < 3) return
  const cid = parseInt(route.params.id)
  const start = (comic.value && comic.value.lastReadPage) ? comic.value.lastReadPage : 1
  const end = currentPage.value || 1
  console.log('[Reader] recording history:', { comicId: cid, startPage: start, endPage: end, durationSec: duration })
  try {
    const result = await historyApi.record({
      comicId: cid,
      startPage: start,
      endPage: end,
      durationSec: duration
    })
    console.log('[Reader] history saved:', result)
  } catch (e) {
    console.error('[Reader] record history error:', e)
  }
}

function onKeyDown(e) {
  if (e.target.tagName === 'INPUT' || e.target.tagName === 'TEXTAREA') return
  switch (e.key) {
    case 'Escape':
      if (showShortcuts.value) { showShortcuts.value = false; return }
      if (showSettings.value) { showSettings.value = false; return }
      goBack(); break
    case 'ArrowLeft': e.preventDefault(); onPageChange(currentPage.value - 1); break
    case 'ArrowRight': case ' ': e.preventDefault(); onPageChange(currentPage.value + 1); break
    case 'Home': e.preventDefault(); onPageChange(1); break
    case 'End': e.preventDefault(); onPageChange(totalPages.value); break
    case 'f': case 'F': if (!e.ctrlKey) { e.preventDefault(); toggleFullscreen() }; break
    case 'd': case 'D':
      if (!e.ctrlKey) {
        e.preventDefault()
        const modes = ['single', 'double', 'scroll']
        const idx = modes.indexOf(currentMode.value)
        onModeChange(modes[(idx + 1) % modes.length])
      }; break
    case 'p': case 'P': if (!e.ctrlKey) { e.preventDefault(); toggleAutoPlay() }; break
    case '[': adjustInterval(-1); break
    case ']': adjustInterval(1); break
    case '?': showShortcuts.value = !showShortcuts.value; break
  }
}

onMounted(async () => {
  readStartTime = Date.now()
  console.log('[Reader] mounted, comicId:', route.params.id)
  await Promise.all([loadComic(), loadPages()])
  resetToolbarTimer()
  document.addEventListener('keydown', onKeyDown)
  console.log('[Reader] ready, mode:', currentMode.value, 'pages:', pages.value.length)
})

onUnmounted(() => {
  document.removeEventListener('keydown', onKeyDown)
  if (toolbarTimer) clearTimeout(toolbarTimer)
  if (saveProgressTimer) clearTimeout(saveProgressTimer)
  stopAutoPlay()
  recordHistory()
})
</script>

<style scoped lang="scss">
.reader-view {
  position: fixed;
  inset: 0;
  background-color: #000;
  z-index: 1000;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

.reader-loading {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  flex: 1;
  gap: 16px;
  color: #fff;
  font-size: 16px;
}

.spinner {
  width: 36px;
  height: 36px;
  border: 3px solid rgba(255,255,255,0.2);
  border-top-color: #fff;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}
@keyframes spin { to { transform: rotate(360deg); } }

.back-btn {
  margin-top: 12px;
  padding: 8px 24px;
  background: #409eff;
  color: #fff;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
}

// Top Toolbar
.top-toolbar {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  height: 44px;
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 0 12px;
  background: linear-gradient(to bottom, rgba(0,0,0,0.8), rgba(0,0,0,0.4), transparent);
  z-index: 200;
}

.tb-btn {
  padding: 4px 10px;
  background: none;
  border: 1px solid rgba(255,255,255,0.15);
  border-radius: 4px;
  color: rgba(255,255,255,0.8);
  font-size: 12px;
  cursor: pointer;
  white-space: nowrap;
  transition: all 0.15s;
  &:hover { background: rgba(255,255,255,0.1); color: #fff; }
  &.active { background: rgba(64,158,255,0.3); border-color: #409eff; color: #409eff; }
}

.tb-title {
  flex: 1;
  text-align: center;
  color: rgba(255,255,255,0.7);
  font-size: 13px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  padding: 0 8px;
}

.tb-page {
  color: rgba(255,255,255,0.5);
  font-size: 12px;
  white-space: nowrap;
}

// Bottom Progress Bar
.bottom-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  height: 40px;
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 0 16px;
  background: linear-gradient(to top, rgba(0,0,0,0.85), rgba(0,0,0,0.5), transparent);
  z-index: 200;
}

.bb-page {
  color: rgba(255,255,255,0.6);
  font-size: 12px;
  min-width: 24px;
  text-align: center;
  font-variant-numeric: tabular-nums;
}

.bb-slider {
  flex: 1;
  height: 4px;
  -webkit-appearance: none;
  appearance: none;
  background: rgba(255,255,255,0.2);
  border-radius: 2px;
  outline: none;
  cursor: pointer;

  &::-webkit-slider-thumb {
    -webkit-appearance: none;
    width: 14px;
    height: 14px;
    background: #409eff;
    border-radius: 50%;
    cursor: pointer;
    box-shadow: 0 0 4px rgba(0,0,0,0.5);
    transition: transform 0.1s;
    &:hover { transform: scale(1.2); }
  }

  &::-moz-range-thumb {
    width: 14px;
    height: 14px;
    background: #409eff;
    border: none;
    border-radius: 50%;
    cursor: pointer;
  }
}

.bb-input-group {
  display: flex;
  align-items: center;
  gap: 4px;
}

.bb-input {
  width: 48px;
  height: 24px;
  background: rgba(255,255,255,0.1);
  border: 1px solid rgba(255,255,255,0.2);
  border-radius: 4px;
  color: #fff;
  font-size: 12px;
  text-align: center;
  outline: none;
  &:focus { border-color: #409eff; }
}

.bb-label {
  color: rgba(255,255,255,0.5);
  font-size: 11px;
}

// Auto-play indicator
.autoplay-indicator {
  position: fixed;
  top: 50px;
  right: 16px;
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 4px 12px;
  background: rgba(0,0,0,0.7);
  color: #67c23a;
  border-radius: 12px;
  font-size: 11px;
  z-index: 250;
  pointer-events: none;
}

.autoplay-dot {
  width: 6px;
  height: 6px;
  background: #67c23a;
  border-radius: 50%;
  animation: pulse 1.5s ease-in-out infinite;
}
@keyframes pulse { 0%,100% { opacity:1; } 50% { opacity:0.3; } }

// Settings panel
.settings-panel {
  position: fixed;
  top: 0;
  right: 0;
  width: 260px;
  height: 100vh;
  background: #1a1a1a;
  border-left: 1px solid #333;
  z-index: 300;
  display: flex;
  flex-direction: column;
  box-shadow: -4px 0 16px rgba(0,0,0,0.5);
}

.settings-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 14px 16px;
  border-bottom: 1px solid #333;
  h3 { color: #fff; font-size: 14px; margin: 0; }
}

.close-btn {
  background: none;
  border: none;
  color: #fff;
  font-size: 18px;
  cursor: pointer;
  &:hover { color: #f56c6c; }
}

.settings-body {
  padding: 14px;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.setting-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.setting-label { color: #ccc; font-size: 13px; }

.mode-btns {
  display: flex;
  gap: 2px;
  button {
    padding: 3px 10px;
    background: #333;
    color: #ccc;
    border: 1px solid #555;
    cursor: pointer;
    font-size: 12px;
    &:first-child { border-radius: 4px 0 0 4px; }
    &:last-child { border-radius: 0 4px 4px 0; }
    &.active { background: #409eff; color: #fff; border-color: #409eff; }
  }
}

.toggle-btn {
  padding: 3px 10px;
  background: #333;
  color: #ccc;
  border: 1px solid #555;
  border-radius: 4px;
  cursor: pointer;
  font-size: 12px;
}

.interval-control {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #ccc;
  font-size: 13px;
  button {
    width: 24px;
    height: 24px;
    background: #333;
    color: #ccc;
    border: 1px solid #555;
    border-radius: 4px;
    cursor: pointer;
    font-size: 14px;
    &:disabled { opacity: 0.4; }
  }
}

// Shortcuts overlay
.shortcuts-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0,0,0,0.6);
  z-index: 400;
  display: flex;
  align-items: center;
  justify-content: center;
}

.shortcuts-dialog {
  background: #1a1a1a;
  border: 1px solid #333;
  border-radius: 10px;
  width: 320px;
  box-shadow: 0 8px 32px rgba(0,0,0,0.5);
}

.shortcuts-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 14px 18px;
  border-bottom: 1px solid #333;
  h3 { color: #fff; font-size: 14px; margin: 0; }
}

.shortcuts-body { padding: 10px 18px; }

.shortcut-row {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 6px 0;
  border-bottom: 1px solid #222;
  color: #ccc;
  font-size: 12px;
  &:last-child { border-bottom: none; }
  kbd {
    display: inline-block;
    padding: 1px 6px;
    background: #333;
    border: 1px solid #555;
    border-radius: 3px;
    font-family: monospace;
    font-size: 11px;
    color: #fff;
    min-width: 20px;
    text-align: center;
  }
  span { margin-left: auto; color: #888; }
}

// Transitions
.toolbar-fade-enter-active, .toolbar-fade-leave-active { transition: opacity 0.25s; }
.toolbar-fade-enter-from, .toolbar-fade-leave-to { opacity: 0; }

.slide-right-enter-active, .slide-right-leave-active { transition: transform 0.25s; }
.slide-right-enter-from, .slide-right-leave-to { transform: translateX(100%); }

.fade-enter-active, .fade-leave-active { transition: opacity 0.2s; }
.fade-enter-from, .fade-leave-to { opacity: 0; }
</style>
