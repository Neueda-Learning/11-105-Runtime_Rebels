// The Swagger doc lists schema names (DashboardResponse, InvestmentResponse, ...)
// but not their expanded field lists, so this app reads response fields
// defensively via `pick()` -- trying a few common naming variants for each
// value. If your Spring DTOs use different field names, add them to the
// candidate arrays below (or the call sites) rather than hunting through
// every component.
export function pick(obj, keys, fallback = undefined) {
  if (!obj) return fallback
  for (const key of keys) {
    const val = key.split('.').reduce((o, k) => (o == null ? undefined : o[k]), obj)
    if (val !== undefined && val !== null) return val
  }
  return fallback
}

const CURRENCY_LOCALE = {
  INR: 'en-IN',
  USD: 'en-US',
  GBP: 'en-GB',
  EUR: 'de-DE',
  CNY: 'zh-CN',
  JPY: 'ja-JP',
  AED: 'ar-AE',
}

export function formatCurrency(amount, currency = 'INR', opts = {}) {
  const value = Number(amount ?? 0)
  const locale = CURRENCY_LOCALE[currency] || 'en-IN'
  try {
    return new Intl.NumberFormat(locale, {
      style: 'currency',
      currency,
      maximumFractionDigits: opts.decimals ?? (Math.abs(value) >= 1000 ? 0 : 2),
      minimumFractionDigits: 0,
      ...opts,
    }).format(value)
  } catch {
    return `${currency} ${value.toLocaleString(locale)}`
  }
}

export function formatCompact(amount, currency = 'INR') {
  const value = Number(amount ?? 0)
  const locale = CURRENCY_LOCALE[currency] || 'en-IN'
  try {
    return new Intl.NumberFormat(locale, {
      style: 'currency',
      currency,
      notation: 'compact',
      maximumFractionDigits: 1,
    }).format(value)
  } catch {
    return formatCurrency(value, currency)
  }
}

export function formatPercent(value, opts = {}) {
  const n = Number(value ?? 0)
  const sign = n > 0 ? '+' : ''
  return `${sign}${n.toFixed(opts.decimals ?? 1)}%`
}

export function formatDate(value) {
  if (!value) return '—'
  const d = new Date(value)
  if (Number.isNaN(d.getTime())) return String(value)
  return d.toLocaleDateString('en-IN', { day: '2-digit', month: 'short', year: 'numeric' })
}

export function formatDateTime(value) {
  if (!value) return '—'
  const d = new Date(value)
  if (Number.isNaN(d.getTime())) return String(value)
  return d.toLocaleString('en-IN', {
    day: '2-digit',
    month: 'short',
    hour: '2-digit',
    minute: '2-digit',
  })
}

export function classForChange(n) {
  const v = Number(n ?? 0)
  if (v > 0) return 'text-jade'
  if (v < 0) return 'text-brick'
  return 'text-ink-faint'
}
