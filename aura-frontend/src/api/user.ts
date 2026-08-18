import request from '@/utils/request'

// 类型定义
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

export interface ChangePasswordParams {
  oldPassword: string
  newPassword: string
  confirmPassword: string
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
export function updateUserProfile(data: UpdateProfileParams) {
  return request({
    url: '/v1/users/me',
    method: 'put',
    data,
  })
}

/**
 * 修改密码
 */
export function changePassword(data: ChangePasswordParams) {
  return request({
    url: '/v1/users/me/password',
    method: 'put',
    data,
  })
}
