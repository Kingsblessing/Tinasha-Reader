import axios from 'axios'
import { ElMessage } from 'element-plus'

const request = axios.create({
  baseURL: '/api/v1',
  timeout: 30000
})

request.interceptors.response.use(
  (response) => {
    const body = response.data
    // Backend wraps responses in { code, message, data }
    if (body && body.code !== undefined) {
      if (body.code === 200) {
        return body.data
      } else {
        const msg = body.message || '请求失败'
        ElMessage.error(msg)
        return Promise.reject(new Error(msg))
      }
    }
    // If backend doesn't wrap (e.g. blob responses), return as-is
    return body
  },
  (error) => {
    let message = '请求失败'
    if (error.response) {
      const { status, data } = error.response
      // If response is a blob (image), just show generic error
      if (data instanceof Blob) {
        message = `请求失败 (${status})`
      } else if (data && data.message) {
        message = data.message
      } else {
        switch (status) {
          case 400:
            message = '请求参数错误'
            break
          case 404:
            message = '资源不存在'
            break
          case 500:
            message = '服务器内部错误'
            break
          default:
            message = `请求失败 (${status})`
        }
      }
    } else if (error.code === 'ECONNABORTED') {
      message = '请求超时'
    } else if (!window.navigator.onLine) {
      message = '网络连接已断开'
    }

    ElMessage.error(message)
    return Promise.reject(error)
  }
)

export default request
