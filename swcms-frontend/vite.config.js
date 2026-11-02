import react from '@vitejs/plugin-react'
import { defineConfig, loadEnv } from 'vite'

// https://vite.dev/config/
export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, '.', '')

  return {
  plugins: [react()],
  server: {
    proxy: {
      '/api': {
        target: env.SWCMS_API_PROXY || 'http://localhost:8080',
        changeOrigin: true
      }
    }
  }
  }
})
