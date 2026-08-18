// Types for the Aura application

export interface User {
  id: string
  username: string
  nickname: string
  avatar: string
  bio: string
  followers: number
  following: number
  outfits: number
  styleProfile?: StyleProfile
}

export interface StyleProfile {
  mainStyle: string
  mainStyleScore: number
  preferredColors: string[]
  dimensions: {
    minimalist: number
    elegant: number
    casual: number
    trendy: number
  }
}

export interface OutfitItem {
  id: string
  name: string
  category: 'top' | 'bottom' | 'shoes' | 'accessory'
  imageUrl: string
  wearCount: number
  tags: string[]
  color?: string
  brand?: string
  season?: string
  notes?: string
  isNew?: boolean
}

export interface OutfitPost {
  id: string
  title: string
  imageUrl: string
  author: {
    username: string
    avatar: string
  }
  likes: number
  createdAt: string
  tags: string[]
}

export interface ChatMessage {
  id: string
  role: 'user' | 'assistant'
  content: string
  timestamp: string
  recommendation?: OutfitRecommendation
}

export interface OutfitRecommendation {
  title: string
  items: OutfitItem[]
  matchScore: number
  imageUrl?: string
}

export interface Category {
  id: string
  name: string
  icon?: string
}

export type TabType = 'home' | 'wardrobe' | 'ai' | 'profile'

export interface ApiListResponse<T> {
  items: T[]
  total: number
  page: number
  pageSize: number
}
