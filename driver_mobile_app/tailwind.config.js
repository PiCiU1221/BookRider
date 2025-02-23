/** @type {import('tailwindcss').Config} */
module.exports = {
  content: [
    "./app/**/*.tsx",
    "./app/(screens)/**/*.tsx",
  ],
  presets: [
    require("nativewind/preset")
  ],
  theme: {
    extend: {
      colors: {
        theme_background: '#3b576c',
        theme_accent: '#f7ca65',
      }
    },
  },
  plugins: [],
}

