<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { ArrowRight, Search } from 'lucide-vue-next'
import { useAgentStore } from '@/stores/agent'
import { useConversationStore } from '@/stores/conversation'

const router = useRouter()
const agentStore = useAgentStore()
const conversationStore = useConversationStore()
const isLoaded = ref(false)

// Mouse spotlight effect
const mouseX = ref(0)
const mouseY = ref(0)

const handleMouseMove = (e: MouseEvent) => {
  mouseX.value = e.clientX
  mouseY.value = e.clientY
}

onMounted(() => {
  setTimeout(() => { isLoaded.value = true }, 100)
  window.addEventListener('mousemove', handleMouseMove)
})

onUnmounted(() => {
  window.removeEventListener('mousemove', handleMouseMove)
})

const searchQuery = ref('')

const filteredAgents = computed(() => {
  if (!searchQuery.value) return agentStore.agents
  const q = searchQuery.value.toLowerCase()
  return agentStore.agents.filter(a => 
    a.name.toLowerCase().includes(q) || a.description.toLowerCase().includes(q)
  )
})

const handleStartChat = (agentId: string) => {
  const agent = agentStore.getAgent(agentId)
  if (agent) {
    const conv = conversationStore.createConversation(agent.id, agent.name, agent.icon)
    router.push(`/sandbox/${conv.id}`)
  }
}

const patterns = ['waves', 'dots', 'grid', 'circles']
</script>

