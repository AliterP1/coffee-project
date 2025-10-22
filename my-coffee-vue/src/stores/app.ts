import { defineStore } from 'pinia'

export const useAppStore = defineStore('app', {
  state: () => ({
    theme: 'light' as 'light' | 'dark',
    language: 'zh-CN',
    loading: false,
  }),
  actions: {
    setTheme(theme: 'light' | 'dark') {
      this.theme = theme
    },
    setLanguage(lang: string) {
      this.language = lang
    },
    setLoading(status: boolean) {
      this.loading = status
    },
  },
})
