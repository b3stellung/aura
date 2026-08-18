<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { storeToRefs } from 'pinia'
import {
  Bookmark,
  ChevronDown,
  Heart,
  Loader2,
  RefreshCw,
  Shirt,
  Sparkles,
  SunMedium,
  WandSparkles,
} from 'lucide-vue-next'
import imagesData from '@/assets/images.json'
import { useHomeStore, type PostItem } from '@/stores/home'

type Reason = {
  title: string
  summary: string
  detail: string
}

type OutfitPiece = {
  label: string
  name: string
  imageUrl: string
  note: string
}

type Scene = {
  id: string
  label: string
  title: string
  copy: string
}

const homeStore = useHomeStore()
const {
  recommendations,
  activeCategory,
  categories,
  filteredPosts,
  isLoading,
  isRefreshing,
  hasMore,
  isUsingExampleContent,
} = storeToRefs(homeStore)

const activeReason = ref(0)
const activeScene = ref('通勤')
const localOutfitIndex = ref(0)

const localOutfits = imagesData.outfits
const clothing = imagesData.clothing

const fallbackPost = computed<PostItem>(() => ({
  id: 'local-today-outfit',
  title: '清爽白衬衫与直筒牛仔',
  imageUrl: activeLocalOutfit.value.url,
  author: { username: 'Aura 示例', avatar: '' },
  likes: 128,
  createdAt: '',
  tags: ['通勤', '休闲'],
  height: 320,
  color: '#F3EEE8',
  time: '示例',
  isLiked: false,
  isFavorited: false,
}))

const activeLocalOutfit = computed(() => localOutfits[localOutfitIndex.value % localOutfits.length])

const todayPost = computed(() => filteredPosts.value[0] || fallbackPost.value)

const todayImage = computed(() => todayPost.value.imageUrl || activeLocalOutfit.value.url || imagesData.placeholders.outfit)

const todayTitle = computed(() => {
  const apiTitle = recommendations.value[0]?.title || todayPost.value.title
  return apiTitle || '今日穿搭：清爽通勤，晚上也不突兀'
})

const todayDescription = computed(() => {
  return recommendations.value[0]?.description || '适合 26℃ 微风天气，从办公桌到晚间小聚都保持轻盈利落。'
})

const weatherSummary = computed(() => ({
  weather: '26℃ · 微风',
  occasion: activeCategory.value === '全部' ? '通勤 / 轻社交' : activeCategory.value,
  style: todayPost.value.tags?.slice(0, 2).join(' · ') || '清爽 · 简约',
}))

const reasons: Reason[] = [
  {
    title: '适配今天的体感',
    summary: '轻薄上装与可叠穿外层，应对空调房和傍晚温差。',
    detail: 'Aura 将天气设为第一层约束：不会只追求造型感，而是优先保证体感舒适、活动方便。',
  },
  {
    title: '覆盖你的主要日程',
    summary: '办公、咖啡会面、晚间散步都不需要大幅换装。',
    detail: '核心单品保持干净线条，配饰和鞋履负责提升精致度，所以场景切换时只要微调外层即可。',
  },
  {
    title: '延续你的风格偏好',
    summary: '低饱和配色、直线剪裁和少量金属点缀，稳定但不沉闷。',
    detail: '如果你更想要松弛或更正式，可以让 Aura 在不推翻整套逻辑的前提下换一套。',
  },
]

const outfitPieces = computed<OutfitPiece[]>(() => [
  {
    label: '上装',
    name: clothing.tops[0]?.name || 'White Linen Shirt',
    imageUrl: clothing.tops[0]?.url || imagesData.placeholders.clothing,
    note: '透气、提亮脸部，是整套的清爽基底。',
  },
  {
    label: '下装',
    name: clothing.bottoms[0]?.name || 'Blue Straight Jeans',
    imageUrl: clothing.bottoms[0]?.url || imagesData.placeholders.clothing,
    note: '直筒线条降低随意感，走路和久坐都舒服。',
  },
  {
    label: '鞋履',
    name: clothing.shoes[0]?.name || 'White Sneakers',
    imageUrl: clothing.shoes[0]?.url || imagesData.placeholders.clothing,
    note: '白色呼应上装，让比例更轻。',
  },
  {
    label: '配饰',
    name: clothing.accessories[0]?.name || 'Gold Necklace',
    imageUrl: clothing.accessories[0]?.url || imagesData.placeholders.clothing,
    note: '一点金属光泽，让简约搭配有记忆点。',
  },
])

