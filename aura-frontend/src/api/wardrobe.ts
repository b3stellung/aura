import { apiClient } from './client'
import type { OutfitItem } from '@/types'

export interface WardrobeListResponse {
  items: OutfitItem[]
  total: number
}

export interface CreateClothingRequest {
  name: string
  category: 'top' | 'bottom' | 'shoes' | 'accessory'
  imageUrl?: string
  color?: string
  tags?: string[]
  brand?: string
  season?: string
  notes?: string
}

export interface UpdateClothingRequest extends Partial<CreateClothingRequest> {}

// 获取衣物列表
export function getWardrobeItems(params?: { category?: string }) {
  const query = new URLSearchParams()
  if (params?.category && params.category !== '全部') {
    query.set('category', params.category)
  }
  const qs = query.toString()
  return apiClient.get<WardrobeListResponse>(`/wardrobe/items${qs ? '?' + qs : ''}`)
}

// 获取单个衣物详情
export function getClothingItem(itemId: string) {
  return apiClient.get<OutfitItem>(`/wardrobe/items/${itemId}`)
}

// 添加衣物
export function createClothingItem(data: CreateClothingRequest) {
  return apiClient.post<OutfitItem>('/wardrobe/items', data)
}

// 更新衣物
export function updateClothingItem(itemId: string, data: UpdateClothingRequest) {
  return apiClient.put<OutfitItem>(`/wardrobe/items/${itemId}`, data)
}

// 删除衣物
export function deleteClothingItem(itemId: string) {
  return apiClient.delete(`/wardrobe/items/${itemId}`)
}

// 上传衣物图片
export async function uploadClothingImage(file: File): Promise<{ url: string }> {
  const formData = new FormData()
  formData.append('image', file)

  const token = localStorage.getItem('aura_token')
  const res = await fetch('/api/wardrobe/upload', {
    method: 'POST',
    headers: token ? { Authorization: `Bearer ${token}` } : {},
    body: formData,
  })

  if (!res.ok) throw new Error('图片上传失败')
  return res.json()
}
