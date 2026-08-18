export interface User {
  id: string
  username: string
  nickname: string
  avatar?: string
  bio?: string
  followers?: number
  following?: number
  outfits?: number
  styleProfile?: StyleProfile
}

export interface StyleProfile {
  mainStyle: string
  mainStyleScore: number
  preferredColors: string[]
  dimensions: {
    minimalist: number
    elegant: number
    casual: number
    trendy: number
  }
}

export interface LoginRequest {
  username: string
  password: string
}

export interface RegisterRequest {
  username: string
  password: string
  nickname: string
}

export interface AuthResponse {
  token: string
  refreshToken: string
  user: User
}
