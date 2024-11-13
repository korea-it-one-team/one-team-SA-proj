/** @type {import('tailwindcss').Config} */
module.exports = {
  content: [],
  theme: {
    extend: {},
  },
  plugins: [
    require('daisyui'), // DaisyUI 플러그인 추가
  ],
  daisyui: {
    styled: false, // DaisyUI의 기본 스타일 비활성화
    themes: true,  // 테마 관련 기능은 활성화
  },
}

