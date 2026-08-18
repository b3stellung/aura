<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import {
  Settings,
  Share2,
  Grid3x3,
  Heart,
  Bookmark,
  LogOut,
  X,
  Loader2,
  UserCircle,
  Sparkles,
  Palette,
  Camera,
  Edit3,
  TrendingUp,
  Award,
} from 'lucide-vue-next'
import { useAuthStore } from '@/stores/auth'
import { storeToRefs } from 'pinia'
import imagesData from '@/assets/images.json'

const router = useRouter()
const authStore = useAuthStore()
const {
  user,
  isLoggedIn,
  styleProfile,
  isLoading,
  error: authError,
  showLoginModal,
  showRegisterModal,
} = storeToRefs(authStore)

const activeOutfitTab = ref<'grid' | 'liked' | 'saved'>('grid')

// Login form
const loginForm = ref({ username: '', password: '' })

// Register form
const registerForm = ref({ username: '', password: '', nickname: '' })

// Edit profile form
const showEditProfile = ref(false)
const editForm = ref({ nickname: '', bio: '' })

// Style dimensions
const dimensions = computed(() => {
  if (!styleProfile.value) return []
  return [
    { label: 'Minimalist', value: styleProfile.value.dimensions.minimalist, color: '#F58529' },
    { label: 'Elegant', value: styleProfile.value.dimensions.elegant, color: '#DD2A7B' },
    { label: 'Casual', value: styleProfile.value.dimensions.casual, color: '#8134AF' },
    { label: 'Trendy', value: styleProfile.value.dimensions.trendy, color: '#5B51D8' },
  ]
})

// Use real images for outfit grid
const userOutfits = computed(() => {
  return imagesData.outfits.map((o) => ({
    id: o.id,
    url: o.url,
    name: o.name,
    occasion: o.occasion,
  }))
})

const avatarUrl = computed(() => {
  if (user.value.avatar) return user.value.avatar
  return imagesData.avatars[0].url
})

onMounted(() => {
  if (isLoggedIn.value) {
    authStore.fetchCurrentUser()
  }
})

async function handleLogin() {
  if (!loginForm.value.username.trim() || !loginForm.value.password.trim()) return
  try {
    await authStore.login(loginForm.value)
    loginForm.value = { username: '', password: '' }
  } catch {}
}

async function handleRegister() {
  if (!registerForm.value.username.trim() || !registerForm.value.password.trim() || !registerForm.value.nickname.trim()) return
  try {
    await authStore.register(registerForm.value)
    registerForm.value = { username: '', password: '', nickname: '' }
  } catch {}
}

function openEditProfile() {
  editForm.value = {
    nickname: user.value.nickname,
    bio: user.value.bio ?? '',
  }
  showEditProfile.value = true
}

async function saveProfile() {
  await authStore.updateProfile({
    nickname: editForm.value.nickname,
    bio: editForm.value.bio,
  })
  showEditProfile.value = false
}

function handleLogout() {
  authStore.logout()
}
</script>

