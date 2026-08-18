export interface User {
  id: string
  username: string
  nickname: string
  avatar?: string
  studentId?: string
}

export interface LoginRequest {
  username: string
  password: string
}

export interface RegisterRequest {
  username: string
  password: string
  nickname: string
  studentId?: string
}

export interface AuthResponse {
  token: string
  refreshToken: string
  user: User
}
