import request from './request'

export function getAll(params) {
  return request.get('/favorites', { params })
}

export function checkFavorited(comicId) {
  return request.get('/favorites/check', { params: { comicId } })
}

export function create(data) {
  return request.post('/favorites', data)
}

export function deleteById(id) {
  return request.delete(`/favorites/${id}`)
}

export function getGroupNames() {
  return request.get('/favorites/groups')
}
