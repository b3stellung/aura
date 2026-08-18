import { defineStore } from 'pinia'
import { computed, ref } from 'vue'
import * as homeApi from '@/api/home'
import type { RecommendationItem } from '@/api/home'
import type { OutfitPost } from '@/types'

const CATEGORIES = ['全部', '通勤', '约会', '休闲', '派对']

// Mock data for development / API fallback. Surfaces as example content in UI.
const MOCK_POSTS: PostItem[] = [
  {
    id: '1',
    title: '轻盈通勤套装',
    imageUrl: '',
    author: { username: 'Aura 示例', avatar: '' },
    likes: 128,
    createdAt: '',
    tags: ['通勤'],
    height: 260,
    color: '#E8DDD3',
    time: '示例',
    isLiked: false,
    isFavorited: false,
  },
  {
    id: '2',
    title: '周末松弛感 look',
    imageUrl: '',
    author: { username: 'Aura 示例', avatar: '' },
    likes: 256,
    createdAt: '',
    tags: ['休闲'],
    height: 220,
    color: '#D4E4ED',
    time: '示例',
    isLiked: false,
    isFavorited: false,
  },
  {
    id: '3',
    title: '柔和晚间搭配',
    imageUrl: '',
    author: { username: 'Aura 示例', avatar: '' },
    likes: 89,
    createdAt: '',
    tags: ['约会'],
    height: 240,
    color: '#F0E0E0',
    time: '示例',
    isLiked: false,
    isFavorited: false,
  },
  {
    id: '4',
    title: '低饱和派对层次',
    imageUrl: '',
    author: { username: 'Aura 示例', avatar: '' },
    likes: 342,
    createdAt: '',
    tags: ['派对'],
    height: 230,
    color: '#D3D8E8',
    time: '示例',
    isLiked: false,
    isFavorited: false,
  },
]

const MOCK_RECOMMENDATIONS: RecommendationItem[] = [
  {
    id: '1',
    title: '温柔通勤穿搭',
    description: '示例推荐：基于清爽天气、办公室场合与简约偏好生成。',
    gradient: 'from-accent-start via-accent-via to-accent-end',
  },
]

export type PostItem = OutfitPost & {
  height: number
  color: string
  time: string
  isLiked: boolean
  isFavorited: boolean
}

export const useHomeStore = defineStore('home', () => {
  const posts = ref<PostItem[]>([])
  const recommendations = ref<RecommendationItem[]>([])
  const activeCategory = ref(CATEGORIES[0])
  const categories = ref(CATEGORIES)
  const isLoading = ref(false)
  const isRefreshing = ref(false)
  const page = ref(1)
  const hasMore = ref(true)
  const error = ref<string | null>(null)
  const isUsingExampleContent = ref(false)

  const filteredPosts = computed(() => {
    const cat = activeCategory.value
    if (cat === '全部') return posts.value
    return posts.value.filter((post) => post.tags?.includes(cat))
  })

  const leftColumn = computed(() => filteredPosts.value.filter((_, index) => index % 2 === 0))
  const rightColumn = computed(() => filteredPosts.value.filter((_, index) => index % 2 === 1))

  async function fetchPosts(reset = false) {
    if (isLoading.value) return
    if (reset) {
      page.value = 1
      hasMore.value = true
    }
    if (!hasMore.value) return

    isLoading.value = true
    error.value = null
    try {
      const res = await homeApi.getOutfitPosts({
        page: page.value,
        pageSize: 10,
        category: activeCategory.value,
      })
      const mapped = res.items.map((item) => ({
        ...item,
        height: Math.floor(Math.random() * 100) + 180,
        color: '#E8E8E8',
        time: formatTime(item.createdAt),
        isLiked: false,
        isFavorited: false,
      }))
      if (reset || page.value === 1) {
        posts.value = mapped
      } else {
        posts.value.push(...mapped)
      }
      hasMore.value = posts.value.length < res.total
      page.value++
      isUsingExampleContent.value = false
    } catch {
      error.value = '暂时无法连接推荐服务，当前展示示例内容。'
      if (reset || posts.value.length === 0) {
        posts.value = [...MOCK_POSTS]
        hasMore.value = false
        isUsingExampleContent.value = true
      }
    } finally {
      isLoading.value = false
    }
  }

  async function fetchRecommendations() {
    try {
      recommendations.value = await homeApi.getRecommendations()
      isUsingExampleContent.value = false
    } catch {
      recommendations.value = [...MOCK_RECOMMENDATIONS]
      error.value = '暂时无法连接推荐服务，当前展示示例内容。'
      isUsingExampleContent.value = true
    }
  }

  async function refresh() {
    isRefreshing.value = true
    await fetchPosts(true)
    await fetchRecommendations()
    isRefreshing.value = false
  }

  async function loadMore() {
    if (!hasMore.value || isLoading.value) return
    await fetchPosts()
  }

  async function toggleLike(postId: string) {
    const post = posts.value.find((item) => item.id === postId)
    if (!post) return

    const wasLiked = post.isLiked
    post.isLiked = !wasLiked
    post.likes += wasLiked ? -1 : 1

    try {
      if (wasLiked) {
        await homeApi.unlikePost(postId)
      } else {
        await homeApi.likePost(postId)
      }
    } catch {
      post.isLiked = wasLiked
      post.likes += wasLiked ? 1 : -1
    }
  }

  async function toggleFavorite(postId: string) {
    const post = posts.value.find((item) => item.id === postId)
    if (!post) return

    const wasFavorited = post.isFavorited
    post.isFavorited = !wasFavorited

    try {
      if (wasFavorited) {
        await homeApi.unfavoritePost(postId)
      } else {
        await homeApi.favoritePost(postId)
      }
    } catch {
      post.isFavorited = wasFavorited
    }
  }

  function setCategory(cat: string) {
    activeCategory.value = cat
  }

  return {
    posts,
    recommendations,
    activeCategory,
    categories,
    isLoading,
    isRefreshing,
    page,
    hasMore,
    error,
    isUsingExampleContent,
    filteredPosts,
    leftColumn,
    rightColumn,
    fetchPosts,
    fetchRecommendations,
    refresh,
    loadMore,
    toggleLike,
    toggleFavorite,
    setCategory,
  }
})

function formatTime(dateStr: string): string {
  if (!dateStr) return ''
  const now = Date.now()
  const then = new Date(dateStr).getTime()
  const diff = now - then
  const mins = Math.floor(diff / 60000)
  if (mins < 60) return `${Math.max(mins, 1)}分钟前`
  const hours = Math.floor(mins / 60)
  if (hours < 24) return `${hours}小时前`
  const days = Math.floor(hours / 24)
  return `${days}天前`
}