const scenes: Scene[] = [
  {
    id: '通勤',
    label: '通勤',
    title: '加一件浅色外套',
    copy: '保持干净职业感，早晚温差也能稳住。',
  },
  {
    id: '约会',
    label: '约会',
    title: '换成细项链与乐福鞋',
    copy: '把休闲感往精致方向推一点，不显用力。',
  },
  {
    id: '休闲',
    label: '休闲',
    title: '卷袖口，改斜挎包',
    copy: '让整体更松弛，适合周末散步或看展。',
  },
  {
    id: '派对',
    label: '派对',
    title: '叠一件短夹克',
    copy: '增加层次和轮廓，适合晚间灯光环境。',
  },
]

const activeSceneCopy = computed(() => scenes.find((scene) => scene.id === activeScene.value) || scenes[0])

function viewOutfit() {
  if (todayPost.value.id !== fallbackPost.value.id) {
    void homeStore.toggleFavorite(todayPost.value.id)
  }
  document.getElementById('outfit-breakdown')?.scrollIntoView({ behavior: 'smooth', block: 'start' })
}

async function shuffleOutfit() {
  localOutfitIndex.value += 1
  await homeStore.refresh()
}

function selectCategory(category: string) {
  homeStore.setCategory(category)
}

onMounted(async () => {
  await homeStore.refresh()
})
</script>

