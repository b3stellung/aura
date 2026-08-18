import { apiClient } from './client'
import type { ChatMessage as LegacyChatMessage, OutfitRecommendation } from '@/types'
import type { ChatMessage, Citation, RunEvent, StreamCallbacks, ToolCall, RunStatus } from '@/types/maas'
export type { ChatMessage, Citation, RunEvent, StreamCallbacks, ToolCall, RunStatus }
export interface ChatSession { id: string; title: string; createdAt: string; lastMessage: string }
export interface SendMessageRequest { content: string; sessionId?: string }
export interface SendMessageResponse { message: LegacyChatMessage; recommendation?: OutfitRecommendation }
export function parseSseEvent(input: string | { event?: string; data?: string }): RunEvent | null {
  const eventName = typeof input === 'string' ? undefined : input.event; const raw = typeof input === 'string' ? input : input.data
  if (!raw?.trim() || raw.trim() === '[DONE]') return null
  let v: any; try { v = JSON.parse(raw.trim()) } catch { return null }; if (!v || typeof v !== 'object') return null
  const t = String(v.type ?? eventName ?? '').toLowerCase()
  if (['text','delta','token'].includes(t)) return { type: 'delta', delta: String(v.delta ?? v.content ?? v.text ?? '') }
  if (['thought','thinking'].includes(t)) return { type: 'thought', content: String(v.content ?? v.delta ?? '') }
  if (['tool','tool_call','toolcall'].includes(t)) return { type: 'tool', tool: (v.tool ?? v.data ?? v) as ToolCall }
  if (['observation','result','tool_result'].includes(t)) return { type: 'observation', result: v.result ?? v.data ?? v }
  if (['retrieval','citation','citations'].includes(t)) return { type: 'retrieval', citations: Array.isArray(v.citations ?? v.data) ? (v.citations ?? v.data) : [] }
  if (['done','complete','completed'].includes(t)) return { type: 'done', message: v.message, status: v.status as RunStatus }
  if (['error','failed'].includes(t)) return { type: 'error', error: String(v.error ?? v.message ?? 'Stream error'), code: v.code }
  return null
}
function emit(e: RunEvent, c: StreamCallbacks) { 
  c.onEvent?.(e); 
  if (e.type==='delta') c.onDelta?.(e.delta); 
  else if(e.type==='thought') c.onThought?.(e.content); 
  else if(e.type==='tool') c.onTool?.(e.tool); 
  else if(e.type==='observation') c.onObservation?.(e.result); 
  else if(e.type==='retrieval') c.onRetrieval?.(e.citations); 
  else if(e.type==='done') c.onDone?.(e.message); 
  else c.onError?.(e.error) 
}
export async function streamChat(data: SendMessageRequest, callbacks: StreamCallbacks = {}, signal?: AbortSignal): Promise<void> {
  const token = localStorage.getItem('aura-token') ?? localStorage.getItem('aura_token'); const r = await fetch('/api/chat/messages/stream', { method:'POST', headers:{'Content-Type':'application/json', ...(token?{Authorization:`Bearer ${token}`}:{})}, body:JSON.stringify(data), signal }); if(!r.ok) throw new Error(`Stream request failed (${r.status})`)
  const reader=r.body?.getReader(); if(!reader) throw new Error('Unable to read stream response'); const d=new TextDecoder(); let b=''; let stop=false
  const process = (p: string) => { const lines=p.split(/\r?\n/); const ev=lines.find(l=>l.startsWith('event:'))?.slice(6).trim(); const dat=lines.filter(l=>l.startsWith('data:')).map(l=>l.slice(5).trim()).join('\n'); const e=parseSseEvent({event:ev,data:dat}); if(e){emit(e,callbacks); if(e.type==='done'||e.type==='error') stop=true} }
  try { while(!stop){ const x=await reader.read(); if(x.done) { b += d.decode(); break }; b+=d.decode(x.value,{stream:true}); const parts=b.split(/\r?\n\r?\n/); b=parts.pop()??''; for(const p of parts){ process(p); if(stop) break } } if(!stop && b.trim()) process(b) } finally { if(stop || signal?.aborted) await reader.cancel().catch(()=>{}); reader.releaseLock() }
}
export function getChatHistory() { return apiClient.get<ChatSession[]>('/chat/sessions') }
export function getSessionMessages(sessionId: string) { return apiClient.get<LegacyChatMessage[]>(`/chat/sessions/${sessionId}/messages`) }
export function sendMessage(data: SendMessageRequest) { return apiClient.post<SendMessageResponse>('/chat/messages', data) }
export function sendMessageStream(data: SendMessageRequest, onChunk: (text:string)=>void, onRecommendation?: (rec:OutfitRecommendation)=>void, signal?: AbortSignal) { return streamChat(data, {onDelta:onChunk, onTool:t=>{if(t.name==='recommendation'&&onRecommendation) onRecommendation(t.output as OutfitRecommendation)}}, signal) }
export function deleteSession(sessionId: string) { return apiClient.delete(`/chat/sessions/${sessionId}`) }
