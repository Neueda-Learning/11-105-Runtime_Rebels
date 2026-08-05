// import { defineConfig } from 'vite'
// import react from '@vitejs/plugin-react'

// // Dev server proxies /api to the Spring Boot backend so the browser
// // never has to deal with CORS while developing locally.
// export default defineConfig({
//   plugins: [react()],
//   server: {
//     port: 5173,
//     proxy: {
//       '/api': {
//         target: 'http://localhost:8080',
//         changeOrigin: true,
//       },
//     },
//   },
// })


import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

export default defineConfig({
  plugins: [react()],
  server: {
    host: '0.0.0.0',
    port: 5173,
    proxy: {
      '/api': {
        // Local dev on Windows/macOS/Linux: backend runs on localhost:8080.
        // host.docker.internal is only needed when frontend itself runs inside Docker.
        target: 'http://localhost:8080',
        changeOrigin: true,
      },
    },
  },
})