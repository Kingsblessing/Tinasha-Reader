import request from './request'

export function exportData() {
  return request.get('/backup/export')
}

export function importData(data) {
  return request.post('/backup/import', data)
}

export function getInfo() {
  return request.get('/backup/info')
}
