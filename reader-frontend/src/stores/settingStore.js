import { reactive } from 'vue'
import * as settingApi from '../api/settingApi'

const state = reactive({
  settings: {},
  loaded: false
})

async function loadSettings() {
  try {
    const data = await settingApi.getAll()
    if (data) {
      const settingsMap = {}
      if (Array.isArray(data)) {
        data.forEach(s => {
          settingsMap[s.key] = s.value
        })
      } else {
        Object.assign(settingsMap, data)
      }
      state.settings = settingsMap
    }
    state.loaded = true
  } catch (e) {
    console.error('Failed to load settings:', e)
    state.loaded = true
  }
}

async function saveSetting(key, value) {
  try {
    await settingApi.set(key, value)
    state.settings[key] = value
  } catch (e) {
    console.error('Failed to save setting:', e)
  }
}

function getSetting(key, defaultValue) {
  return state.settings[key] ?? defaultValue
}

export function useSettingStore() {
  return {
    state,
    loadSettings,
    saveSetting,
    getSetting
  }
}
