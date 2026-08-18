<script setup lang="ts">
import { onMounted, ref, computed } from 'vue'
import {
  Plus,
  Pencil,
  Trash2,
  X,
  Loader2,
  Shirt,
  Footprints,
  Watch,
  PackageOpen,
  ShoppingBag,
  LayoutGrid,
  List,
  Search,
  MoreHorizontal,
} from 'lucide-vue-next'
import { useWardrobeStore, CATEGORY_LABELS } from '@/stores/wardrobe'
import { storeToRefs } from 'pinia'
import type { OutfitItem } from '@/types'
import imagesData from '@/assets/images.json'

const wardrobeStore = useWardrobeStore()
const {
  activeCategory,
  isLoading,
  filteredItems,
  categoryCounts,
  showAddModal,
  showEditModal,
  editingItem,
} = storeToRefs(wardrobeStore)

const CATEGORY_ICONS: Record<string, typeof Shirt> = {
  top: Shirt,
  bottom: PackageOpen,
  shoes: Footprints,
  accessory: Watch,
}

function getCategoryIcon(category: string) {
  return CATEGORY_ICONS[category] || ShoppingBag
}

const viewMode = ref<'grid' | 'list'>('grid')
const searchQuery = ref('')

// Merge with real image data
const enrichedItems = computed(() => {
  const allClothing = [
    ...imagesData.clothing.tops,
    ...imagesData.clothing.bottoms,
    ...imagesData.clothing.shoes,
    ...imagesData.clothing.accessories,
  ]
  return filteredItems.value.map((item, i) => ({
    ...item,
    imageUrl: item.imageUrl || allClothing[i % allClothing.length]?.url || '',
    realColor: allClothing[i % allClothing.length] ? undefined : item.color,
  }))
})

const displayItems = computed(() => {
  if (!searchQuery.value.trim()) return enrichedItems.value
  const q = searchQuery.value.toLowerCase()
  return enrichedItems.value.filter(
    (item) => item.name.toLowerCase().includes(q) || item.tags?.some(t => t.toLowerCase().includes(q))
  )
})

// Add form
const addForm = ref({
  name: '',
  category: 'top' as OutfitItem['category'],
  color: '#E8E8E8',
  tags: [] as string[],
  brand: '',
  season: '',
})

// Edit form
const editForm = ref({
  name: '',
  category: 'top' as OutfitItem['category'],
  color: '#E8E8E8',
  tags: [] as string[],
  brand: '',
  season: '',
})

const categoryOptions = [
  { label: 'Top', value: 'top' },
  { label: 'Bottom', value: 'bottom' },
  { label: 'Shoes', value: 'shoes' },
  { label: 'Accessory', value: 'accessory' },
]

onMounted(() => {
  wardrobeStore.fetchItems()
})

function handleAdd() {
  if (!addForm.value.name.trim()) return
  wardrobeStore.addItem({
    name: addForm.value.name.trim(),
    category: addForm.value.category,
    color: addForm.value.color,
    tags: addForm.value.tags,
    brand: addForm.value.brand,
    season: addForm.value.season,
  })
  resetAddForm()
}

function resetAddForm() {
  addForm.value = { name: '', category: 'top', color: '#E8E8E8', tags: [], brand: '', season: '' }
}

function openEdit(item: OutfitItem & { color?: string }) {
  editForm.value = {
    name: item.name,
    category: item.category,
    color: item.color || '#E8E8E8',
    tags: [...item.tags],
    brand: item.brand || '',
    season: item.season || '',
  }
  wardrobeStore.openEdit(item)
}

function handleUpdate() {
  if (!editingItem.value || !editForm.value.name.trim()) return
  wardrobeStore.updateItem(editingItem.value.id, {
    name: editForm.value.name.trim(),
    category: editForm.value.category,
    color: editForm.value.color,
    tags: editForm.value.tags,
    brand: editForm.value.brand,
    season: editForm.value.season,
  })
}

function handleDelete(itemId: string) {
  wardrobeStore.deleteItem(itemId)
}
</script>

