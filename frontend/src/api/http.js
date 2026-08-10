const API_BASE_URL = '/api'

export async function api_get(path, params = {}) {
  const url = new URL(`${API_BASE_URL}${path}`, window.location.origin)

  Object.entries(params).forEach(([key, value]) => {
    if (value !== undefined && value !== null && value !== '') {
      url.searchParams.set(key, value)
    }
  })

  const response = await fetch(url)
  const payload = await response.json().catch(() => null)

  if (!response.ok || payload?.code !== 0) {
    const error = new Error(payload?.message || 'Request failed')
    error.code = payload?.code
    error.status = response.status
    throw error
  }

  return payload.data
}

export async function api_post(path, body = {}) {
  const response = await fetch(`${API_BASE_URL}${path}`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify(body),
  })
  const payload = await response.json().catch(() => null)

  if (!response.ok || payload?.code !== 0) {
    const error = new Error(payload?.message || 'Request failed')
    error.code = payload?.code
    error.status = response.status
    throw error
  }

  return payload.data
}

export async function api_delete(path) {
  const response = await fetch(`${API_BASE_URL}${path}`, {
    method: 'DELETE',
  })
  const payload = await response.json().catch(() => null)

  if (!response.ok || payload?.code !== 0) {
    const error = new Error(payload?.message || 'Request failed')
    error.code = payload?.code
    error.status = response.status
    throw error
  }

  return payload.data
}
