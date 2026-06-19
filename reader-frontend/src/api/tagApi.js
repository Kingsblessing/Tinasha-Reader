import request from './request'

export function getAll() {
  return request.get('/tags')
}

export function create(data) {
  return request.post('/tags', data)
}

export function update(id, data) {
  return request.put(`/tags/${id}`, data)
}

export function deleteTag(id) {
  return request.delete(`/tags/${id}`)
}

export function getGroupNames() {
  return request.get('/tags/groups')
}

export function addTagToComic(comicId, tagId) {
  return request.post(`/tags/comics/${comicId}/tags`, { tagId })
}

export function removeTagFromComic(comicId, tagId) {
  return request.delete(`/tags/comics/${comicId}/tags/${tagId}`)
}

export function getComicTags(comicId) {
  return request.get(`/tags/comics/${comicId}/tags`)
}
