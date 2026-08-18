/** @type {import('tailwindcss').Config} */
export default {
  content: ['./index.html', './src/**/*.{vue,js,ts,jsx,tsx}'],
  theme: {
    extend: {
      colors: {
        // Aura warm palette
        aura: {
          bg: '#F5F1EA',
          surface: '#FFFDFC',
          text: '#2E2A26',
          muted: '#81786E',
          accent: '#A98570',
          accentStrong: '#68705A',
          border: '#E5DED4',
          borderStrong: '#D6C8B8',
        },
        // Back-compat aliases for existing views
        primary: '#2E2A26',
        'primary-light': '#5F564D',
        'bg-primary': '#F5F1EA',
        'bg-card': '#FFFDFC',
        'bg-secondary': '#E5DED4',
        'bg-hover': '#EFE7DE',
        'text-primary': '#2E2A26',
        'text-secondary': '#81786E',
        'text-tertiary': '#A89F95',
        border: '#E5DED4',
        'border-light': '#F0E9DE',
        // Warm CTA and utility tones
        'ig-blue': '#A98570',
        'ig-blue-hover': '#68705A',
        'ig-blue-light': '#F0E9DE',
        // Accent (brand moments only)
        accent: {
          start: '#E5DED4',
          via: '#A98570',
          end: '#68705A',
          blue: '#81786E',
        },
        success: '#68705A',
        warning: '#A98570',
        error: '#8B5E4A',
        info: '#81786E',
      },
      fontFamily: {
        sans: [
          '-apple-system',
          'BlinkMacSystemFont',
          'Segoe UI',
          'Roboto',
          'Helvetica Neue',
          'PingFang SC',
          'Microsoft YaHei',
          'sans-serif',
        ],
      },
      fontSize: {
        'caption': ['12px', { lineHeight: '16px' }],
        'body': ['14px', { lineHeight: '20px' }],
        'subtitle': ['16px', { lineHeight: '24px' }],
        'title': ['18px', { lineHeight: '24px', fontWeight: '600' }],
        'heading': ['22px', { lineHeight: '28px', fontWeight: '600' }],
      },
      borderRadius: {
        card: '12px',
        btn: '8px',
        input: '6px',
        tag: '100px',
      },
      boxShadow: {
        'card': '0 1px 2px rgba(0, 0, 0, 0.06)',
        'card-hover': '0 2px 8px rgba(0, 0, 0, 0.08)',
        'sidebar': '1px 0 0 0 #DBDBDB',
        'dropdown': '0 4px 12px rgba(0, 0, 0, 0.15)',
      },
      width: {
        'sidebar': '244px',
        'sidebar-collapsed': '72px',
      },
      spacing: {
        'sidebar': '244px',
        'sidebar-collapsed': '72px',
      },
    },
  },
  plugins: [],
}