<template>
  <div class="h-full overflow-y-auto">
    <!-- Not Logged In State -->
    <div v-if="!isLoggedIn" class="flex items-center justify-center h-full">
      <div class="w-full max-w-sm text-center px-6">
        <div class="w-20 h-20 mx-auto rounded-2xl gradient-instagram p-[2px] mb-6">
          <div class="w-full h-full rounded-2xl bg-bg-card flex items-center justify-center">
            <UserCircle :size="36" class="text-text-tertiary" />
          </div>
        </div>
        <h2 class="text-xl font-semibold text-text-primary">Welcome to Aura</h2>
        <p class="text-sm text-text-secondary mt-2">Sign in to unlock all features</p>

        <div class="mt-8 space-y-3">
          <div class="space-y-3">
            <div>
              <input
                v-model="loginForm.username"
                type="text"
                placeholder="Username"
                class="w-full h-10 px-4 bg-bg-card border border-border rounded-card text-sm focus:outline-none focus:border-ig-blue transition-colors"
              />
            </div>
            <div>
              <input
                v-model="loginForm.password"
                type="password"
                placeholder="Password"
                class="w-full h-10 px-4 bg-bg-card border border-border rounded-card text-sm focus:outline-none focus:border-ig-blue transition-colors"
                @keyup.enter="handleLogin"
              />
            </div>
          </div>

          <div v-if="authError" class="p-2.5 bg-error/10 rounded-card text-[12px] text-error text-center">
            {{ authError }}
          </div>

          <button
            class="w-full h-10 rounded-card text-sm font-medium text-white transition-colors flex items-center justify-center gap-2"
            :class="loginForm.username.trim() && loginForm.password.trim()
              ? 'bg-ig-blue hover:bg-ig-blue-hover'
              : 'bg-bg-secondary text-text-tertiary cursor-not-allowed'"
            :disabled="isLoading || !loginForm.username.trim() || !loginForm.password.trim()"
            @click="handleLogin"
          >
            <Loader2 v-if="isLoading" :size="16" class="animate-spin" />
            {{ isLoading ? 'Signing in...' : 'Log In' }}
          </button>

          <div class="flex items-center gap-3 my-4">
            <div class="flex-1 h-px bg-border" />
            <span class="text-[11px] text-text-tertiary">or</span>
            <div class="flex-1 h-px bg-border" />
          </div>

          <button
            class="w-full h-10 rounded-card text-sm font-medium bg-bg-card border border-border text-text-primary hover:bg-bg-secondary transition-colors"
            @click="router.push('/register')"
          >
            Create Account
          </button>
        </div>
      </div>
    </div>

    <!-- Logged In Content -->
    <div v-else class="max-w-[1100px] mx-auto px-6 py-5">
      <!-- Profile Header Card -->
      <div class="bg-bg-card rounded-card border border-border overflow-hidden mb-5">
        <!-- Cover gradient -->
        <div class="h-28 gradient-instagram relative">
          <div class="absolute top-3 right-3 flex gap-2">
            <button
              class="w-8 h-8 bg-white/20 backdrop-blur-sm rounded-btn flex items-center justify-center hover:bg-white/30 transition-colors"
            >
              <Share2 :size="14" class="text-white" />
            </button>
            <button
              class="w-8 h-8 bg-white/20 backdrop-blur-sm rounded-btn flex items-center justify-center hover:bg-white/30 transition-colors"
            >
              <Settings :size="14" class="text-white" />
            </button>
          </div>
        </div>

        <div class="px-6 pb-6">
          <!-- Avatar -->
          <div class="-mt-10 mb-4 flex items-end justify-between">
            <div class="w-20 h-20 rounded-full gradient-instagram p-[2px] shadow-lg">
              <div class="w-full h-full rounded-full bg-bg-card flex items-center justify-center overflow-hidden">
                <img
                  :src="avatarUrl"
                  :alt="user.nickname"
                  class="w-full h-full object-cover"
                />
              </div>
            </div>
            <div class="flex gap-2">
              <button
                class="h-8 px-4 flex items-center gap-1.5 bg-bg-secondary rounded-btn text-[12px] font-medium text-text-primary hover:bg-bg-hover transition-colors"
                @click="openEditProfile"
              >
                <Edit3 :size="13" />
                Edit Profile
              </button>
              <button
                class="h-8 px-4 flex items-center gap-1.5 bg-bg-secondary rounded-btn text-[12px] font-medium text-text-primary hover:bg-bg-hover transition-colors"
              >
                <Palette :size="13" />
                Style Profile
              </button>
            </div>
          </div>

          <!-- User Info -->
          <div class="flex items-start justify-between">
            <div>
              <h2 class="text-lg font-semibold text-text-primary">{{ user.nickname }}</h2>
              <p class="text-[13px] text-text-secondary mt-0.5">@{{ user.username }}</p>
              <p class="text-[13px] text-text-secondary mt-1.5 max-w-md">{{ user.bio }}</p>
            </div>
            <button
              class="flex items-center gap-1.5 text-[12px] text-text-secondary hover:text-error transition-colors"
              @click="handleLogout"
            >
              <LogOut :size="13" />
              Log Out
            </button>
          </div>

          <!-- Stats -->
          <div class="flex items-center gap-8 mt-4 pt-4 border-t border-border-light">
            <div class="flex items-center gap-2">
              <TrendingUp :size="14" class="text-text-tertiary" />
              <span class="text-[13px] font-semibold text-text-primary">{{ user.following }}</span>
              <span class="text-[12px] text-text-secondary">Following</span>
            </div>
            <div class="flex items-center gap-2">
              <Heart :size="14" class="text-text-tertiary" />
              <span class="text-[13px] font-semibold text-text-primary">{{ user.followers }}</span>
              <span class="text-[12px] text-text-secondary">Followers</span>
            </div>
            <div class="flex items-center gap-2">
              <Sparkles :size="14" class="text-text-tertiary" />
              <span class="text-[13px] font-semibold text-text-primary">{{ user.outfits }}</span>
              <span class="text-[12px] text-text-secondary">Outfits</span>
            </div>
          </div>
        </div>
      </div>

      <!-- Two Column Layout -->
      <div class="grid grid-cols-12 gap-5">
        <!-- Left: Style Profile -->
        <div class="col-span-4">
          <div v-if="styleProfile" class="bg-bg-card rounded-card border border-border p-5">
            <div class="flex items-center gap-2 mb-5">
              <Palette :size="16" class="text-text-secondary" />
              <h3 class="text-[13px] font-semibold text-text-primary">Style Profile</h3>
            </div>

            <!-- Style Radar -->
            <div class="flex items-center justify-center mb-5">
              <div class="relative w-40 h-40">
                <div class="absolute inset-0 rounded-full border border-border/30" />
                <div class="absolute inset-4 rounded-full border border-border/30" />
                <div class="absolute inset-8 rounded-full border border-border/30" />
                <div class="absolute inset-12 rounded-full border border-border/30" />
                <div class="absolute top-1/2 left-1/2 w-2 h-2 -translate-x-1/2 -translate-y-1/2 rounded-full gradient-instagram" />
                <span class="absolute top-0 left-1/2 -translate-x-1/2 -translate-y-2 text-[10px] text-text-secondary">Minimal</span>
                <span class="absolute bottom-0 left-1/2 -translate-x-1/2 translate-y-2 text-[10px] text-text-secondary">Casual</span>
                <span class="absolute left-0 top-1/2 -translate-y-1/2 -translate-x-2 text-[10px] text-text-secondary">Elegant</span>
                <span class="absolute right-0 top-1/2 -translate-y-1/2 translate-x-2 text-[10px] text-text-secondary">Trendy</span>
                <svg class="absolute inset-0 w-full h-full" viewBox="0 0 100 100">
                  <polygon
                    points="50,10 85,50 50,80 15,50"
                    fill="url(#radarGradient)"
                    stroke="none"
                    opacity="0.3"
                  />
                  <polygon
                    points="50,10 85,50 50,80 15,50"
                    fill="none"
                    stroke="url(#radarStroke)"
                    stroke-width="1.5"
                  />
                  <defs>
                    <linearGradient id="radarGradient" x1="0%" y1="0%" x2="100%" y2="100%">
                      <stop offset="0%" stop-color="#F58529" />
                      <stop offset="50%" stop-color="#DD2A7B" />
                      <stop offset="100%" stop-color="#8134AF" />
                    </linearGradient>
                    <linearGradient id="radarStroke" x1="0%" y1="0%" x2="100%" y2="100%">
                      <stop offset="0%" stop-color="#F58529" />
                      <stop offset="50%" stop-color="#DD2A7B" />
                      <stop offset="100%" stop-color="#8134AF" />
                    </linearGradient>
                  </defs>
                </svg>
              </div>
            </div>

            <!-- Style Bars -->
            <div class="space-y-3">
              <div v-for="dim in dimensions" :key="dim.label" class="flex items-center gap-3">
                <span class="text-[11px] text-text-secondary w-16 text-right">{{ dim.label }}</span>
                <div class="flex-1 h-1.5 bg-bg-secondary rounded-full overflow-hidden">
                  <div
                    class="h-full rounded-full transition-all duration-500"
                    :style="{ width: dim.value + '%', backgroundColor: dim.color }"
                  />
                </div>
                <span class="text-[11px] text-text-secondary w-8 text-right">{{ dim.value }}%</span>
              </div>
            </div>

            <!-- Main Style -->
            <div class="mt-5 pt-4 border-t border-border-light">
              <div class="flex items-center justify-between mb-3">
                <span class="text-[12px] text-text-secondary">Main Style</span>
                <span class="text-[13px] font-semibold text-text-primary">{{ styleProfile.mainStyle }}</span>
              </div>
              <div class="flex items-center justify-between">
                <span class="text-[12px] text-text-secondary">Preferred Colors</span>
                <div class="flex gap-1.5">
                  <div
                    v-for="(color, i) in styleProfile.preferredColors"
                    :key="i"
                    class="w-5 h-5 rounded-full border border-border"
                    :style="{ backgroundColor: color }"
                  />
                </div>
              </div>
            </div>
          </div>

          <!-- Quick Stats Card -->
          <div class="bg-bg-card rounded-card border border-border p-5 mt-5">
            <div class="flex items-center gap-2 mb-4">
              <Award :size="16" class="text-text-secondary" />
              <h3 class="text-[13px] font-semibold text-text-primary">Achievements</h3>
            </div>
            <div class="grid grid-cols-2 gap-3">
              <div class="bg-bg-secondary rounded-card p-3 text-center">
                <div class="text-lg font-bold text-text-primary">12</div>
                <div class="text-[11px] text-text-secondary">Days Active</div>
              </div>
              <div class="bg-bg-secondary rounded-card p-3 text-center">
                <div class="text-lg font-bold text-text-primary">64</div>
                <div class="text-[11px] text-text-secondary">Items Added</div>
              </div>
              <div class="bg-bg-secondary rounded-card p-3 text-center">
                <div class="text-lg font-bold text-text-primary">28</div>
                <div class="text-[11px] text-text-secondary">Outfits Created</div>
              </div>
              <div class="bg-bg-secondary rounded-card p-3 text-center">
                <div class="text-lg font-bold text-text-primary">5</div>
                <div class="text-[11px] text-text-secondary">AI Sessions</div>
              </div>
            </div>
          </div>
        </div>

        <!-- Right: Outfits Grid -->
        <div class="col-span-8">
          <div class="bg-bg-card rounded-card border border-border overflow-hidden">
            <!-- Tab Bar -->
            <div class="flex items-center justify-between px-5 py-3 border-b border-border">
              <div class="flex items-center gap-1">
                <button
                  class="px-3 py-1.5 rounded-btn text-[12px] font-medium transition-colors"
                  :class="activeOutfitTab === 'grid' ? 'bg-bg-secondary text-text-primary' : 'text-text-secondary hover:text-text-primary'"
                  @click="activeOutfitTab = 'grid'"
                >
                  <Grid3x3 :size="14" class="inline mr-1.5" />
                  My Outfits
                </button>
                <button
                  class="px-3 py-1.5 rounded-btn text-[12px] font-medium transition-colors"
                  :class="activeOutfitTab === 'liked' ? 'bg-bg-secondary text-text-primary' : 'text-text-secondary hover:text-text-primary'"
                  @click="activeOutfitTab = 'liked'"
                >
                  <Heart :size="14" class="inline mr-1.5" />
                  Liked
                </button>
                <button
                  class="px-3 py-1.5 rounded-btn text-[12px] font-medium transition-colors"
                  :class="activeOutfitTab === 'saved' ? 'bg-bg-secondary text-text-primary' : 'text-text-secondary hover:text-text-primary'"
                  @click="activeOutfitTab = 'saved'"
                >
                  <Bookmark :size="14" class="inline mr-1.5" />
                  Saved
                </button>
              </div>
              <button
                class="h-8 px-3 flex items-center gap-1.5 bg-ig-blue text-white text-[12px] font-medium rounded-btn hover:bg-ig-blue-hover transition-colors"
              >
                <Camera :size="13" />
                Add Outfit
              </button>
            </div>

            <!-- Outfit Grid -->
            <div class="p-5">
              <div class="grid grid-cols-4 gap-4">
                <div
                  v-for="outfit in userOutfits"
                  :key="outfit.id"
                  class="aspect-[3/4] rounded-card overflow-hidden border border-border cursor-pointer hover:shadow-card-hover transition-all duration-200 group relative"
                >
                  <img
                    :src="outfit.url"
                    :alt="outfit.name"
                    class="w-full h-full object-cover group-hover:scale-105 transition-transform duration-300"
                  />
                  <div class="absolute inset-0 bg-gradient-to-t from-black/50 to-transparent opacity-0 group-hover:opacity-100 transition-opacity">
                    <div class="absolute bottom-3 left-3 right-3">
                      <div class="text-[12px] font-medium text-white">{{ outfit.name }}</div>
                      <div class="text-[10px] text-white/70 capitalize">{{ outfit.occasion }}</div>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Login Modal -->
    <Teleport to="body">
      <Transition name="fade">
        <div
          v-if="showLoginModal"
          class="fixed inset-0 z-[100] bg-black/40 flex items-center justify-center"
          @click.self="authStore.closeModals()"
        >
          <div class="w-[400px] bg-bg-card rounded-card border border-border shadow-dropdown p-6">
            <div class="flex items-center justify-between mb-5">
              <h2 class="text-[15px] font-semibold text-text-primary">Log In</h2>
              <button
                class="w-7 h-7 flex items-center justify-center rounded-btn hover:bg-bg-secondary transition-colors"
                @click="authStore.closeModals()"
              >
                <X :size="16" class="text-text-secondary" />
              </button>
            </div>

            <div v-if="authError" class="mb-4 p-2.5 bg-error/10 rounded-card text-[12px] text-error">
              {{ authError }}
            </div>

            <div class="space-y-3">
              <div>
                <label class="text-[12px] text-text-secondary mb-1.5 block font-medium">Username</label>
                <input
                  v-model="loginForm.username"
                  type="text"
                  placeholder="Enter username"
                  class="w-full h-9 px-3 bg-bg-secondary border border-transparent focus:border-ig-blue rounded-btn text-[13px] outline-none transition-colors"
                  @keyup.enter="handleLogin"
                />
              </div>
              <div>
                <label class="text-[12px] text-text-secondary mb-1.5 block font-medium">Password</label>
                <input
                  v-model="loginForm.password"
                  type="password"
                  placeholder="Enter password"
                  class="w-full h-9 px-3 bg-bg-secondary border border-transparent focus:border-ig-blue rounded-btn text-[13px] outline-none transition-colors"
                  @keyup.enter="handleLogin"
                />
              </div>
            </div>

            <button
              class="w-full h-9 mt-5 rounded-btn text-[13px] font-medium text-white bg-ig-blue hover:bg-ig-blue-hover flex items-center justify-center gap-2 transition-colors"
              :disabled="isLoading"
              @click="handleLogin"
            >
              <Loader2 v-if="isLoading" :size="14" class="animate-spin" />
              {{ isLoading ? 'Signing in...' : 'Log In' }}
            </button>

            <div class="mt-3 text-center">
              <span class="text-[12px] text-text-secondary">Don't have an account?</span>
              <button
                class="text-[12px] text-ig-blue font-medium ml-1 hover:underline"
                @click="authStore.openRegister()"
              >
                Register
              </button>
            </div>
          </div>
        </div>
      </Transition>
    </Teleport>

    <!-- Register Modal -->
    <Teleport to="body">
      <Transition name="fade">
        <div
          v-if="showRegisterModal"
          class="fixed inset-0 z-[100] bg-black/40 flex items-center justify-center"
          @click.self="authStore.closeModals()"
        >
          <div class="w-[400px] bg-bg-card rounded-card border border-border shadow-dropdown p-6">
            <div class="flex items-center justify-between mb-5">
              <h2 class="text-[15px] font-semibold text-text-primary">Register</h2>
              <button
                class="w-7 h-7 flex items-center justify-center rounded-btn hover:bg-bg-secondary transition-colors"
                @click="authStore.closeModals()"
              >
                <X :size="16" class="text-text-secondary" />
              </button>
            </div>

            <div v-if="authError" class="mb-4 p-2.5 bg-error/10 rounded-card text-[12px] text-error">
              {{ authError }}
            </div>

            <div class="space-y-3">
              <div>
                <label class="text-[12px] text-text-secondary mb-1.5 block font-medium">Nickname</label>
                <input
                  v-model="registerForm.nickname"
                  type="text"
                  placeholder="Choose a display name"
                  class="w-full h-9 px-3 bg-bg-secondary border border-transparent focus:border-ig-blue rounded-btn text-[13px] outline-none transition-colors"
                />
              </div>
              <div>
                <label class="text-[12px] text-text-secondary mb-1.5 block font-medium">Username</label>
                <input
                  v-model="registerForm.username"
                  type="text"
                  placeholder="For login"
                  class="w-full h-9 px-3 bg-bg-secondary border border-transparent focus:border-ig-blue rounded-btn text-[13px] outline-none transition-colors"
                />
              </div>
              <div>
                <label class="text-[12px] text-text-secondary mb-1.5 block font-medium">Password</label>
                <input
                  v-model="registerForm.password"
                  type="password"
                  placeholder="At least 6 characters"
                  class="w-full h-9 px-3 bg-bg-secondary border border-transparent focus:border-ig-blue rounded-btn text-[13px] outline-none transition-colors"
                  @keyup.enter="handleRegister"
                />
              </div>
            </div>

            <button
              class="w-full h-9 mt-5 rounded-btn text-[13px] font-medium text-white bg-ig-blue hover:bg-ig-blue-hover flex items-center justify-center gap-2 transition-colors"
              :disabled="isLoading"
              @click="handleRegister"
            >
              <Loader2 v-if="isLoading" :size="14" class="animate-spin" />
              {{ isLoading ? 'Creating account...' : 'Register' }}
            </button>

            <div class="mt-3 text-center">
              <span class="text-[12px] text-text-secondary">Already have an account?</span>
              <button
                class="text-[12px] text-ig-blue font-medium ml-1 hover:underline"
                @click="authStore.openLogin()"
              >
                Log In
              </button>
            </div>
          </div>
        </div>
      </Transition>
    </Teleport>

    <!-- Edit Profile Modal -->
    <Teleport to="body">
      <Transition name="fade">
        <div
          v-if="showEditProfile"
          class="fixed inset-0 z-[100] bg-black/40 flex items-center justify-center"
          @click.self="showEditProfile = false"
        >
          <div class="w-[480px] bg-bg-card rounded-card border border-border shadow-dropdown p-6">
            <div class="flex items-center justify-between mb-5">
              <h2 class="text-[15px] font-semibold text-text-primary">Edit Profile</h2>
              <button
                class="w-7 h-7 flex items-center justify-center rounded-btn hover:bg-bg-secondary transition-colors"
                @click="showEditProfile = false"
              >
                <X :size="16" class="text-text-secondary" />
              </button>
            </div>

            <div class="space-y-4">
              <div>
                <label class="text-[12px] text-text-secondary mb-1.5 block font-medium">Nickname</label>
                <input
                  v-model="editForm.nickname"
                  type="text"
                  class="w-full h-9 px-3 bg-bg-secondary border border-transparent focus:border-ig-blue rounded-btn text-[13px] outline-none transition-colors"
                />
              </div>
              <div>
                <label class="text-[12px] text-text-secondary mb-1.5 block font-medium">Bio</label>
                <textarea
                  v-model="editForm.bio"
                  rows="3"
                  class="w-full px-3 py-2.5 bg-bg-secondary border border-transparent focus:border-ig-blue rounded-btn text-[13px] resize-none outline-none transition-colors"
                />
              </div>
            </div>

            <div class="flex gap-3 mt-5">
              <button
                class="flex-1 h-9 rounded-btn text-[13px] font-medium bg-bg-secondary text-text-primary hover:bg-bg-hover transition-colors"
                @click="showEditProfile = false"
              >
                Cancel
              </button>
              <button
                class="flex-1 h-9 rounded-btn text-[13px] font-medium text-white bg-ig-blue hover:bg-ig-blue-hover flex items-center justify-center gap-2 transition-colors"
                :disabled="isLoading"
                @click="saveProfile"
              >
                <Loader2 v-if="isLoading" :size="14" class="animate-spin" />
                Save
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
