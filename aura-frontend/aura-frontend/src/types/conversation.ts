export interface Message {
  id: string
  role: 'user' | 'assistant' | 'system' | 'tool'
  content: string
  toolCalls?: ToolCall[]
  toolResult?: ToolResult
  timestamp: string
  isStreaming?: boolean
}

export interface ToolCall {
  id: string
  name: string
  arguments: string
}

export interface ToolResult {
  toolCallId: string
  content: string
  isError?: boolean
}

export interface Conversation {
  id: string
  title: string
  agentId: string
  agentName: string
  agentIcon: string
  messages: Message[]
  createdAt: string
  updatedAt: string
  messageCount: number
  lastMessage?: string
}

export interface ConversationList {
  today: Conversation[]
  yesterday: Conversation[]
  older: Conversation[]
}
