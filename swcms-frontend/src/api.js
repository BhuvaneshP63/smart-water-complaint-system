
import axios from 'axios'

const API = axios.create({
  // In development Vite proxies /api to Spring Boot. Production deployments
  // can point this at their API with VITE_API_BASE_URL.
  baseURL: import.meta.env.VITE_API_BASE_URL || '/api',
  timeout: 15000
})

export function getApiErrorMessage(error, fallback = 'The request could not be completed.') {
  if (error?.code === 'ECONNABORTED') return 'The server took too long to respond. Please try again.'
  if (error?.message === 'Network Error' || !error?.response) {
    return 'Cannot connect to SWCMS. Start the backend and try again.'
  }
  return error.response?.data?.message || error.response?.data?.error || fallback
}

// Automatically attach JWT token to every API request
API.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token')

    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }

    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

export default API
