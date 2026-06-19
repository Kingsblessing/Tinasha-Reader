import request from './request'

export function importComic(plateNumber, downloadDir) {
  return request.post('/jm/import', { plateNumber, downloadDir })
}

export function getStatus() {
  return request.get('/jm/status')
}
