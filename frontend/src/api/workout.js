import { api_delete, api_get, api_patch, api_post } from './http'

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

export function delete_workout_template(template_id) {
  return api_delete(`/workout-templates/${template_id}`)
}

export function update_workout_template(template_id, payload) {
  return api_patch(`/workout-templates/${template_id}`, payload)
}

export function fetch_workout_template_substitutes(template_id, template_exercise_id) {
  return api_get(`/workout-templates/${template_id}/exercises/${template_exercise_id}/substitutes`)
}
