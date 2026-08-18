import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { OutfitItem } from '@/types'
import * as wardrobeApi from '@/api/wardrobe'
import type { CreateClothingRequest, UpdateClothingRequest } from '@/api/wardrobe'

// Category map: display name -> API category value
export const CATEGORY_MAP: Record<string, string> = {
  '全部': '',
  '上衣': 'top',
  '下装': 'bottom',
  '鞋': 'shoes',
  '配饰': 'accessory',
}

export const CATEGORY_LABELS = Object.keys(CATEGORY_MAP)

// Mock data for development without backend
const MOCK_ITEMS: (OutfitItem & { color: string; isNew: boolean })[] = [
  { id: '1', name: '白色 T 恤', category: 'top', imageUrl: '', wearCount: 12, tags: ['百搭', '基础'], color: '#F5F5F5', isNew: false },
  { id: '2', name: '牛仔裤', category: 'bottom', imageUrl: '', wearCount: 8, tags: ['经典'], color: '#A8C4E0', isNew: false },
  { id: '3', name: '运动鞋', category: 'shoes', imageUrl: '', wearCount: 15, tags: ['舒适'], color: '#E8E8E8', isNew: false },
  { id: '4', name: '条纹衬衫', category: 'top', imageUrl: '', wearCount: 3, tags: ['通勤'], color: '#D6E4F0', isNew: true },
  { id: '5', name: '卡其裤', category: 'bottom', imageUrl: '', wearCount: 5, tags: ['休闲'], color: '#E0D3B8', isNew: false },
  { id: '6', name: '帆布包', category: 'accessory', imageUrl: '', wearCount: 7, tags: ['百搭'], color: '#D8CDB8', isNew: false },
  { id: '7', name: '针织衫', category: 'top', imageUrl: '', wearCount: 2, tags: ['温柔'], color: '#E8D8D0', isNew: true },
  { id: '8', name: '小白鞋', category: 'shoes', imageUrl: '', wearCount: 20, tags: ['百搭', '经典'], color: '#FFFFFF', isNew: false },
  { id: '9', name: '丝巾', category: 'accessory', imageUrl: '', wearCount: 1, tags: ['优雅'], color: '#E0C8D8', isNew: true },
]


export const useWardrobeStore = defineStore('wardrobe', () => {
  // State
  const items = ref<(OutfitItem & { color?: string; isNew?: boolean })[]>([])
  const activeCategory = ref('全部')
  const isLoading = ref(false)
  const error = ref<string | null>(null)
  const showAddModal = ref(false)
  const showEditModal = ref(false)
  const editingItem = ref<(OutfitItem & { color?: string }) | null>(null)

  // Getters
  const filteredItems = computed(() => {
    const cat = CATEGORY_MAP[activeCategory.value]
    if (!cat) return items.value
    return items.value.filter((item) => item.category === cat)
  })

  const itemCount = computed(() => items.value.length)

  const categoryCounts = computed(() => {
    const counts: Record<string, number> = { '全部': items.value.length }
    for (const [label, val] of Object.entries(CATEGORY_MAP)) {
      if (val) {
        counts[label] = items.value.filter((i) => i.category === val).length
      }
    }
    return counts
  })

  // Actions
  async function fetchItems() {
    isLoading.value = true
    error.value = null
    try {
      const res = await wardrobeApi.getWardrobeItems({
        category: activeCategory.value,
      })
      items.value = res.items
    } catch {
      // Fallback to mock data
      items.value = [...MOCK_ITEMS]
    } finally {
      isLoading.value = false
    }
  }

  async function addItem(data: CreateClothingRequest) {
    isLoading.value = true
    error.value = null
    try {
      const newItem = await wardrobeApi.createClothingItem(data)
      items.value.unshift(newItem)
    } catch {
      // Optimistic: add locally with mock ID
      const mockNew: OutfitItem & { color: string; isNew: boolean } = {
        id: 'mock_' + Date.now(),
        name: data.name,
        category: data.category,
        imageUrl: data.imageUrl || '',
        wearCount: 0,
        tags: data.tags || [],
        color: data.color || '#E8E8E8',
        isNew: true,
      }
      items.value.unshift(mockNew)
    } finally {
      isLoading.value = false
      showAddModal.value = false
    }
  }

  async function updateItem(itemId: string, data: UpdateClothingRequest) {
    isLoading.value = true
    error.value = null
    try {
      const updated = await wardrobeApi.updateClothingItem(itemId, data)
      const idx = items.value.findIndex((i) => i.id === itemId)
      if (idx !== -1) items.value[idx] = { ...items.value[idx], ...updated }
    } catch {
      // Optimistic: update locally
      const idx = items.value.findIndex((i) => i.id === itemId)
      if (idx !== -1) {
        items.value[idx] = { ...items.value[idx], ...data } as OutfitItem & { color?: string; isNew?: boolean }
      }
    } finally {
      isLoading.value = false
      showEditModal.value = false
      editingItem.value = null
    }
  }

  async function deleteItem(itemId: string) {
    try {
      await wardrobeApi.deleteClothingItem(itemId)
    } catch {
      // Proceed with local deletion
    }
    items.value = items.value.filter((i) => i.id !== itemId)
  }

  function setCategory(cat: string) {
    activeCategory.value = cat
  }

  function openAdd() {
    showAddModal.value = true
  }

  function openEdit(item: OutfitItem & { color?: string }) {
    editingItem.value = { ...item }
    showEditModal.value = true
  }

  function closeModals() {
    showAddModal.value = false
    showEditModal.value = false
    editingItem.value = null
  }


  return {
    items,
    activeCategory,
    isLoading,
    error,
    filteredItems,
    itemCount,
    categoryCounts,
    showAddModal,
    showEditModal,
    editingItem,
    fetchItems,
    addItem,
    updateItem,
    deleteItem,
    setCategory,
    openAdd,
    openEdit,
    closeModals,
  }
})
