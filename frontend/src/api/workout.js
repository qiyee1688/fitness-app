import { api_post } from './http'

export function generate_on_demand_workout(payload) {
  return api_post('/workouts/on-demand', payload)
}

export function start_on_demand_workout(workout_id) {
  return api_post(`/workouts/${workout_id}/start`)
}

export function complete_on_demand_workout(workout_id) {
  return api_post(`/workouts/${workout_id}/complete`)
}
