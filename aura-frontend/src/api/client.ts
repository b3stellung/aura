/**
 * HTTP Client - Aura API
 * Wraps fetch with auth token injection, error handling, and JSON parsing.
 */

const BASE_URL = '/api'

interface RequestOptions {
  method?: string
  body?: unknown
  headers?: Record<string, string>
  signal?: AbortSignal
}

class ApiClient {
  private getToken(): string | null {
    return localStorage.getItem('aura_token')
  }

  private buildHeaders(extra?: Record<string, string>): Record<string, string> {
    const headers: Record<string, string> = {
      'Content-Type': 'application/json',
      ...extra,
    }
    const token = this.getToken()
    if (token) {
      headers['Authorization'] = `Bearer ${token}`
    }
    return headers
  }

  async request<T>(endpoint: string, options: RequestOptions = {}): Promise<T> {
    const { method = 'GET', body, headers: extraHeaders, signal } = options
    const url = `${BASE_URL}${endpoint}`

    const res = await fetch(url, {
      method,
      headers: this.buildHeaders(extraHeaders),
      body: body ? JSON.stringify(body) : undefined,
      signal,
    })

    if (!res.ok) {
      const errBody = await res.json().catch(() => ({}))
      const message = (errBody as { message?: string }).message || `请求失败 (${res.status})`
      throw new ApiError(message, res.status, errBody)
    }

    // Handle 204 No Content
    if (res.status === 204) return undefined as T

    return res.json() as Promise<T>
  }

  get<T>(endpoint: string, signal?: AbortSignal) {
    return this.request<T>(endpoint, { signal })
  }

  post<T>(endpoint: string, body?: unknown, signal?: AbortSignal) {
    return this.request<T>(endpoint, { method: 'POST', body, signal })
  }

  put<T>(endpoint: string, body?: unknown, signal?: AbortSignal) {
    return this.request<T>(endpoint, { method: 'PUT', body, signal })
  }

  delete<T>(endpoint: string, signal?: AbortSignal) {
    return this.request<T>(endpoint, { method: 'DELETE', signal })
  }
}

export class ApiError extends Error {
  status: number
  body: unknown

  constructor(message: string, status: number, body: unknown) {
    super(message)
    this.name = 'ApiError'
    this.status = status
    this.body = body
  }
}

export const apiClient = new ApiClient()
