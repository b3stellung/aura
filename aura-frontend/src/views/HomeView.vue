<script setup lang="ts">
import { onMounted, ref, computed } from 'vue'
import {
  Heart,
  Bookmark,
  Loader2,
  Sparkles,
  Shirt,
  Gem,
  TrendingUp,
  Filter,
  LayoutGrid,
  List,
} from 'lucide-vue-next'
import { useInfiniteScroll } from '@vueuse/core'
import { useHomeStore, type PostItem } from '@/stores/home'
import { storeToRefs } from 'pinia'
import imagesData from '@/assets/images.json'

const homeStore = useHomeStore()
const {
  recommendations,
  activeCategory,
  categories,
  isLoading,
  isRefreshing,
  hasMore,
  leftColumn,
  rightColumn,
} = storeToRefs(homeStore)

const viewMode = ref<'grid' | 'list'>('grid')

// Discover images from assets
const discoverItems = computed<PostItem[]>(() => {
  return imagesData.discover.map((item, i) => ({
    id: item.id,
    title: item.title,
    imageUrl: item.url,
    author: { username: ['小雅', '设计师Lily', 'Momo', '穿搭师Amy', 'StyleBoy', '活力少女'][i % 6], avatar: '' },
    likes: [128, 256, 89, 342, 176, 203][i % 6],
    createdAt: '',
    tags: [item.category],
    height: [280, 220, 260, 240, 300, 200][i % 6],
    color: '#F0F0F0',
    time: ['2h', '5h', '8h', '1d', '1d', '2d'][i % 6],
    isLiked: false,
    isFavorited: false,
  }))
})

const leftDiscover = computed(() => discoverItems.value.filter((_, i) => i % 2 === 0))
const rightDiscover = computed(() => discoverItems.value.filter((_, i) => i % 2 === 1))

// Merge with store data
const allLeft = computed(() => {
  const storeLeft = leftColumn.value
  const discLeft = leftDiscover.value
  return storeLeft.length > 0 ? storeLeft : discLeft
})

const allRight = computed(() => {
  const storeRight = rightColumn.value
  const discRight = rightDiscover.value
  return storeRight.length > 0 ? storeRight : discRight
})

useInfiniteScroll(
  () => document.getElementById('home-scroll-container'),
  () => homeStore.loadMore(),
  { distance: 200 }
)

onMounted(async () => {
  await homeStore.refresh()
})
</script>