<template>
  <main class="min-h-full overflow-y-auto bg-[#f7f3ee] text-[#241d18]">
    <div class="mx-auto max-w-[1180px] px-4 py-5 sm:px-6 lg:px-8">
      <div
        v-if="isUsingExampleContent"
        class="mb-4 rounded-2xl border border-amber-200/70 bg-amber-50/80 px-4 py-3 text-xs leading-5 text-amber-800"
      >
        示例内容提示：推荐服务暂时不可用，当前展示的是本地示例内容，不代表真实用户数据。
      </div>

      <section class="fade-in grid min-h-[620px] gap-5 lg:grid-cols-[minmax(0,1.1fr)_minmax(360px,0.9fr)]">
        <div class="relative overflow-hidden rounded-[2rem] bg-[#e8ded2] shadow-sm">
          <img
            :src="todayImage"
            :alt="todayTitle"
            class="h-[520px] w-full object-cover sm:h-[660px]"
          />
          <div class="absolute inset-x-0 bottom-0 bg-gradient-to-t from-black/65 via-black/20 to-transparent p-6 text-white sm:p-8">
            <div class="mb-3 inline-flex items-center gap-2 rounded-full bg-white/18 px-3 py-1.5 text-xs font-medium backdrop-blur">
              <Sparkles :size="14" />
              今日穿搭
            </div>
            <h1 class="max-w-xl text-3xl font-semibold tracking-tight sm:text-5xl">
              {{ todayTitle }}
            </h1>
            <p class="mt-3 max-w-lg text-sm leading-6 text-white/82 sm:text-base">
              {{ todayDescription }}
            </p>
          </div>
        </div>

        <aside class="flex flex-col justify-between rounded-[2rem] border border-white/70 bg-white/75 p-5 shadow-sm backdrop-blur sm:p-7">
          <div>
            <div class="flex items-center justify-between gap-3">
              <div>
                <p class="text-xs font-semibold uppercase tracking-[0.24em] text-[#a0744f]">Aura Daily</p>
                <h2 class="mt-2 text-2xl font-semibold">今天这样穿</h2>
              </div>
              <button
                class="inline-flex h-10 w-10 items-center justify-center rounded-full bg-[#241d18] text-white transition hover:-translate-y-0.5 hover:bg-[#5c4032]"
                :disabled="isRefreshing"
                aria-label="刷新今日穿搭"
                @click="shuffleOutfit"
              >
                <Loader2 v-if="isRefreshing" :size="17" class="animate-spin" />
                <RefreshCw v-else :size="17" />
              </button>
            </div>

            <div class="mt-8 grid gap-3">
              <div class="rounded-3xl bg-[#f7f3ee] p-4">
                <div class="flex items-center gap-2 text-xs font-medium text-[#9b7656]">
                  <SunMedium :size="15" />
                  天气
                </div>
                <p class="mt-1 text-lg font-semibold">{{ weatherSummary.weather }}</p>
              </div>
              <div class="grid grid-cols-2 gap-3">
                <div class="rounded-3xl bg-[#f7f3ee] p-4">
                  <p class="text-xs font-medium text-[#9b7656]">场合</p>
                  <p class="mt-1 font-semibold">{{ weatherSummary.occasion }}</p>
                </div>
                <div class="rounded-3xl bg-[#f7f3ee] p-4">
                  <p class="text-xs font-medium text-[#9b7656]">风格</p>
                  <p class="mt-1 font-semibold">{{ weatherSummary.style }}</p>
                </div>
              </div>
            </div>
          </div>

          <div class="mt-8 space-y-3">
            <button
              class="flex w-full items-center justify-center gap-2 rounded-full bg-[#241d18] px-5 py-3.5 text-sm font-semibold text-white transition hover:-translate-y-0.5 hover:bg-[#5c4032]"
              @click="viewOutfit"
            >
              查看搭配
              <Bookmark :size="16" :fill="todayPost.isFavorited ? 'currentColor' : 'none'" />
            </button>
            <button
              class="flex w-full items-center justify-center gap-2 rounded-full border border-[#d9c8b8] bg-white/60 px-5 py-3.5 text-sm font-semibold text-[#5c4032] transition hover:-translate-y-0.5 hover:bg-white"
              :disabled="isRefreshing"
              @click="shuffleOutfit"
            >
              让 Aura 换一套
              <WandSparkles :size="16" />
            </button>
          </div>
        </aside>
      </section>

      <section class="fade-in mt-6 rounded-[2rem] border border-white/70 bg-white/70 p-5 shadow-sm sm:p-7">
        <div class="flex flex-col justify-between gap-4 sm:flex-row sm:items-end">
          <div>
            <p class="text-xs font-semibold uppercase tracking-[0.24em] text-[#a0744f]">Why it works</p>
            <h2 class="mt-2 text-2xl font-semibold">为什么适合你</h2>
          </div>
          <div class="flex flex-wrap gap-2">
            <button
              v-for="category in categories"
              :key="category"
              class="rounded-full px-3.5 py-2 text-xs font-semibold transition"
              :class="activeCategory === category ? 'bg-[#241d18] text-white' : 'bg-[#f2ebe4] text-[#6f5a4a] hover:bg-[#e8ded2]'"
              @click="selectCategory(category)"
            >
              {{ category }}
            </button>
          </div>
        </div>

        <div class="mt-5 grid gap-3 md:grid-cols-3">
          <article
            v-for="(reason, index) in reasons"
            :key="reason.title"
            class="rounded-3xl border border-[#eadfd4] bg-[#fbf8f4] p-5 transition hover:-translate-y-0.5 hover:shadow-sm"
          >
            <button class="flex w-full items-start justify-between gap-4 text-left" @click="activeReason = activeReason === index ? -1 : index">
              <span>
                <span class="text-xs font-semibold text-[#a0744f]">0{{ index + 1 }}</span>
                <h3 class="mt-2 font-semibold">{{ reason.title }}</h3>
                <p class="mt-2 text-sm leading-6 text-[#756457]">{{ reason.summary }}</p>
              </span>
              <ChevronDown
                :size="18"
                class="mt-6 shrink-0 text-[#a0744f] transition"
                :class="{ 'rotate-180': activeReason === index }"
              />
            </button>
            <p v-if="activeReason === index" class="mt-4 border-t border-[#eadfd4] pt-4 text-sm leading-6 text-[#5f4f44]">
              {{ reason.detail }}
            </p>
          </article>
        </div>
      </section>

      <section id="outfit-breakdown" class="fade-in mt-6 grid gap-6 lg:grid-cols-[0.95fr_1.05fr]">
        <div class="rounded-[2rem] border border-white/70 bg-white/70 p-5 shadow-sm sm:p-7">
          <p class="text-xs font-semibold uppercase tracking-[0.24em] text-[#a0744f]">Breakdown</p>
          <h2 class="mt-2 text-2xl font-semibold">搭配拆解</h2>
          <div class="mt-5 grid grid-cols-2 gap-3">
            <article
              v-for="piece in outfitPieces"
              :key="piece.label"
              class="overflow-hidden rounded-3xl bg-[#fbf8f4]"
            >
              <img :src="piece.imageUrl" :alt="piece.name" class="h-36 w-full object-cover" />
              <div class="p-4">
                <p class="text-xs font-semibold text-[#a0744f]">{{ piece.label }}</p>
                <h3 class="mt-1 text-sm font-semibold">{{ piece.name }}</h3>
                <p class="mt-2 text-xs leading-5 text-[#756457]">{{ piece.note }}</p>
              </div>
            </article>
          </div>
        </div>

        <div class="rounded-[2rem] border border-white/70 bg-white/70 p-5 shadow-sm sm:p-7">
          <div class="flex items-center justify-between gap-4">
            <div>
              <p class="text-xs font-semibold uppercase tracking-[0.24em] text-[#a0744f]">Scenes</p>
              <h2 class="mt-2 text-2xl font-semibold">场景切换</h2>
            </div>
            <button
              v-if="hasMore"
              class="rounded-full bg-[#f2ebe4] px-4 py-2 text-xs font-semibold text-[#6f5a4a] transition hover:bg-[#e8ded2]"
              :disabled="isLoading"
              @click="homeStore.loadMore()"
            >
              加载更多灵感
            </button>
          </div>

          <div class="mt-5 flex gap-2 overflow-x-auto pb-1">
            <button
              v-for="scene in scenes"
              :key="scene.id"
              class="shrink-0 rounded-full px-4 py-2 text-sm font-semibold transition"
              :class="activeScene === scene.id ? 'bg-[#241d18] text-white' : 'bg-[#f2ebe4] text-[#6f5a4a] hover:bg-[#e8ded2]'"
              @click="activeScene = scene.id"
            >
              {{ scene.label }}
            </button>
          </div>

          <div class="mt-5 rounded-3xl bg-[#fbf8f4] p-5">
            <h3 class="text-xl font-semibold">{{ activeSceneCopy.title }}</h3>
            <p class="mt-2 text-sm leading-6 text-[#756457]">{{ activeSceneCopy.copy }}</p>
            <div class="mt-5 flex items-center gap-4 text-xs text-[#8b7868]">
              <button
                class="inline-flex items-center gap-1.5 transition hover:text-[#241d18]"
                :class="{ 'text-rose-500': todayPost.isLiked }"
                @click="homeStore.toggleLike(todayPost.id)"
              >
                <Heart :size="15" :fill="todayPost.isLiked ? 'currentColor' : 'none'" />
                {{ todayPost.likes }} 人觉得合适
              </button>
              <span v-if="isLoading" class="inline-flex items-center gap-1.5">
                <Loader2 :size="14" class="animate-spin" />
                加载中
              </span>
            </div>
          </div>

          <div class="mt-5 rounded-[1.75rem] bg-[#241d18] p-5 text-white">
            <div class="flex items-start gap-3">
              <span class="mt-0.5 grid h-10 w-10 shrink-0 place-items-center rounded-2xl bg-white/12">
                <Shirt :size="18" />
              </span>
              <div>
                <h3 class="font-semibold">AI 调整</h3>
                <p class="mt-2 text-sm leading-6 text-white/72">
                  想更显高、换成雨天版本，或把预算控制在已有衣橱里？告诉 Aura 一个条件，它会基于这套逻辑微调。
                </p>
                <button
                  class="mt-4 rounded-full bg-white px-4 py-2 text-xs font-semibold text-[#241d18] transition hover:-translate-y-0.5 hover:bg-[#f4eee8]"
                  @click="shuffleOutfit"
                >
                  让 Aura 重新调整
                </button>
              </div>
            </div>
          </div>
        </div>
      </section>
    </div>
  </main>
</template>

<style scoped>
.fade-in {
  animation: aura-home-fade 560ms ease both;
}

@keyframes aura-home-fade {
  from {
    opacity: 0;
    transform: translateY(14px);
  }

  to {
    opacity: 1;
    transform: translateY(0);
  }
}
</style>
