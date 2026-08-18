export interface AgentConfig {
  id: string
  name: string
  description: string
  icon: string
  model: string
  systemPrompt: string
  temperature: number
  maxTokens: number
  tools: string[]
  createdAt: string
  updatedAt: string
}

export interface AgentTemplate {
  id: string
  name: string
  description: string
  icon: string
  category: string
  defaultConfig: Partial<AgentConfig>
}
