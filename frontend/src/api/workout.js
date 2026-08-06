import { api_get, api_post } from './http'

export function generate_on_demand_workout(payload) {
  return api_post('/workouts/on-demand', payload)
}

export function start_on_demand_workout(workout_id) {
  return api_post(`/workouts/${workout_id}/start`)
}

export function complete_on_demand_workout(workout_id) {
  return api_post(`/workouts/${workout_id}/complete`)
}


export function save_workout_template(payload) {
  return api_post('/workout-templates', payload)
}

export function fetch_workout_templates() {
  return api_get('/workout-templates')
}
