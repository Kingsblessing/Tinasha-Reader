import request from './request'

export function getAll(params) {
  return request.get('/history', { params })
}

export function record(data) {
  return request.post('/history', data)
}

export function deleteHistory(id) {
  return request.delete(`/history/${id}`)
}

export function clearOld(beforeDate) {
  return request.delete('/history', { data: { beforeDate } })
}

export function getStats() {
  return request.get('/history/stats')
}

export function getRecent(params) {
  return request.get('/history/recent', { params })
}
