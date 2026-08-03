import { createContext, useContext, useEffect, useMemo, useState, useCallback } from 'react'
import { AnimatePresence, motion } from 'framer-motion'
import { CheckCircle2, AlertCircle, X } from 'lucide-react'
import { getBaseCurrency } from '../api/client'

const AppCtx = createContext(null)

function useProvideTheme() {
  const [theme, setTheme] = useState(() => localStorage.getItem('pm-theme') || 'system')

  useEffect(() => {
    const root = document.documentElement
    const apply = (t) => {
      const isDark =
        t === 'dark' || (t === 'system' && window.matchMedia('(prefers-color-scheme: dark)').matches)
      root.classList.toggle('dark', isDark)
    }
    apply(theme)
    localStorage.setItem('pm-theme', theme)
    if (theme === 'system') {
      const mq = window.matchMedia('(prefers-color-scheme: dark)')
      const listener = () => apply('system')
      mq.addEventListener('change', listener)
      return () => mq.removeEventListener('change', listener)
    }
  }, [theme])

  return { theme, setTheme }
}

function useProvideToasts() {
  const [toasts, setToasts] = useState([])
  const push = useCallback((message, variant = 'success') => {
    const id = Math.random().toString(36).slice(2)
    setToasts((t) => [...t, { id, message, variant }])
    setTimeout(() => setToasts((t) => t.filter((x) => x.id !== id)), 4000)
  }, [])
  const dismiss = useCallback((id) => setToasts((t) => t.filter((x) => x.id !== id)), [])
  return { toasts, push, dismiss }
}

export function AppProvider({ children }) {
  const theme = useProvideTheme()
  const toasts = useProvideToasts()
  const [baseCurrency, setBaseCurrencyState] = useState('INR')

  useEffect(() => {
  getBaseCurrency()
    .then((res) => {
      const code = res?.baseCurrency || res?.currencyCode || res?.currency || res?.code
      if (code) setBaseCurrencyState(code)
    })
    .catch(() => {})
}, [])

  const value = useMemo(
    () => ({
      ...theme,
      ...toasts,
      baseCurrency,
      setBaseCurrency: setBaseCurrencyState,
    }),
    [theme, toasts, baseCurrency]
  )

  return (
    <AppCtx.Provider value={value}>
      {children}
      <ToastHost toasts={toasts.toasts} dismiss={toasts.dismiss} />
    </AppCtx.Provider>
  )
}

export function useApp() {
  const ctx = useContext(AppCtx)
  if (!ctx) throw new Error('useApp must be used within AppProvider')
  return ctx
}

function ToastHost({ toasts, dismiss }) {
  return (
    <div className="fixed bottom-4 right-4 z-[100] flex w-[calc(100%-2rem)] max-w-sm flex-col gap-2 sm:bottom-6 sm:right-6">
      <AnimatePresence>
        {toasts.map((t) => (
          <motion.div
            key={t.id}
            initial={{ opacity: 0, y: 12, scale: 0.98 }}
            animate={{ opacity: 1, y: 0, scale: 1 }}
            exit={{ opacity: 0, y: 8, scale: 0.98 }}
            className="flex items-start gap-2.5 rounded-xl2 border border-line bg-paper-raised px-4 py-3 shadow-glass-sm"
          >
            {t.variant === 'error' ? (
              <AlertCircle className="mt-0.5 h-4 w-4 shrink-0 text-brick" />
            ) : (
              <CheckCircle2 className="mt-0.5 h-4 w-4 shrink-0 text-jade" />
            )}
            <p className="flex-1 text-sm text-ink">{t.message}</p>
            <button onClick={() => dismiss(t.id)} className="text-ink-faint hover:text-ink">
              <X className="h-3.5 w-3.5" />
            </button>
          </motion.div>
        ))}
      </AnimatePresence>
    </div>
  )
}
