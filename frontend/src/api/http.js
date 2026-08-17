const API_BASE_URL = '/api'

export async function api_get(path, params = {}) {
  const url = new URL(`${API_BASE_URL}${path}`, window.location.origin)

  Object.entries(params).forEach(([key, value]) => {
    if (value !== undefined && value !== null && value !== '') {
      url.searchParams.set(key, value)
    }
  })

  return handle_response(await fetch(url))
}

async function api_request(path, method, body) {
  const response = await fetch(`${API_BASE_URL}${path}`, {
    method,
    headers: body === undefined ? undefined : { 'Content-Type': 'application/json' },
    body: body === undefined ? undefined : JSON.stringify(body),
  })
  return handle_response(response)
}

async function handle_response(response) {
  const payload = await response.json().catch(() => null)

  if (!response.ok || payload?.code !== 0) {
    const error = new Error(payload?.message || 'Request failed')
    error.code = payload?.code
    error.status = response.status
    throw error
  }
  return payload.data
}

export function api_post(path, body = {}) {
  return api_request(path, 'POST', body)
}

export async function api_patch(path, body = {}) {
  return api_request(path, 'PATCH', body)
}

export async function api_delete(path) {
  return api_request(path, 'DELETE')
}
