import { api_get } from './http'

export function fetch_articles(params) {
  return api_get('/knowledge-articles', params)
}

export function fetch_article(slug) {
  return api_get(`/knowledge-articles/${encodeURIComponent(slug)}`)
}

export function fetch_articles_for_exercise(exercise_id) {
  return api_get(`/exercises/${encodeURIComponent(exercise_id)}/articles`)
}
