import { api_get, api_post } from './http'

export function fetch_current_plan(username = 'demo') {
  return api_get('/plans/current', { username })
}

export function fetch_today_workout(date) {
  return api_get('/plans/today', { username: 'demo', date })
}

export function complete_workout(workout_id) {
  return api_post(`/plans/workouts/${workout_id}/complete`)
}
