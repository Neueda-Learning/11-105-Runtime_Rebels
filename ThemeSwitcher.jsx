import clsx from 'clsx'
import { Sun, Moon, Laptop } from 'lucide-react'

const OPTIONS = [
  { value: 'light', label: 'Light', icon: Sun },
  { value: 'dark', label: 'Dark', icon: Moon },
  { value: 'system', label: 'System', icon: Laptop },
]

export default function ThemeSwitcher({ theme, setTheme, compact = false, className }) {
  return (
    <div
      className={clsx(
        'inline-flex items-center gap-1 rounded-full border border-line bg-paper-raised/95 p-1 shadow-glass-sm',
        className
      )}
      role="group"
      aria-label="Theme"
    >
      {OPTIONS.map((option) => {
        const Icon = option.icon
        const active = theme === option.value
        return (
          <button
            key={option.value}
            type="button"
            onClick={() => setTheme(option.value)}
            className={clsx(
              'inline-flex items-center rounded-full text-xs font-semibold transition',
              compact ? 'h-8 w-8 justify-center' : 'gap-1.5 px-3 py-1.5',
              active
                ? 'bg-rose/20 text-rose-deep shadow-glass-sm dark:bg-gradient-to-r dark:from-rose dark:to-violet dark:text-white'
                : 'text-ink-soft hover:bg-paper-sunken hover:text-ink'
            )}
            aria-pressed={active}
            title={option.label}
          >
            <span
              className={clsx(
                'inline-flex h-5 w-5 items-center justify-center rounded-full transition',
                active ? 'bg-rose-deep/15 dark:bg-white/20' : 'bg-paper-sunken/80'
              )}
            >
              <Icon className="h-3.5 w-3.5 text-current" />
            </span>
            {!compact && <span>{option.label}</span>}
          </button>
        )
      })}
    </div>
  )
}


