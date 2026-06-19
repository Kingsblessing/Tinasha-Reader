import request from './request'

export function getAll() {
  return request.get('/settings')
}

export function get(key) {
  return request.get(`/settings/${key}`)
}

export function set(key, value) {
  return request.put(`/settings/${key}`, { value })
}
