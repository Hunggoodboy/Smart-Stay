import { defineConfig, presetUno } from 'unocss'
import presetIcons from '@unocss/preset-icons'
export default defineConfig({
  presets: [
    presetUno(),
    presetIcons({
      scale: 1.2, // Kích thước mặc định của icon
      extraProperties: {
        'display': 'inline-block',
        'vertical-align': 'middle',
      },
    }),
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