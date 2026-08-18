import request from '@/utils/request'

// 类型定义
export interface RegisterParams {
  username: string
  email: string
  password: string
  nickname?: string
}

export interface LoginParams {
  username: string
  password: string
}

export interface TokenResponse {
  code: number
  message: string
  data: {
    accessToken: string
    refreshToken: string
    expiresIn: number
  }
}

export interface RefreshTokenParams {
  refreshToken: string
}

export interface UserProfile {
  id: string
  username: string
  email: string
  nickname: string
  avatar: string
  bio: string
  createdAt: string
  updatedAt: string
}

export interface UpdateProfileParams {
  nickname?: string
  avatar?: string
  bio?: string
}

/**
 * 用户注册
 */
export function register(data: RegisterParams): Promise<TokenResponse> {
  return request({
    url: '/v1/auth/register',
    method: 'post',
    data,
  }) as Promise<TokenResponse>
}

/**
 * 用户登录
 */
export function login(data: LoginParams): Promise<TokenResponse> {
  return request({
    url: '/v1/auth/login',
    method: 'post',
    data,
  }) as Promise<TokenResponse>
}

/**
 * 刷新Token
 */
export function refreshToken(data: RefreshTokenParams): Promise<TokenResponse> {
  return request({
    url: '/v1/auth/refresh',
    method: 'post',
    data,
  }) as Promise<TokenResponse>
}

/**
 * 用户登出
 */
export function logout() {
  return request({
    url: '/v1/auth/logout',
    method: 'post',
  })
}

/**
 * 获取当前用户资料
 */
export function getUserProfile(): Promise<{ code: number; data: UserProfile }> {
  return request({
    url: '/v1/users/me',
    method: 'get',
  }) as Promise<{ code: number; data: UserProfile }>
}

/**
 * 更新当前用户资料
 */
export function updateUserProfile(data: UpdateProfileParams): Promise<{ code: number; data: UserProfile }> {
  return request({
    url: '/v1/users/me',
    method: 'put',
    data,
  }) as Promise<{ code: number; data: UserProfile }>
}
