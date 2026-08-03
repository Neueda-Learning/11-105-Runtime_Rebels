import { useEffect, useState } from 'react'
import { Drawer, Field, Button, inputClass } from '../ui.jsx'

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

export default function InvestmentDrawer({ open, onClose, onSubmit, initial }) {
  const [form, setForm] = useState(empty)
  const isEdit = Boolean(initial)

  useEffect(() => {
    setForm(initial ? { ...empty, ...initial } : empty)
  }, [initial, open])

  function set(key, value) {
    setForm((f) => ({ ...f, [key]: value }))
  }

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
        {/* Symbol Field */}
        <Field label="Symbol">
          <input
            required
            className={inputClass}
            placeholder="e.g. Airtel Ltd or AAPL"
            value={form.symbol}
            onChange={(e) => set('symbol', e.target.value)}
          />
        </Field>

        {/* Company / Asset Name Field */}
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
      </form>
    </Drawer>
  )
}