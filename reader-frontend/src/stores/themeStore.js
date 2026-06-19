import { reactive } from 'vue'

const state = reactive({
  theme: localStorage.getItem('theme') || 'light'
})

function toggleTheme() {
  state.theme = state.theme === 'light' ? 'dark' : 'light'
  localStorage.setItem('theme', state.theme)
  applyTheme()
}

function applyTheme() {
  document.documentElement.setAttribute('data-theme', state.theme)
}

// Apply on load
applyTheme()

export function useThemeStore() {
  return {
    state,
    toggleTheme,
    applyTheme
  }
}
