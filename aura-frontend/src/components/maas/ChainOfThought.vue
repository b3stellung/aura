<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { ChevronDown, ChevronRight, Brain, Wrench, Eye, CheckCircle } from 'lucide-vue-next'

export interface COTStep {
  id: string
  type: 'thought' | 'tool' | 'observation' | 'result'
  content: string
  timestamp: number
  toolName?: string
  toolParams?: any
  toolResult?: any
  stepNumber?: number
}

const props = defineProps<{
  steps: COTStep[]
  isStreaming?: boolean
}>()

const isExpanded = ref(false)
const autoExpand = ref(true)

// 自动展开第一个思考步骤
watch(() => props.steps.length, (newLen) => {
  if (newLen > 0 && autoExpand.value) {
    isExpanded.value = true
  }
})

const toggleExpand = () => {
  isExpanded.value = !isExpanded.value
}

const stepIcon = (type: string) => {
  switch (type) {
    case 'thought': return Brain
    case 'tool': return Wrench
    case 'observation': return Eye
    case 'result': return CheckCircle
    default: return Brain
  }
}

const stepLabel = (type: string) => {
  switch (type) {
    case 'thought': return '思考'
    case 'tool': return '调用工具'
    case 'observation': return '观察结果'
    case 'result': return '最终结果'
    default: return '步骤'
  }
}

const stepColor = (type: string) => {
  switch (type) {
    case 'thought': return 'text-purple-500'
    case 'tool': return 'text-blue-500'
    case 'observation': return 'text-green-500'
    case 'result': return 'text-orange-500'
    default: return 'text-gray-500'
  }
}

const formattedContent = (step: COTStep) => {
  if (step.type === 'tool' && step.toolName) {
    return `${step.toolName}(${JSON.stringify(step.toolParams || {})})`
  }
  if (step.type === 'observation' && step.toolResult) {
    return typeof step.toolResult === 'string' 
      ? step.toolResult 
      : JSON.stringify(step.toolResult, null, 2)
  }
  return step.content
}
</script>

<template>
  <div class="cot-container">
    <!-- 折叠状态：只显示摘要 -->
    <button 
      v-if="!isExpanded"
      @click="toggleExpand"
      class="flex items-center gap-2 text-sm text-gray-500 hover:text-gray-700 transition-colors py-2"
    >
      <Brain :size="14" class="text-purple-500" />
      <span>思考过程</span>
      <span class="text-xs text-gray-400">({{ steps.length }} 步)</span>
      <ChevronRight :size="14" />
    </button>

    <!-- 展开状态：显示详细步骤 -->
    <div v-else class="border border-gray-200 rounded-xl overflow-hidden">
      <!-- 头部 -->
      <button 
        @click="toggleExpand"
        class="w-full flex items-center justify-between p-3 bg-gray-50 hover:bg-gray-100 transition-colors"
      >
        <div class="flex items-center gap-2">
          <Brain :size="16" class="text-purple-500" />
          <span class="text-sm font-medium text-gray-700">思考过程</span>
          <span class="text-xs text-gray-400">({{ steps.length }} 步)</span>
        </div>
        <ChevronDown :size="16" class="text-gray-400" />
      </button>

      <!-- 步骤列表 -->
      <div class="divide-y divide-gray-100">
        <div 
          v-for="(step, index) in steps" 
          :key="step.id"
          class="p-3 hover:bg-gray-50 transition-colors"
        >
          <!-- 步骤头部 -->
          <div class="flex items-center gap-2 mb-2">
            <component 
              :is="stepIcon(step.type)" 
              :size="14" 
              :class="stepColor(step.type)"
            />
            <span class="text-xs font-medium" :class="stepColor(step.type)">
              {{ stepLabel(step.type) }}
            </span>
            <span v-if="step.stepNumber" class="text-xs text-gray-400">
              步骤 {{ step.stepNumber }}
            </span>
            <span class="text-xs text-gray-300 ml-auto">
              {{ new Date(step.timestamp).toLocaleTimeString() }}
            </span>
          </div>

          <!-- 步骤内容 -->
          <div class="pl-6">
            <!-- 思考内容 -->
            <div v-if="step.type === 'thought'" class="text-sm text-gray-600 leading-relaxed">
              {{ step.content }}
            </div>

            <!-- 工具调用 -->
            <div v-else-if="step.type === 'tool'" class="space-y-2">
              <div class="flex items-center gap-2">
                <span class="text-sm font-medium text-blue-600">{{ step.toolName }}</span>
                <span class="text-xs text-gray-400">工具调用</span>
              </div>
              <div v-if="step.toolParams" class="bg-blue-50 rounded-lg p-2">
                <pre class="text-xs text-blue-800 whitespace-pre-wrap">{{ JSON.stringify(step.toolParams, null, 2) }}</pre>
              </div>
            </div>

            <!-- 观察结果 -->
            <div v-else-if="step.type === 'observation'" class="space-y-2">
              <div class="flex items-center gap-2">
                <Eye :size="14" class="text-green-500" />
                <span class="text-xs font-medium text-green-600">工具返回</span>
              </div>
              <div class="bg-green-50 rounded-lg p-2">
                <pre class="text-xs text-green-800 whitespace-pre-wrap">{{ formattedContent(step) }}</pre>
              </div>
            </div>

            <!-- 最终结果 -->
            <div v-else-if="step.type === 'result'" class="space-y-2">
              <div class="flex items-center gap-2">
                <CheckCircle :size="14" class="text-orange-500" />
                <span class="text-xs font-medium text-orange-600">生成结果</span>
              </div>
              <div class="bg-orange-50 rounded-lg p-2">
                <pre class="text-xs text-orange-800 whitespace-pre-wrap">{{ formattedContent(step) }}</pre>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 流式状态指示器 -->
      <div v-if="isStreaming" class="p-3 bg-gray-50 border-t border-gray-100">
        <div class="flex items-center gap-2 text-sm text-gray-500">
          <span class="w-2 h-2 rounded-full bg-purple-500 animate-pulse" />
          <span>思考中...</span>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.cot-container {
  margin: 8px 0;
}
</style>
