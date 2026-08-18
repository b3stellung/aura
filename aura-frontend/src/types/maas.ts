export type RunStatus = 'idle' | 'pending' | 'running' | 'completed' | 'cancelled' | 'error'

export interface Citation {
  id?: string
  title?: string
  url?: string
  [key: string]: unknown
}

export interface ToolCall {
  id?: string
  name: string
  arguments?: unknown
  output?: unknown
  status?: string
  [key: string]: unknown
}

export interface ChatMessage {
  id?: string
  role: 'user' | 'assistant' | 'system' | 'tool'
  content: string
  timestamp?: string
  citations?: Citation[]
  [key: string]: unknown
}

export type RunEvent =
  | { type: 'delta'; delta: string; message?: ChatMessage }
  | { type: 'thought'; content: string }
  | { type: 'tool'; tool: ToolCall }
  | { type: 'observation'; result: unknown }
  | { type: 'retrieval'; citations: Citation[]; query?: string }
  | { type: 'done'; message?: ChatMessage; status?: RunStatus }
  | { type: 'error'; error: string; code?: string }

export interface StreamCallbacks {
  onEvent?: (event: RunEvent) => void
  onDelta?: (delta: string) => void
  onThought?: (content: string) => void
  onTool?: (tool: ToolCall) => void
  onObservation?: (result: unknown) => void
  onRetrieval?: (citations: Citation[]) => void
  onDone?: (message?: ChatMessage) => void
  onError?: (error: string) => void
}
