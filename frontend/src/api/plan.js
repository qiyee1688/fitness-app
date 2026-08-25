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

export function submit_exercise_feedback(workout_id, exercise_id, feedback) {
  return api_post(`/plans/workouts/${workout_id}/exercises/${exercise_id}/feedback`, feedback)
}

export function replace_plan_workout(plan_id, workout_id, payload) {
  return api_post(`/plans/${plan_id}/workouts/${workout_id}/replace`, payload)
}

export function fetch_prescription_adjustments(username = 'demo') {
  return api_get('/plans/adjustments', { username })
}

export function accept_prescription_adjustment(adjustment_id, expected_plan_version, username = 'demo') {
  return api_post(`/plans/adjustments/${adjustment_id}/accept?username=${encodeURIComponent(username)}`, {
    expectedPlanVersion: expected_plan_version,
  }, { username })
}

export function decline_prescription_adjustment(adjustment_id, expected_plan_version, username = 'demo') {
  return api_post(`/plans/adjustments/${adjustment_id}/decline?username=${encodeURIComponent(username)}`, {
    expectedPlanVersion: expected_plan_version,
  }, { username })
}
