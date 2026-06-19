import request from './request'

export function getAll() {
  return request.get('/sources')
}

export function create(data) {
  return request.post('/sources', data)
}

export function getById(id) {
  return request.get(`/sources/${id}`)
}

export function update(id, data) {
  return request.put(`/sources/${id}`, data)
}

export function deleteSource(id) {
  return request.delete(`/sources/${id}`)
}

export function triggerScan(id) {
  return request.post(`/sources/${id}/scan`)
}

export function getScanStatus(id) {
  return request.get(`/sources/${id}/scan-status`)
}

export function toggleEnabled(id) {
  return request.put(`/sources/${id}/toggle`)
}
