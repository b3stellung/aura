import { apiClient } from './client'
import type { OutfitPost } from '@/types'

export interface OutfitPostListResponse {
  items: OutfitPost[]
  total: number
  page: number
  pageSize: number
}

export interface RecommendationItem {
  id: string
  title: string
  description: string
  imageUrl?: string
  gradient?: string
}

// 获取推荐穿搭列表
export function getRecommendations() {
  return apiClient.get<RecommendationItem[]>('/home/recommendations')
}

// 获取穿搭帖子列表（分页+分类筛选）
export function getOutfitPosts(params: {
  page?: number
  pageSize?: number
  category?: string
}) {
  const query = new URLSearchParams()
  if (params.page) query.set('page', String(params.page))
  if (params.pageSize) query.set('pageSize', String(params.pageSize))
  if (params.category && params.category !== '全部') query.set('category', params.category)
  return apiClient.get<OutfitPostListResponse>(`/home/posts?${query.toString()}`)
}

// 点赞帖子
export function likePost(postId: string) {
  return apiClient.post<{ likes: number }>(`/home/posts/${postId}/like`)
}

// 取消点赞
export function unlikePost(postId: string) {
  return apiClient.delete<{ likes: number }>(`/home/posts/${postId}/like`)
}

// 收藏帖子
export function favoritePost(postId: string) {
  return apiClient.post(`/home/posts/${postId}/favorite`)
}

// 取消收藏
export function unfavoritePost(postId: string) {
  return apiClient.delete(`/home/posts/${postId}/favorite`)
}
