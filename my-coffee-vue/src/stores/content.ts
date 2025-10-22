import { defineStore } from 'pinia'
import request from '@/utils/request'

interface Content {
  id: number
  title: string
  content: string
  images: string[]
  createdAt: string
}

export const useContentStore = defineStore('content', {
  state: () => ({
    contentList: [] as Content[],
    contentDetail: null as Content | null,
  }),
  actions: {
    async fetchContents() {
      const res = await request.get('/contents')
      this.contentList = res.data
    },
    async fetchContentDetail(id: number) {
      const res = await request.get(`/contents/${id}`)
      this.contentDetail = res.data
    },
  },
})