<template>
  <div class="h-full overflow-y-auto">
    <div class="max-w-[1200px] mx-auto px-6 py-5">

      <!-- Top Stats Bar -->
      <div class="grid grid-cols-5 gap-4 mb-6">
        <div
          v-for="cat in CATEGORY_LABELS"
          :key="cat"
          class="bg-bg-card rounded-card border border-border p-4 cursor-pointer hover:border-text-secondary transition-colors"
          :class="activeCategory === cat ? 'border-ig-blue bg-ig-blue/5' : ''"
          @click="wardrobeStore.setCategory(cat)"
        >
          <div class="flex items-center justify-between mb-2">
            <span class="text-[11px] text-text-secondary uppercase tracking-wider">{{ cat }}</span>
            <span class="text-lg font-bold text-text-primary">{{ categoryCounts[cat] || 0 }}</span>
          </div>
          <div class="h-1 bg-bg-secondary rounded-full overflow-hidden">
            <div
              class="h-full rounded-full transition-all duration-300"
              :class="activeCategory === cat ? 'bg-ig-blue' : 'bg-text-tertiary/30'"
              :style="{ width: Math.min(((categoryCounts[cat] || 0) / (categoryCounts['全部'] || 1)) * 100, 100) + '%' }"
            />
          </div>
        </div>
      </div>

      <!-- Toolbar -->
      <div class="flex items-center justify-between mb-4">
        <div class="flex items-center gap-3">
          <h3 class="text-sm font-semibold text-text-primary">
            {{ activeCategory === '全部' ? 'All Items' : activeCategory }}
            <span class="text-text-tertiary font-normal ml-1">({{ displayItems.length }})</span>
          </h3>
        </div>
        <div class="flex items-center gap-2">
          <!-- Search -->
          <div class="flex items-center gap-2 h-8 px-3 bg-bg-secondary rounded-btn border border-transparent hover:border-border transition-colors">
            <Search :size="14" class="text-text-tertiary" />
            <input
              v-model="searchQuery"
              type="text"
              placeholder="Search items..."
              class="bg-transparent text-[13px] text-text-primary placeholder:text-text-tertiary w-32 outline-none"
            />
          </div>
          <div class="w-px h-5 bg-border mx-1" />
          <button
            class="w-8 h-8 flex items-center justify-center rounded-btn transition-colors"
            :class="viewMode === 'grid' ? 'bg-bg-secondary text-text-primary' : 'text-text-tertiary hover:text-text-secondary'"
            @click="viewMode = 'grid'"
          >
            <LayoutGrid :size="16" />
          </button>
          <button
            class="w-8 h-8 flex items-center justify-center rounded-btn transition-colors"
            :class="viewMode === 'list' ? 'bg-bg-secondary text-text-primary' : 'text-text-tertiary hover:text-text-secondary'"
            @click="viewMode = 'list'"
          >
            <List :size="16" />
          </button>
          <div class="w-px h-5 bg-border mx-1" />
          <button
            class="h-8 px-3 flex items-center gap-1.5 bg-ig-blue text-white text-[13px] font-medium rounded-btn hover:bg-ig-blue-hover transition-colors"
            @click="wardrobeStore.openAdd()"
          >
            <Plus :size="14" />
            Add Item
          </button>
        </div>
      </div>

      <!-- Loading -->
      <div v-if="isLoading" class="flex justify-center py-16">
        <Loader2 :size="24" class="text-text-secondary animate-spin" />
      </div>

      <!-- Grid View -->
      <div v-else-if="viewMode === 'grid'" class="grid grid-cols-6 gap-4">
        <div
          v-for="item in displayItems"
          :key="item.id"
          class="bg-bg-card rounded-card overflow-hidden border border-border relative group cursor-pointer hover:shadow-card-hover transition-all duration-200"
          @dblclick="openEdit(item)"
        >
          <!-- New badge -->
          <div
            v-if="item.isNew"
            class="absolute top-2 left-2 z-10 bg-ig-blue text-white text-[10px] px-2 py-0.5 rounded-tag font-medium"
          >
            New
          </div>

          <!-- Hover overlay -->
          <div class="absolute inset-0 bg-black/0 group-hover:bg-black/30 transition-all duration-200 flex items-center justify-center gap-2 opacity-0 group-hover:opacity-100 z-10">
            <button
              class="w-8 h-8 bg-white rounded-full flex items-center justify-center shadow-md hover:scale-110 transition-transform"
              @click.stop="openEdit(item)"
            >
              <Pencil :size="13" class="text-text-primary" />
            </button>
            <button
              class="w-8 h-8 bg-white rounded-full flex items-center justify-center shadow-md hover:scale-110 transition-transform"
              @click.stop="handleDelete(item.id)"
            >
              <Trash2 :size="13" class="text-error" />
            </button>
          </div>

          <!-- Image -->
          <div
            class="aspect-[3/4] flex items-center justify-center overflow-hidden"
            :style="{ backgroundColor: item.color || '#E8E8E8' }"
          >
            <img
              v-if="item.imageUrl"
              :src="item.imageUrl"
              :alt="item.name"
              class="w-full h-full object-cover group-hover:scale-105 transition-transform duration-300"
            />
            <component v-else :is="getCategoryIcon(item.category)" :size="28" class="text-text-tertiary/40" />
          </div>

          <!-- Info -->
          <div class="p-3">
            <h3 class="text-[12px] font-medium text-text-primary line-clamp-1">{{ item.name }}</h3>
            <div class="flex items-center justify-between mt-1">
              <span class="text-[11px] text-text-secondary">
                {{ item.wearCount > 0 ? `Worn ${item.wearCount}x` : 'Never worn' }}
              </span>
              <button class="opacity-0 group-hover:opacity-100 transition-opacity">
                <MoreHorizontal :size="14" class="text-text-tertiary" />
              </button>
            </div>
          </div>
        </div>
      </div>

      <!-- List View -->
      <div v-else class="space-y-1">
        <div class="grid grid-cols-[1fr_120px_100px_80px_40px] gap-4 px-4 py-2 text-[11px] text-text-secondary uppercase tracking-wider font-medium border-b border-border">
          <span>Item</span>
          <span>Category</span>
          <span>Worn</span>
          <span>Tags</span>
          <span></span>
        </div>
        <div
          v-for="item in displayItems"
          :key="item.id"
          class="grid grid-cols-[1fr_120px_100px_80px_40px] gap-4 px-4 py-3 items-center rounded-btn hover:bg-bg-hover cursor-pointer transition-colors group"
          @dblclick="openEdit(item)"
        >
          <div class="flex items-center gap-3 min-w-0">
            <div
              class="w-10 h-12 rounded overflow-hidden flex-shrink-0"
              :style="{ backgroundColor: item.color || '#E8E8E8' }"
            >
              <img
                v-if="item.imageUrl"
                :src="item.imageUrl"
                :alt="item.name"
                class="w-full h-full object-cover"
              />
              <div v-else class="w-full h-full flex items-center justify-center">
                <component :is="getCategoryIcon(item.category)" :size="16" class="text-text-tertiary/40" />
              </div>
            </div>
            <div class="min-w-0">
              <div class="text-[13px] font-medium text-text-primary truncate">{{ item.name }}</div>
              <div v-if="item.brand" class="text-[11px] text-text-tertiary">{{ item.brand }}</div>
            </div>
          </div>
          <span class="text-[12px] text-text-secondary capitalize">{{ item.category }}</span>
          <span class="text-[12px] text-text-secondary">{{ item.wearCount }}x</span>
          <div class="flex gap-1 flex-wrap">
            <span
              v-for="tag in (item.tags || []).slice(0, 2)"
              :key="tag"
              class="text-[10px] px-1.5 py-0.5 bg-bg-secondary rounded-tag text-text-secondary"
            >
              {{ tag }}
            </span>
          </div>
          <div class="flex gap-1 opacity-0 group-hover:opacity-100 transition-opacity">
            <button class="w-6 h-6 flex items-center justify-center rounded hover:bg-bg-secondary" @click.stop="openEdit(item)">
              <Pencil :size="12" class="text-text-secondary" />
            </button>
            <button class="w-6 h-6 flex items-center justify-center rounded hover:bg-error/10" @click.stop="handleDelete(item.id)">
              <Trash2 :size="12" class="text-error" />
            </button>
          </div>
        </div>
      </div>

      <!-- Empty state -->
      <div v-if="!isLoading && displayItems.length === 0" class="text-center py-16">
        <ShoppingBag :size="48" class="text-text-tertiary/30 mx-auto" />
        <p class="text-sm text-text-secondary mt-3">No items found</p>
        <button
          class="mt-4 h-8 px-4 flex items-center gap-1.5 mx-auto bg-ig-blue text-white text-[13px] font-medium rounded-btn hover:bg-ig-blue-hover transition-colors"
          @click="wardrobeStore.openAdd()"
        >
          <Plus :size="14" />
          Add your first item
        </button>
      </div>
    </div>

    <!-- Add Item Modal (centered desktop modal) -->
    <Teleport to="body">
      <Transition name="fade">
        <div
          v-if="showAddModal"
          class="fixed inset-0 z-[100] bg-black/40 flex items-center justify-center"
          @click.self="wardrobeStore.closeModals()"
        >
          <div class="w-[480px] bg-bg-card rounded-card border border-border shadow-dropdown p-6">
            <div class="flex items-center justify-between mb-5">
              <h2 class="text-[15px] font-semibold text-text-primary">Add Item</h2>
              <button
                class="w-7 h-7 flex items-center justify-center rounded-btn hover:bg-bg-secondary transition-colors"
                @click="wardrobeStore.closeModals()"
              >
                <X :size="16" class="text-text-secondary" />
              </button>
            </div>

            <div class="space-y-4">
              <div>
                <label class="text-[12px] text-text-secondary mb-1.5 block font-medium">Name</label>
                <input
                  v-model="addForm.name"
                  type="text"
                  placeholder="e.g. White T-shirt"
                  class="w-full h-9 px-3 bg-bg-secondary border border-transparent focus:border-ig-blue rounded-btn text-[13px] transition-colors outline-none"
                />
              </div>

              <div>
                <label class="text-[12px] text-text-secondary mb-1.5 block font-medium">Category</label>
                <div class="flex gap-2">
                  <button
                    v-for="opt in categoryOptions"
                    :key="opt.value"
                    class="px-3 py-1.5 rounded-tag text-[12px] font-medium transition-colors"
                    :class="addForm.category === opt.value
                      ? 'bg-text-primary text-white'
                      : 'bg-bg-secondary text-text-secondary hover:text-text-primary'"
                    @click="addForm.category = opt.value as OutfitItem['category']"
                  >
                    {{ opt.label }}
                  </button>
                </div>
              </div>

              <div class="grid grid-cols-2 gap-4">
                <div>
                  <label class="text-[12px] text-text-secondary mb-1.5 block font-medium">Color</label>
                  <div class="flex items-center gap-2">
                    <input
                      v-model="addForm.color"
                      type="color"
                      class="w-9 h-9 rounded-btn border border-border cursor-pointer"
                    />
                    <span class="text-[12px] text-text-secondary font-mono">{{ addForm.color }}</span>
                  </div>
                </div>
                <div>
                  <label class="text-[12px] text-text-secondary mb-1.5 block font-medium">Season</label>
                  <input
                    v-model="addForm.season"
                    type="text"
                    placeholder="e.g. Spring/Summer"
                    class="w-full h-9 px-3 bg-bg-secondary border border-transparent focus:border-ig-blue rounded-btn text-[13px] transition-colors outline-none"
                  />
                </div>
              </div>

              <div>
                <label class="text-[12px] text-text-secondary mb-1.5 block font-medium">Brand (optional)</label>
                <input
                  v-model="addForm.brand"
                  type="text"
                  placeholder="e.g. Uniqlo"
                  class="w-full h-9 px-3 bg-bg-secondary border border-transparent focus:border-ig-blue rounded-btn text-[13px] transition-colors outline-none"
                />
              </div>
            </div>

            <div class="flex gap-3 mt-6">
              <button
                class="flex-1 h-9 rounded-btn text-[13px] font-medium bg-bg-secondary text-text-primary hover:bg-bg-hover transition-colors"
                @click="wardrobeStore.closeModals()"
              >
                Cancel
              </button>
              <button
                class="flex-1 h-9 rounded-btn text-[13px] font-medium text-white transition-colors flex items-center justify-center gap-1.5"
                :class="addForm.name.trim() ? 'bg-ig-blue hover:bg-ig-blue-hover' : 'bg-bg-secondary text-text-tertiary cursor-not-allowed'"
                :disabled="!addForm.name.trim()"
                @click="handleAdd"
              >
                Add to Wardrobe
              </button>
            </div>
          </div>
        </div>
      </Transition>
    </Teleport>

    <!-- Edit Item Modal -->
    <Teleport to="body">
      <Transition name="fade">
        <div
          v-if="showEditModal"
          class="fixed inset-0 z-[100] bg-black/40 flex items-center justify-center"
          @click.self="wardrobeStore.closeModals()"
        >
          <div class="w-[480px] bg-bg-card rounded-card border border-border shadow-dropdown p-6">
            <div class="flex items-center justify-between mb-5">
              <h2 class="text-[15px] font-semibold text-text-primary">Edit Item</h2>
              <button
                class="w-7 h-7 flex items-center justify-center rounded-btn hover:bg-bg-secondary transition-colors"
                @click="wardrobeStore.closeModals()"
              >
                <X :size="16" class="text-text-secondary" />
              </button>
            </div>

            <div class="space-y-4">
              <div>
                <label class="text-[12px] text-text-secondary mb-1.5 block font-medium">Name</label>
                <input
                  v-model="editForm.name"
                  type="text"
                  class="w-full h-9 px-3 bg-bg-secondary border border-transparent focus:border-ig-blue rounded-btn text-[13px] transition-colors outline-none"
                />
              </div>

              <div>
                <label class="text-[12px] text-text-secondary mb-1.5 block font-medium">Category</label>
                <div class="flex gap-2">
                  <button
                    v-for="opt in categoryOptions"
                    :key="opt.value"
                    class="px-3 py-1.5 rounded-tag text-[12px] font-medium transition-colors"
                    :class="editForm.category === opt.value
                      ? 'bg-text-primary text-white'
                      : 'bg-bg-secondary text-text-secondary hover:text-text-primary'"
                    @click="editForm.category = opt.value as OutfitItem['category']"
                  >
                    {{ opt.label }}
                  </button>
                </div>
              </div>

              <div class="grid grid-cols-2 gap-4">
                <div>
                  <label class="text-[12px] text-text-secondary mb-1.5 block font-medium">Color</label>
                  <div class="flex items-center gap-2">
                    <input
                      v-model="editForm.color"
                      type="color"
                      class="w-9 h-9 rounded-btn border border-border cursor-pointer"
                    />
                    <span class="text-[12px] text-text-secondary font-mono">{{ editForm.color }}</span>
                  </div>
                </div>
                <div>
                  <label class="text-[12px] text-text-secondary mb-1.5 block font-medium">Season</label>
                  <input
                    v-model="editForm.season"
                    type="text"
                    class="w-full h-9 px-3 bg-bg-secondary border border-transparent focus:border-ig-blue rounded-btn text-[13px] transition-colors outline-none"
                  />
                </div>
              </div>

              <div>
                <label class="text-[12px] text-text-secondary mb-1.5 block font-medium">Brand</label>
                <input
                  v-model="editForm.brand"
                  type="text"
                  class="w-full h-9 px-3 bg-bg-secondary border border-transparent focus:border-ig-blue rounded-btn text-[13px] transition-colors outline-none"
                />
              </div>
            </div>

            <div class="flex gap-3 mt-6">
              <button
                class="flex-1 h-9 rounded-btn text-[13px] font-medium bg-bg-secondary text-text-primary hover:bg-bg-hover transition-colors"
                @click="wardrobeStore.closeModals()"
              >
                Cancel
              </button>
              <button
                class="flex-1 h-9 rounded-btn text-[13px] font-medium text-white bg-ig-blue hover:bg-ig-blue-hover transition-colors"
                @click="handleUpdate"
              >
                Save Changes
              </button>
            </div>
          </div>
        </div>
      </Transition>
    </Teleport>
  </div>
</template>

<style scoped>
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s ease;
}
.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>
