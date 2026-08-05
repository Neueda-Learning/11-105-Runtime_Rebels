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
        rose: {
          DEFAULT: '#E649A1',
          soft: '#F39BC7',
          deep: '#C92E86',
        },
        jade: {
          DEFAULT: '#10B981',
          soft: '#6EE7B7',
          deep: '#059669',
        },
        brick: {
          DEFAULT: '#F06292',
          soft: '#F7A8C7',
          deep: '#D13D74',
        },
        violet: {
          DEFAULT: '#B48CFF',
          soft: '#DEC9FF',
          deep: '#8E63F4',
        },
      },
      boxShadow: {
        glass: '0 1px 0 0 rgba(255,255,255,0.08) inset, 0 20px 50px -20px rgba(76,22,95,0.55)',
        'glass-sm': '0 1px 0 0 rgba(255,255,255,0.06) inset, 0 10px 28px -14px rgba(109,37,120,0.45)',
      },
      backgroundImage: {
        aurora:
          'radial-gradient(120% 140% at 10% -10%, rgba(230,73,161,0.45) 0%, transparent 55%), radial-gradient(100% 120% at 100% 0%, rgba(142,99,244,0.35) 0%, transparent 50%), linear-gradient(180deg, rgba(26,12,36,1) 0%, rgba(16,8,24,1) 100%)',
      },
      borderRadius: {
        xl2: '1.25rem',
      },
    },
  },
  plugins: [],
}
