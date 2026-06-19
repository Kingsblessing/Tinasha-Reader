import request from './request'

export function browse(path = '') {
  return request.get('/filesystem/browse', { params: { path } })
}
