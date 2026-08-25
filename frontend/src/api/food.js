import { api_get } from './http'

export function fetch_food_items(params) {
  return api_get('/food-items', params)
}

export function fetch_food_item(id) {
  return api_get(`/food-items/${encodeURIComponent(id)}`)
}

export function convert_food_item(id, servings) {
  return api_get(`/food-items/${encodeURIComponent(id)}/conversion`, { servings })
}
