import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { AgentConfig } from '@/types/agent'

const DEFAULT_AGENTS: AgentConfig[] = [
  {
    id: 'general',
    name: '通用助手',
    description: '全能AI助手，可以帮你解答各种问题',
    icon: 'Bot',
    model: 'qwen-plus',
    systemPrompt: '你是Aura个人美学操作系统的AI助手。你善于理解用户需求，提供精准、有用的回复。',
    temperature: 0.7,
    maxTokens: 4096,
    tools: ['web_search', 'terminal'],
    createdAt: new Date().toISOString(),
    updatedAt: new Date().toISOString(),
  },
  {
    id: 'fashion',
    name: '穿搭助手',
    description: '专业的时尚穿搭顾问，帮你搭配每日造型',
    icon: 'Shirt',
    model: 'qwen-plus',
    systemPrompt: '你是Aura的穿搭助手。你精通时尚搭配，能够根据场合、天气、用户风格偏好给出专业的穿搭建议。',
    temperature: 0.8,
    maxTokens: 2048,
    tools: ['web_search'],
    createdAt: new Date().toISOString(),
    updatedAt: new Date().toISOString(),
  },
  {
    id: 'coder',
    name: '代码助手',
    description: '专业的编程助手，帮你写代码、调Bug',
    icon: 'Code',
    model: 'qwen-plus',
    systemPrompt: '你是Aura的代码助手。你精通多种编程语言，擅长代码编写、调试、优化和架构设计。',
    temperature: 0.3,
    maxTokens: 8192,
    tools: ['terminal', 'file'],
    createdAt: new Date().toISOString(),
    updatedAt: new Date().toISOString(),
  },
  {
    id: 'study',
    name: '学习伙伴',
    description: '学习助手，帮你理解知识点、整理笔记',
    icon: 'GraduationCap',
    model: 'qwen-plus',
    systemPrompt: '你是Aura的学习伙伴。你善于用简单易懂的方式解释复杂概念，帮助用户高效学习。',
    temperature: 0.7,
    maxTokens: 4096,
    tools: ['web_search'],
    createdAt: new Date().toISOString(),
    updatedAt: new Date().toISOString(),
  },
]

export const useAgentStore = defineStore('agent', () => {
  const agents = ref<AgentConfig[]>([])
  const currentAgent = ref<AgentConfig | null>(null)

  function loadAgents() {
    const saved = localStorage.getItem('aura-agents')
    if (saved) {
      agents.value = JSON.parse(saved)
    } else {
      agents.value = DEFAULT_AGENTS
      saveAgents()
    }
  }

  function saveAgents() {
    localStorage.setItem('aura-agents', JSON.stringify(agents.value))
  }

  function getAgent(id: string): AgentConfig | undefined {
    return agents.value.find(a => a.id === id)
  }

  function setCurrentAgent(agent: AgentConfig) {
    currentAgent.value = agent
  }

  function addAgent(agent: AgentConfig) {
    agents.value.push(agent)
    saveAgents()
  }

  function updateAgent(id: string, updates: Partial<AgentConfig>) {
    const index = agents.value.findIndex(a => a.id === id)
    if (index !== -1) {
      agents.value[index] = { ...agents.value[index], ...updates, updatedAt: new Date().toISOString() }
      saveAgents()
    }
  }

  function deleteAgent(id: string) {
    agents.value = agents.value.filter(a => a.id !== id)
    saveAgents()
  }

  // Initialize
  loadAgents()

  return {
    agents,
    currentAgent,
    loadAgents,
    saveAgents,
    getAgent,
    setCurrentAgent,
    addAgent,
    updateAgent,
    deleteAgent,
  }
})