<template>
  <div id="home-scroll-container" class="h-full overflow-y-auto">
    <div class="max-w-[1200px] mx-auto px-6 py-5">

      <!-- Top Section: AI Recommendation + Stats -->
      <div class="grid grid-cols-12 gap-5 mb-6">
        <!-- AI Recommendation Banner -->
        <div class="col-span-8">
          <div
            class="relative overflow-hidden rounded-card h-[200px] gradient-instagram cursor-pointer group"
          >
            <div class="absolute inset-0 flex flex-col justify-end p-6">
              <div class="flex items-center gap-1.5 text-white/80 text-xs font-medium mb-1.5">
                <Sparkles :size="12" />
                <span>AI Daily Pick</span>
              </div>
              <div class="text-white text-xl font-semibold">
                {{ recommendations.length ? recommendations[0].title : 'Summer Essentials' }}
              </div>
              <div class="text-white/70 text-sm mt-1.5 max-w-md">
                {{ recommendations.length ? recommendations[0].description : 'Curated by AI based on your style profile' }}
              </div>
            </div>
            <div class="absolute top-6 right-8 w-24 h-24 rounded-full bg-white/10 blur-sm" />
            <div class="absolute top-12 right-20 w-14 h-14 rounded-full bg-white/15 blur-sm" />
            <div class="absolute bottom-4 right-6 opacity-0 group-hover:opacity-100 transition-opacity">
              <span class="text-white/90 text-sm font-medium bg-white/20 backdrop-blur-sm px-4 py-2 rounded-btn">
                View Details →
              </span>
            </div>
          </div>
        </div>

        <!-- Quick Stats -->
        <div class="col-span-4 flex flex-col gap-3">
          <div class="bg-bg-card rounded-card border border-border p-4 flex-1 flex items-center gap-4">
            <div class="w-10 h-10 rounded-btn bg-ig-blue/10 flex items-center justify-center">
              <TrendingUp :size="20" class="text-ig-blue" />
            </div>
            <div>
              <div class="text-[11px] text-text-secondary uppercase tracking-wider">Trending</div>
              <div class="text-lg font-semibold text-text-primary">2.4k</div>
            </div>
          </div>
          <div class="bg-bg-card rounded-card border border-border p-4 flex-1 flex items-center gap-4">
            <div class="w-10 h-10 rounded-btn bg-accent-via/10 flex items-center justify-center">
              <Sparkles :size="20" class="text-accent-via" />
            </div>
            <div>
              <div class="text-[11px] text-text-secondary uppercase tracking-wider">AI Picks</div>
              <div class="text-lg font-semibold text-text-primary">128</div>
            </div>
          </div>
        </div>
      </div>

      <!-- Toolbar -->
      <div class="flex items-center justify-between mb-4">
        <div class="flex items-center gap-2">
          <button
            v-for="cat in categories"
            :key="cat"
            class="px-3.5 py-1.5 rounded-tag text-[13px] font-medium transition-all duration-150"
            :class="activeCategory === cat
              ? 'bg-text-primary text-white'
              : 'bg-bg-card text-text-secondary border border-border hover:text-text-primary hover:border-text-secondary'"
            @click="homeStore.setCategory(cat)"
          >
            {{ cat }}
          </button>
        </div>
        <div class="flex items-center gap-1">
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
            class="w-8 h-8 flex items-center justify-center rounded-btn text-text-secondary hover:bg-bg-secondary transition-colors"
          >
            <Filter :size="16" />
          </button>
        </div>
      </div>

      <!-- Pull-to-refresh indicator -->
      <div v-if="isRefreshing" class="flex justify-center py-4">
        <Loader2 :size="20" class="text-text-secondary animate-spin" />
      </div>

      <!-- Waterfall Feed -->
      <div class="flex gap-5">
        <!-- Left Column -->
        <div class="flex-1 flex flex-col gap-5">
          <div
            v-for="post in allLeft"
            :key="post.id"
            class="bg-bg-card rounded-card overflow-hidden border border-border cursor-pointer hover:shadow-card-hover transition-all duration-200 group"
          >
            <div
              class="w-full flex items-center justify-center overflow-hidden"
              :style="{ height: (post.height || 240) + 'px', backgroundColor: post.color || '#F0F0F0' }"
            >
              <img
                v-if="post.imageUrl"
                :src="post.imageUrl"
                :alt="post.title"
                class="w-full h-full object-cover group-hover:scale-105 transition-transform duration-300"
              />
              <Shirt v-else :size="32" class="text-text-tertiary/40" />
            </div>
            <div class="p-4">
              <h3 class="text-[13px] font-semibold text-text-primary line-clamp-1">{{ post.title }}</h3>
              <div class="flex items-center justify-between mt-2.5">
                <div class="flex items-center gap-2">
                  <div class="w-5 h-5 rounded-full bg-bg-secondary" />
                  <span class="text-[12px] text-text-secondary">@{{ post.author?.username || post.author }} · {{ post.time }}</span>
                </div>
                <div class="flex items-center gap-3">
                  <button
                    class="flex items-center gap-1 transition-colors"
                    :class="post.isLiked ? 'text-error' : 'text-text-tertiary hover:text-text-secondary'"
                    @click.stop="homeStore.toggleLike(post.id)"
                  >
                    <Heart :size="14" :fill="post.isLiked ? 'currentColor' : 'none'" />
                    <span class="text-[12px]">{{ post.likes }}</span>
                  </button>
                  <button
                    class="transition-colors"
                    :class="post.isFavorited ? 'text-accent-via' : 'text-text-tertiary hover:text-text-secondary'"
                    @click.stop="homeStore.toggleFavorite(post.id)"
                  >
                    <Bookmark :size="14" :fill="post.isFavorited ? 'currentColor' : 'none'" />
                  </button>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- Right Column -->
        <div class="flex-1 flex flex-col gap-5">
          <div
            v-for="post in allRight"
            :key="post.id"
            class="bg-bg-card rounded-card overflow-hidden border border-border cursor-pointer hover:shadow-card-hover transition-all duration-200 group"
          >
            <div
              class="w-full flex items-center justify-center overflow-hidden"
              :style="{ height: (post.height || 200) + 'px', backgroundColor: post.color || '#F0F0F0' }"
            >
              <img
                v-if="post.imageUrl"
                :src="post.imageUrl"
                :alt="post.title"
                class="w-full h-full object-cover group-hover:scale-105 transition-transform duration-300"
              />
              <Gem v-else :size="32" class="text-text-tertiary/40" />
            </div>
            <div class="p-4">
              <h3 class="text-[13px] font-semibold text-text-primary line-clamp-1">{{ post.title }}</h3>
              <div class="flex items-center justify-between mt-2.5">
                <div class="flex items-center gap-2">
                  <div class="w-5 h-5 rounded-full bg-bg-secondary" />
                  <span class="text-[12px] text-text-secondary">@{{ post.author?.username || post.author }} · {{ post.time }}</span>
                </div>
                <div class="flex items-center gap-3">
                  <button
                    class="flex items-center gap-1 transition-colors"
                    :class="post.isLiked ? 'text-error' : 'text-text-tertiary hover:text-text-secondary'"
                    @click.stop="homeStore.toggleLike(post.id)"
                  >
                    <Heart :size="14" :fill="post.isLiked ? 'currentColor' : 'none'" />
                    <span class="text-[12px]">{{ post.likes }}</span>
                  </button>
                  <button
                    class="transition-colors"
                    :class="post.isFavorited ? 'text-accent-via' : 'text-text-tertiary hover:text-text-secondary'"
                    @click.stop="homeStore.toggleFavorite(post.id)"
                  >
                    <Bookmark :size="14" :fill="post.isFavorited ? 'currentColor' : 'none'" />
                  </button>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- Loading more indicator -->
      <div v-if="isLoading && !isRefreshing" class="flex justify-center py-8">
        <Loader2 :size="20" class="text-text-secondary animate-spin" />
      </div>

      <!-- No more data -->
      <div v-if="!hasMore && !isLoading" class="text-center py-8">
        <span class="text-[12px] text-text-tertiary">— End of feed —</span>
      </div>
    </div>
  </div>
</template>
