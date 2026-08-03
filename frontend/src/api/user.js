import { api_get, api_post } from './http'

export function fetch_user_profile(username = 'demo') {
  return api_get('/users/profile', { username })
}

export function save_user_profile(profile) {
  return api_post('/users/profile', profile)
}
