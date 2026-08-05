import { AnimatePresence, motion } from 'framer-motion'
import clsx from 'clsx'
import { X } from 'lucide-react'

export function Card({ className, children, ...rest }) {
  return (
    <div
      className={clsx(
        'rounded-xl2 border border-line/70 bg-gradient-to-b from-paper-raised/95 to-paper/85 p-5 shadow-glass-sm backdrop-blur-sm',
        className
      )}
      {...rest}
    >
      {children}
    </div>
  )
}

export function SectionHeading({ eyebrow, title, action }) {
  return (
    <div className="mb-4 flex items-end justify-between gap-3">
      <div>
        {eyebrow && (
          <p className="mb-1 text-[11px] font-semibold uppercase tracking-[0.14em] text-ink-faint">
            {eyebrow}
          </p>
        )}
        <h2 className="font-display text-lg text-ink">{title}</h2>
      </div>
      {action}
    </div>
  )
}

export function Button({ className, variant = 'primary', size = 'md', children, ...rest }) {
  const base =
    'inline-flex items-center justify-center gap-2 rounded-full font-semibold transition active:scale-[0.98] disabled:opacity-50 disabled:pointer-events-none'
  const sizes = {
    sm: 'px-3 py-1.5 text-xs',
    md: 'px-4 py-2.5 text-sm',
    lg: 'px-6 py-3 text-sm',
  }
  const variants = {
    primary: 'bg-gradient-to-r from-rose to-violet text-white hover:from-rose-deep hover:to-violet-deep shadow-glass-sm',
    ghost: 'bg-transparent text-ink hover:bg-paper-sunken border border-line',
    subtle: 'bg-paper-sunken text-ink hover:bg-line/20',
    danger: 'bg-brick/10 text-brick hover:bg-brick/20 border border-brick/30',
  }
  return (
    <button className={clsx(base, sizes[size], variants[variant], className)} {...rest}>
      {children}
    </button>
  )
}

export function Badge({ children, tone = 'neutral', className }) {
  const tones = {
    neutral: 'bg-paper-sunken text-ink-soft',
    rose: 'bg-rose/15 text-rose-deep dark:text-rose-soft',
    jade: 'bg-jade/15 text-jade-deep dark:text-jade-soft',
    brick: 'bg-brick/15 text-brick-deep dark:text-brick-soft',
    violet: 'bg-violet/15 text-violet-deep dark:text-violet-soft',
  }
  return (
    <span
      className={clsx(
        'inline-flex items-center rounded-full px-2.5 py-1 text-[11px] font-semibold uppercase tracking-wide',
        tones[tone],
        className
      )}
    >
      {children}
    </span>
  )
}

export function Skeleton({ className }) {
  return <div className={clsx('animate-pulse rounded-lg bg-paper-sunken', className)} />
}

export function EmptyState({ icon: Icon, title, description, action }) {
  return (
    <div className="flex flex-col items-center justify-center rounded-xl2 border border-dashed border-line px-6 py-14 text-center">
      {Icon && (
        <div className="mb-4 flex h-12 w-12 items-center justify-center rounded-full bg-paper-sunken">
          <Icon className="h-5 w-5 text-ink-faint" />
        </div>
      )}
      <p className="font-display text-base text-ink">{title}</p>
      {description && <p className="mt-1.5 max-w-sm text-sm text-ink-faint">{description}</p>}
      {action && <div className="mt-5">{action}</div>}
    </div>
  )
}

export function ErrorState({ message, onRetry }) {
  return (
    <div className="flex flex-col items-center justify-center rounded-xl2 border border-brick/30 bg-brick/5 px-6 py-10 text-center">
      <p className="text-sm font-medium text-brick">{message || 'Could not load this data.'}</p>
      {onRetry && (
        <Button variant="ghost" size="sm" className="mt-4" onClick={onRetry}>
          Try again
        </Button>
      )}
    </div>
  )
}

export function Drawer({ open, onClose, title, children, footer }) {
  return (
    <AnimatePresence>
      {open && (
        <>
          <motion.div
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
            exit={{ opacity: 0 }}
            className="fixed inset-0 z-40 bg-black/50 backdrop-blur-[2px]"
            onClick={onClose}
          />
          <motion.div
            initial={{ x: '100%' }}
            animate={{ x: 0 }}
            exit={{ x: '100%' }}
            transition={{ type: 'spring', damping: 30, stiffness: 300 }}
            className="fixed right-0 top-0 z-50 flex h-full w-full max-w-md flex-col border-l border-line bg-paper shadow-glass"
          >
            <div className="flex items-center justify-between border-b border-line px-6 py-5">
              <h3 className="font-display text-lg text-ink">{title}</h3>
              <button
                onClick={onClose}
                className="flex h-8 w-8 items-center justify-center rounded-full hover:bg-paper-sunken"
              >
                <X className="h-4 w-4" />
              </button>
            </div>
            <div className="flex-1 overflow-y-auto px-6 py-5">{children}</div>
            {footer && <div className="border-t border-line px-6 py-4">{footer}</div>}
          </motion.div>
        </>
      )}
    </AnimatePresence>
  )
}

export function ConfirmDialog({ open, title, description, onConfirm, onCancel, confirmLabel = 'Delete' }) {
  return (
    <AnimatePresence>
      {open && (
        <>
          <motion.div
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
            exit={{ opacity: 0 }}
            className="fixed inset-0 z-40 bg-black/50 backdrop-blur-[2px]"
            onClick={onCancel}
          />
          <motion.div
            initial={{ opacity: 0, scale: 0.96, y: 8 }}
            animate={{ opacity: 1, scale: 1, y: 0 }}
            exit={{ opacity: 0, scale: 0.96, y: 8 }}
            className="fixed left-1/2 top-1/2 z-50 w-[calc(100%-2rem)] max-w-sm -translate-x-1/2 -translate-y-1/2 rounded-xl2 border border-line bg-paper-raised p-6 shadow-glass"
          >
            <p className="font-display text-base text-ink">{title}</p>
            {description && <p className="mt-2 text-sm text-ink-faint">{description}</p>}
            <div className="mt-6 flex justify-end gap-2">
              <Button variant="ghost" size="sm" onClick={onCancel}>
                Cancel
              </Button>
              <Button variant="danger" size="sm" onClick={onConfirm}>
                {confirmLabel}
              </Button>
            </div>
          </motion.div>
        </>
      )}
    </AnimatePresence>
  )
}

export function Field({ label, children, hint }) {
  return (
    <label className="mb-4 block">
      <span className="mb-1.5 block text-xs font-semibold uppercase tracking-wide text-ink-faint">
        {label}
      </span>
      {children}
      {hint && <span className="mt-1 block text-xs text-ink-faint">{hint}</span>}
    </label>
  )
}

export const inputClass =
  'w-full rounded-lg border border-line bg-paper px-3 py-2.5 text-sm text-ink outline-none transition focus:border-violet focus:ring-2 focus:ring-violet/25'
