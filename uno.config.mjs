import { defineConfig, presetUno } from 'unocss'

export default defineConfig({
  presets: [
    presetUno(), // Hỗ trợ các class giống Tailwind
  ],
  content: {
    pipeline: {
      include: [
        // Quét tất cả các file HTML trong thư mục templates của Spring Boot
        'src/main/resources/templates/**/*.html',
        'src/main/resources/static/**/*.js',
      ],
    },
  },
})