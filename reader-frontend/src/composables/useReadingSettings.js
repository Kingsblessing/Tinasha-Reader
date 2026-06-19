import { ref, watch, onMounted } from 'vue'
import { ReadingMode } from '../utils/constants'

const STORAGE_KEY = 'reader-settings'

const defaults = {
  readingMode: ReadingMode.SINGLE,
  readingDirection: 'ltr', // 'ltr' or 'rtl'
  fitMode: 'width', // 'width', 'height', 'window', 'original'
  preloadCount: 3,
  animationEnabled: true,
  autoHideToolbar: true,
  autoHideDelay: 3000,
  scrollSpeed: 1,
  backgroundColor: '#000000'
}

function loadSettings() {
  try {
    const stored = localStorage.getItem(STORAGE_KEY)
    if (stored) {
      return { ...defaults, ...JSON.parse(stored) }
    }
  } catch (e) {
    // ignore
  }
  return { ...defaults }
}

function saveSettings(settings) {
  try {
    localStorage.setItem(STORAGE_KEY, JSON.stringify(settings))
  } catch (e) {
    // ignore
  }
}

const settings = ref(loadSettings())

export function useReadingSettings() {
  function updateSetting(key, value) {
    settings.value[key] = value
    saveSettings(settings.value)
  }

  function setReadingMode(mode) {
    updateSetting('readingMode', mode)
  }

  function setReadingDirection(direction) {
    updateSetting('readingDirection', direction)
  }

  function setFitMode(mode) {
    updateSetting('fitMode', mode)
  }

  function toggleDirection() {
    const newDir = settings.value.readingDirection === 'ltr' ? 'rtl' : 'ltr'
    updateSetting('readingDirection', newDir)
  }

  function cycleFitMode() {
    const modes = ['width', 'height', 'window', 'original']
    const idx = modes.indexOf(settings.value.fitMode)
    const next = modes[(idx + 1) % modes.length]
    updateSetting('fitMode', next)
  }

  function cycleReadingMode() {
    const modes = [ReadingMode.SINGLE, ReadingMode.DOUBLE, ReadingMode.SCROLL]
    const idx = modes.indexOf(settings.value.readingMode)
    const next = modes[(idx + 1) % modes.length]
    updateSetting('readingMode', next)
  }

  return {
    settings,
    updateSetting,
    setReadingMode,
    setReadingDirection,
    setFitMode,
    toggleDirection,
    cycleFitMode,
    cycleReadingMode
  }
}
