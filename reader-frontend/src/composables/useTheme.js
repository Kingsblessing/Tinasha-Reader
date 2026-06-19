import { computed } from 'vue'
import { useThemeStore } from '../stores/themeStore'

export function useTheme() {
  const { state, toggleTheme, applyTheme } = useThemeStore()

  const isDark = computed(() => state.theme === 'dark')
  const currentTheme = computed(() => state.theme)

  return {
    isDark,
    currentTheme,
    toggleTheme,
    applyTheme
  }
}
