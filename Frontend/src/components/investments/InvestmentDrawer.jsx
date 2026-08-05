import { useEffect, useMemo, useState } from 'react'
import { Drawer, Field, Button, inputClass } from '../ui.jsx'
import { formatCurrency, pick } from '../../utils/format.js'

const TYPES = ['STOCK', 'ETF', 'FD', 'CASH']
const COUNTRIES = ['INDIA', 'US', 'UK', 'EUROPE', 'CHINA']
const CURRENCIES = ['INR', 'USD', 'GBP', 'EUR', 'CNY', 'JPY', 'AED']

const empty = {
  symbol: '',
  name: '',
  type: 'STOCK',
  country: 'INDIA',
  currency: 'INR',
  quantity: '',
  purchasePrice: '',
  currentPrice: '',
}

export default function InvestmentDrawer({ open, investments, onClose, onSubmit, initial }) {
  const [form, setForm] = useState(empty)
  const [symbolQuery, setSymbolQuery] = useState('')
  const isEdit = Boolean(initial)

  useEffect(() => {
    const nextForm = initial ? mapInitial(initial) : empty
    setForm(nextForm)
    setSymbolQuery(nextForm.symbol || '')
  }, [initial, open])

  function set(key, value) {
    setForm((f) => ({ ...f, [key]: value }))
  }

  const suggestions = useMemo(() => {
    const query = normalize(symbolQuery)
    const mapped = investments.map((inv) => ({
      investment: inv,
      symbol: String(pick(inv, ['symbol', 'ticker'], '')).trim(),
      name: pick(inv, ['name', 'companyName'], 'Unnamed'),
      type: pick(inv, ['type', 'investmentType'], 'STOCK'),
      country: pick(inv, ['country', 'market'], 'INDIA'),
      currency: pick(inv, ['currency'], 'INR'),
      purchasePrice: Number(pick(inv, ['avgBuyPrice', 'purchasePrice'], 0)),
      currentPrice: Number(pick(inv, ['currentPrice', 'price', 'marketPrice', 'latestPrice'], 0)),
      searchSymbol: normalize(pick(inv, ['symbol', 'ticker'], '')),
      searchName: normalize(pick(inv, ['name', 'companyName'], '')),
    }))

    if (!query) return []
    return mapped
      .filter((item) => item.searchSymbol.startsWith(query) || item.searchName.startsWith(query))
      .slice(0, 8)
  }, [investments, symbolQuery])

  const hasTypedSymbol = normalize(symbolQuery).length > 0

  const quantityNum = Number(form.quantity || 0)
  const purchaseNum = Number(form.purchasePrice || 0)
  const currentNum = Number(form.currentPrice || form.purchasePrice || 0)
  const investedAmount = quantityNum * purchaseNum
  const currentValue = quantityNum * currentNum
  const unrealizedPl = currentValue - investedAmount

  function submit(e) {
    e.preventDefault()

    // Explicitly construct the exact payload expected by the backend API
    const payload = {
      symbol: (form.symbol || form.name).trim(), // Ensures symbol is never blank
      name: (form.name || form.symbol).trim(),
      type: form.type,
      country: form.country,
      currency: form.currency,
      quantity: Number(form.quantity),
      avgBuyPrice: Number(form.purchasePrice), // Maps purchasePrice to avgBuyPrice for the backend
      currentPrice: form.currentPrice ? Number(form.currentPrice) : Number(form.purchasePrice),
    }

    onSubmit(payload)
  }

  function chooseSuggestion(item) {
    setForm((f) => ({
      ...f,
      symbol: item.symbol || f.symbol,
      name: item.name || f.name,
      type: item.type || f.type,
      country: item.country || f.country,
      currency: item.currency || f.currency,
      purchasePrice: item.purchasePrice > 0 ? String(item.purchasePrice) : f.purchasePrice,
      currentPrice: item.currentPrice > 0 ? String(item.currentPrice) : f.currentPrice,
    }))
    setSymbolQuery(item.symbol || '')
  }

  return (
    <Drawer
      open={open}
      onClose={onClose}
      title={isEdit ? 'Edit investment' : 'Add investment'}
      footer={
        <div className="flex justify-end gap-2">
          <Button variant="ghost" onClick={onClose}>Cancel</Button>
          <Button onClick={submit}>{isEdit ? 'Save changes' : 'Add investment'}</Button>
        </div>
      }
    >
      <form onSubmit={submit} className="space-y-4">
        <Field label="Symbol" hint="Type a symbol prefix to reuse an existing holding's details and prices.">
          <input
            required
            className={inputClass}
            placeholder="e.g. Airtel Ltd or AAPL"
            value={symbolQuery}
            onChange={(e) => {
              const value = e.target.value
              setSymbolQuery(value)
              set('symbol', value)
            }}
          />

          {hasTypedSymbol && (
            <div className="mt-2 max-h-44 overflow-y-auto rounded-lg border border-line bg-paper-raised">
              {suggestions.length === 0 ? (
                <p className="px-3 py-2 text-xs text-ink-faint">No matching symbols for this prefix.</p>
              ) : (
                suggestions.map((item, idx) => (
                  <button
                    key={`${item.symbol}-${idx}`}
                    type="button"
                    onClick={() => chooseSuggestion(item)}
                    className="flex w-full items-center justify-between gap-3 px-3 py-2 text-left text-sm hover:bg-paper-sunken"
                  >
                    <span className="min-w-0">
                      <span className="block truncate font-medium text-ink">{item.symbol || '—'}</span>
                      <span className="block truncate text-xs text-ink-faint">{item.name}</span>
                    </span>
                    <span className="shrink-0 text-right text-xs font-tabular text-ink-faint">
                      <span className="block">Buy {formatCurrency(item.purchasePrice, item.currency)}</span>
                      <span className="block">Now {formatCurrency(item.currentPrice, item.currency)}</span>
                    </span>
                  </button>
                ))
              )}
            </div>
          )}
        </Field>

        <Field label="Company / Holding Name">
          <input
            required
            className={inputClass}
            placeholder="e.g. Airtel Communication Ltd"
            value={form.name}
            onChange={(e) => set('name', e.target.value)}
          />
        </Field>

        <div className="grid grid-cols-2 gap-3">
          <Field label="Type">
            <select className={inputClass} value={form.type} onChange={(e) => set('type', e.target.value)}>
              {TYPES.map((t) => (
                <option key={t} value={t}>{t}</option>
              ))}
            </select>
          </Field>
          <Field label="Country / market">
            <select className={inputClass} value={form.country} onChange={(e) => set('country', e.target.value)}>
              {COUNTRIES.map((c) => (
                <option key={c} value={c}>{c}</option>
              ))}
            </select>
          </Field>
        </div>

        <Field label="Currency">
          <select className={inputClass} value={form.currency} onChange={(e) => set('currency', e.target.value)}>
            {CURRENCIES.map((c) => (
              <option key={c} value={c}>{c}</option>
            ))}
          </select>
        </Field>

        <div className="grid grid-cols-2 gap-3">
          <Field label="Quantity / units">
            <input
              required
              type="number"
              step="any"
              className={inputClass}
              value={form.quantity}
              onChange={(e) => set('quantity', e.target.value)}
            />
          </Field>
          <Field label="Purchase price / unit">
            <input
              required
              type="number"
              step="any"
              className={inputClass}
              value={form.purchasePrice}
              onChange={(e) => set('purchasePrice', e.target.value)}
            />
          </Field>
        </div>

        <Field label="Current price / unit" hint="Leave blank to use the purchase price initially">
          <input
            type="number"
            step="any"
            className={inputClass}
            value={form.currentPrice}
            onChange={(e) => set('currentPrice', e.target.value)}
          />
        </Field>

        <div className="rounded-lg border border-line bg-paper-sunken/50 p-3 text-sm">
          <p className="mb-2 font-semibold text-ink">Calculation preview</p>
          <p className="text-ink-faint">Invested amount = Quantity x Purchase price</p>
          <p className="font-tabular text-ink">
            {quantityNum || 0} x {purchaseNum || 0} = {formatCurrency(investedAmount, form.currency)}
          </p>

          <p className="mt-2 text-ink-faint">Current value = Quantity x Current price</p>
          <p className="font-tabular text-ink">
            {quantityNum || 0} x {currentNum || 0} = {formatCurrency(currentValue, form.currency)}
          </p>

          <p className="mt-2 text-ink-faint">Unrealized P/L = Current value - Invested amount</p>
          <p className="font-tabular text-ink">{formatCurrency(unrealizedPl, form.currency)}</p>
        </div>
      </form>
    </Drawer>
  )
}

function normalize(value) {
  return String(value || '').trim().toLowerCase()
}

function mapInitial(initial) {
  return {
    ...empty,
    ...initial,
    purchasePrice: String(pick(initial, ['purchasePrice', 'avgBuyPrice'], '')),
    currentPrice: String(pick(initial, ['currentPrice', 'price'], '')),
    quantity: String(pick(initial, ['quantity'], '')),
    symbol: String(pick(initial, ['symbol', 'ticker'], '')),
    name: String(pick(initial, ['name', 'companyName'], '')),
    type: pick(initial, ['type', 'investmentType'], 'STOCK'),
    country: pick(initial, ['country', 'market'], 'INDIA'),
    currency: pick(initial, ['currency'], 'INR'),
  }
}