import request from './request'

export function getByComicId(comicId) {
  return request.get(`/comics/${comicId}/reviews`)
}

export function create(comicId, data) {
  return request.post(`/comics/${comicId}/reviews`, data)
}

export function update(reviewId, data) {
  return request.put(`/reviews/${reviewId}`, data)
}

export function deleteReview(reviewId) {
  return request.delete(`/reviews/${reviewId}`)
}