<template>
  <div class="min-h-screen bg-white relative overflow-hidden">
    
    <!-- Spotlight Effect -->
    <div 
      class="fixed w-[300px] h-[300px] rounded-full pointer-events-none z-50 mix-blend-difference"
      :style="{
        left: mouseX - 150 + 'px',
        top: mouseY - 150 + 'px',
        background: 'radial-gradient(circle, rgba(255,255,255,0.8) 0%, transparent 70%)',
        transition: 'left 0.1s ease-out, top 0.1s ease-out'
      }"
    />

    <!-- Background Pattern -->
    <div class="absolute inset-0 opacity-10">
      <div class="absolute inset-0" style="background-image: radial-gradient(circle, #e5e7eb 1px, transparent 1px); background-size: 40px 40px;" />
    </div>

    <div class="relative max-w-5xl mx-auto px-8 py-8">
      
      <!-- Header -->
      <header class="mb-12" :class="{ 'animate-fade-in': isLoaded }">
        <h1 class="text-5xl font-bold text-black mb-4 tracking-tight">Agent</h1>
        <p class="text-lg text-gray-500">选择专业领域的AI助手开始对话</p>
      </header>

      <!-- Search -->
      <div class="mb-10 animate-slide-up" style="animation-delay: 0.1s">
        <div class="relative max-w-md">
          <Search :size="16" class="absolute left-4 top-1/2 -translate-y-1/2 text-gray-400" />
          <input 
            v-model="searchQuery"
            class="w-full h-12 pl-11 pr-4 bg-gray-50 rounded-xl text-sm text-black
                   placeholder:text-gray-400 border border-gray-200
                   focus:border-black focus:bg-white transition-all duration-300"
            placeholder="搜索Agent..." 
          />
        </div>
      </div>

      <!-- Agent Grid -->
      <div class="grid grid-cols-2 gap-8">
        <div
          v-for="(agent, index) in filteredAgents"
          :key="agent.id"
          @click="handleStartChat(agent.id)"
          class="group cursor-pointer animate-slide-up"
          :style="{ animationDelay: `${0.2 + index * 0.1}s` }"
        >
          <div class="rounded-2xl border border-gray-200 overflow-hidden 
                      hover:border-gray-400 hover:shadow-lg transition-all duration-500">
            <!-- Abstract Graphic -->
            <div class="h-48 bg-gray-50 relative overflow-hidden">
              <div class="absolute inset-0 flex items-center justify-center">
                <!-- Waves -->
                <svg v-if="patterns[index % patterns.length] === 'waves'" 
                     class="w-full h-full opacity-15" viewBox="0 0 400 200">
                  <path d="M0 100 Q100 50 200 100 Q300 150 400 100" stroke="black" stroke-width="2" fill="none" />
                  <path d="M0 120 Q100 70 200 120 Q300 170 400 120" stroke="black" stroke-width="1.5" fill="none" />
                  <path d="M0 80 Q100 30 200 80 Q300 130 400 80" stroke="black" stroke-width="1" fill="none" />
                </svg>
                <!-- Dots -->
                <svg v-if="patterns[index % patterns.length] === 'dots'" 
                     class="w-32 h-32 opacity-20" viewBox="0 0 100 100">
                  <circle cx="25" cy="25" r="4" fill="black" />
                  <circle cx="75" cy="25" r="4" fill="black" />
                  <circle cx="50" cy="50" r="4" fill="black" />
                  <circle cx="25" cy="75" r="4" fill="black" />
                  <circle cx="75" cy="75" r="4" fill="black" />
                  <line x1="25" y1="25" x2="75" y2="75" stroke="black" stroke-width="0.5" />
                  <line x1="75" y1="25" x2="25" y2="75" stroke="black" stroke-width="0.5" />
                </svg>
                <!-- Grid -->
                <svg v-if="patterns[index % patterns.length] === 'grid'" 
                     class="w-36 h-36 opacity-15" viewBox="0 0 100 100">
                  <rect x="15" y="15" width="20" height="20" fill="none" stroke="black" stroke-width="1.5" />
                  <rect x="40" y="15" width="20" height="20" fill="none" stroke="black" stroke-width="1.5" />
                  <rect x="65" y="15" width="20" height="20" fill="none" stroke="black" stroke-width="1.5" />
                  <rect x="15" y="40" width="20" height="20" fill="none" stroke="black" stroke-width="1.5" />
                  <rect x="40" y="40" width="20" height="20" fill="black" stroke="black" stroke-width="1.5" />
                  <rect x="65" y="40" width="20" height="20" fill="none" stroke="black" stroke-width="1.5" />
                  <rect x="15" y="65" width="20" height="20" fill="none" stroke="black" stroke-width="1.5" />
                  <rect x="40" y="65" width="20" height="20" fill="none" stroke="black" stroke-width="1.5" />
                  <rect x="65" y="65" width="20" height="20" fill="none" stroke="black" stroke-width="1.5" />
                </svg>
                <!-- Circles -->
                <svg v-if="patterns[index % patterns.length] === 'circles'" 
                     class="w-40 h-40 opacity-15" viewBox="0 0 100 100">
                  <circle cx="50" cy="50" r="40" fill="none" stroke="black" stroke-width="1.5" />
                  <circle cx="50" cy="50" r="25" fill="none" stroke="black" stroke-width="1" />
                  <circle cx="50" cy="50" r="10" fill="black" />
                </svg>
              </div>
            </div>
            <!-- Content -->
            <div class="p-6">
              <div class="flex items-center justify-between mb-3">
                <h3 class="text-lg font-bold text-black group-hover:text-gray-700 transition-colors">
                  {{ agent.name }}
                </h3>
                <ArrowRight :size="18" class="text-gray-300 group-hover:text-black 
                                               group-hover:translate-x-2 transition-all duration-300" />
              </div>
              <p class="text-sm text-gray-500 mb-4">{{ agent.description }}</p>
              <div class="flex flex-wrap gap-1.5">
                <span 
                  v-for="tool in agent.tools"
                  :key="tool"
                  class="px-2.5 py-1 rounded-md text-[11px] bg-gray-100 text-gray-500"
                >
                  {{ tool }}
                </span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- Empty State -->
      <div v-if="filteredAgents.length === 0" 
           class="text-center py-24 animate-fade-in">
        <p class="text-lg text-gray-400 mb-2">未找到匹配的Agent</p>
        <p class="text-sm text-gray-300">尝试其他关键词</p>
      </div>
    </div>
  </div>
</template>

<style scoped>
.animate-fade-in {
  animation: fade-in 0.5s ease-out;
}

.animate-slide-up {
  opacity: 0;
  animation: slide-up 0.5s ease-out forwards;
}

@keyframes fade-in {
  from { opacity: 0; }
  to { opacity: 1; }
}

@keyframes slide-up {
  from { opacity: 0; transform: translateY(12px); }
  to { opacity: 1; transform: translateY(0); }
}
</style>
