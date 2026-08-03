/** @type {import('tailwindcss').Config} */
export default {
  darkMode: 'class',
  content: ['./index.html', './src/**/*.{js,jsx}'],
  theme: {
    extend: {
      fontFamily: {
        display: ['"Fraunces"', 'ui-serif', 'Georgia', 'serif'],
        sans: ['"Manrope"', 'ui-sans-serif', 'system-ui', 'sans-serif'],
        mono: ['"IBM Plex Mono"', 'ui-monospace', 'SFMono-Regular', 'monospace'],
      },
      colors: {
        ink: {
          DEFAULT: 'rgb(var(--ink) / <alpha-value>)',
          soft: 'rgb(var(--ink-soft) / <alpha-value>)',
          faint: 'rgb(var(--ink-faint) / <alpha-value>)',
        },
        paper: {
          DEFAULT: 'rgb(var(--paper) / <alpha-value>)',
          raised: 'rgb(var(--paper-raised) / <alpha-value>)',
          sunken: 'rgb(var(--paper-sunken) / <alpha-value>)',
        },
        line: 'rgb(var(--line) / <alpha-value>)',
        gold: {
          DEFAULT: '#CBA35C',
          soft: '#E4CE9B',
          deep: '#9C7B3D',
        },
        jade: {
          DEFAULT: '#4FA378',
          soft: '#8FCBAA',
          deep: '#336E52',
        },
        brick: {
          DEFAULT: '#B85C56',
          soft: '#DA9A94',
          deep: '#7E3B36',
        },
        violet: {
          DEFAULT: '#7C6FBE',
          soft: '#B4ACDA',
          deep: '#4F4480',
        },
      },
      boxShadow: {
        glass: '0 1px 0 0 rgba(255,255,255,0.06) inset, 0 20px 50px -20px rgba(0,0,0,0.55)',
        'glass-sm': '0 1px 0 0 rgba(255,255,255,0.05) inset, 0 8px 24px -12px rgba(0,0,0,0.4)',
      },
      backgroundImage: {
        aurora:
          'radial-gradient(120% 140% at 10% -10%, rgba(124,111,190,0.35) 0%, transparent 55%), radial-gradient(100% 120% at 100% 0%, rgba(203,163,92,0.25) 0%, transparent 50%), linear-gradient(180deg, rgba(10,14,23,1) 0%, rgba(10,14,23,1) 100%)',
      },
      borderRadius: {
        xl2: '1.25rem',
      },
    },
  },
  plugins: [],
}
