import request from './request'

export function getAll(params) {
  return request.get('/comics', { params })
}

export function getById(id) {
  return request.get(`/comics/${id}`)
}

export function update(id, data) {
  return request.put(`/comics/${id}`, data)
}

export function deleteComic(id) {
  return request.delete(`/comics/${id}`)
}

export function updateRating(id, rating) {
  return request.put(`/comics/${id}/rating`, { rating })
}

export function getPages(comicId) {
  return request.get(`/comics/${comicId}/pages`)
}

export async function getPageImage(comicId, pageNum) {
  const response = await request.get(`/comics/${comicId}/pages/${pageNum}`, {
    responseType: 'blob'
  })
  return URL.createObjectURL(response)
}

export async function getCover(comicId) {
  const response = await request.get(`/comics/${comicId}/cover`, {
    responseType: 'blob'
  })
  return URL.createObjectURL(response)
}

export function updateProgress(comicId, page) {
  return request.put(`/comics/${comicId}/progress`, { page })
}

export function getRecentRead(params) {
  return request.get('/comics/recent-read', { params })
}
