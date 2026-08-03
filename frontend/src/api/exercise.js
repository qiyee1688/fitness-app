import { api_get } from './http'

export function fetch_exercises(params) {
  return api_get('/exercises', params)
}

export function fetch_exercise(id) {
  return api_get(`/exercises/${id}`)
}

export function search_exercises(keyword, limit = 20) {
  return api_get('/exercises/search', { keyword, limit })
}
