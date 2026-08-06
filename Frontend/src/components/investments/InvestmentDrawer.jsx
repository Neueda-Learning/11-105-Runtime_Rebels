import { useEffect, useState } from 'react'
import clsx from 'clsx'
import { Drawer, Field, Button, inputClass, invalidInputClass } from '../ui.jsx'
import {
  VALIDATION_MESSAGES,
  getApiErrorMessage,
  isPositiveNumber,
  isValidSelection,
  mapApiFieldErrors,
  parseNumber,
  toTrimmedString,
} from '../../utils/validation.js'
import { pick } from '../../utils/format.js'

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

function normalizeSymbol(value) {
  return toTrimmedString(value).toUpperCase()
}

function validate(form) {
  const errors = {}

  if (!toTrimmedString(form.symbol)) errors.symbol = VALIDATION_MESSAGES.required
  if (!toTrimmedString(form.name)) errors.name = VALIDATION_MESSAGES.required
  if (!isValidSelection(form.type)) errors.type = VALIDATION_MESSAGES.selectOption
  if (!isValidSelection(form.country)) errors.country = VALIDATION_MESSAGES.selectOption
  if (!isValidSelection(form.currency)) errors.currency = VALIDATION_MESSAGES.selectOption

  if (!isPositiveNumber(form.quantity)) {
    errors.quantity = form.quantity ? VALIDATION_MESSAGES.invalidAmount : VALIDATION_MESSAGES.required
  }

  if (!isPositiveNumber(form.purchasePrice)) {
    errors.purchasePrice = form.purchasePrice ? VALIDATION_MESSAGES.invalidAmount : VALIDATION_MESSAGES.required
  }

  if (String(form.currentPrice ?? '').trim() && !isPositiveNumber(form.currentPrice)) {
    errors.currentPrice = VALIDATION_MESSAGES.invalidAmount
  }

  return errors
}

export default function InvestmentDrawer({ open, onClose, onSubmit, initial, investments = [] }) {
  const [form, setForm] = useState(empty)
  const [errors, setErrors] = useState({})
  const [formError, setFormError] = useState('')
  const [saving, setSaving] = useState(false)
  const isEdit = Boolean(initial)
  const normalizedSymbol = normalizeSymbol(form.symbol)
  const normalizedName = toTrimmedString(form.name).toLowerCase()

  const symbolSuggestions = !isEdit && normalizedSymbol
    ? investments
        .filter((investment) => {
          const symbol = normalizeSymbol(pick(investment, ['symbol']))
          const name = toTrimmedString(pick(investment, ['name'], '')).toLowerCase()
          return symbol.startsWith(normalizedSymbol) || (normalizedName && name.startsWith(normalizedName)) || name.startsWith(normalizedSymbol.toLowerCase())
        })
        .slice(0, 6)
    : []

  const exactMergeMatch = !isEdit && normalizedSymbol
    ? investments.find((investment) => {
        const symbol = normalizeSymbol(pick(investment, ['symbol']))
        const type = pick(investment, ['type'])
        return symbol === normalizedSymbol && type === form.type
      })
    : null

  useEffect(() => {
    if (open) {
      setForm(initial ? { ...empty, ...initial } : empty)
      setErrors({})
      setFormError('')
      setSaving(false)
    }
  }, [initial, open])

  function set(key, value) {
    setErrors((prev) => ({ ...prev, [key]: '' }))
    setFormError('')
    setForm((f) => ({ ...f, [key]: value }))
  }

  function applySuggestion(investment) {
    setErrors((prev) => ({ ...prev, symbol: '', name: '' }))
    setFormError('')
    setForm((current) => ({
      ...current,
      symbol: pick(investment, ['symbol'], current.symbol),
      name: pick(investment, ['name'], current.name),
      type: pick(investment, ['type'], current.type),
      country: pick(investment, ['country'], current.country),
      currency: pick(investment, ['currency'], current.currency),
    }))
  }

  async function submit(e) {
    e.preventDefault()

    const nextErrors = validate(form)
    if (Object.values(nextErrors).some(Boolean)) {
      setErrors(nextErrors)
      return
    }

    const payload = {
      symbol: toTrimmedString(form.symbol || form.name),
      name: toTrimmedString(form.name || form.symbol),
      type: form.type,
      country: form.country,
      currency: form.currency,
      quantity: parseNumber(form.quantity),
      avgBuyPrice: parseNumber(form.purchasePrice),
      currentPrice: String(form.currentPrice ?? '').trim()
        ? parseNumber(form.currentPrice)
        : parseNumber(form.purchasePrice),
    }

    try {
      setSaving(true)
      setErrors({})
      setFormError('')
      await onSubmit(payload)
    } catch (error) {
      setErrors(
        mapApiFieldErrors(error, {
          avgBuyPrice: 'purchasePrice',
          quantity: 'quantity',
          symbol: 'symbol',
          name: 'name',
          type: 'type',
          country: 'country',
          currency: 'currency',
          currentPrice: 'currentPrice',
        })
      )
      setFormError(getApiErrorMessage(error, 'Unable to save investment. Please review the form and try again.'))
    } finally {
      setSaving(false)
    }
  }

  return (
    <Drawer
      open={open}
      onClose={onClose}
      title={isEdit ? 'Edit investment' : 'Add investment'}
      footer={
        <div className="flex justify-end gap-2">
          <Button variant="ghost" onClick={onClose} disabled={saving}>Cancel</Button>
          <Button onClick={submit} disabled={saving}>{saving ? 'Saving...' : isEdit ? 'Save changes' : 'Add investment'}</Button>
        </div>
      }
    >
      <form onSubmit={submit} className="space-y-4" noValidate>
        {/* Symbol Field */}
        <Field
          label="Symbol"
          error={errors.symbol}
          hint={exactMergeMatch ? 'Exact match found. Saving will add to this existing holding instead of creating a duplicate.' : 'Start typing a symbol to see matching holdings.'}
        >
          <div>
            <input
              className={clsx(inputClass, errors.symbol && invalidInputClass)}
              placeholder="e.g. AAPL or RELIANCE"
              value={form.symbol}
              onChange={(e) => set('symbol', e.target.value)}
            />

            {symbolSuggestions.length > 0 && (
              <div className="mt-2 overflow-hidden rounded-xl border border-line bg-paper-raised shadow-glass-sm">
                {symbolSuggestions.map((investment) => {
                  const symbol = pick(investment, ['symbol'], 'Unknown')
                  const name = pick(investment, ['name'], 'Unnamed holding')
                  const type = pick(investment, ['type'], 'Investment')

                  return (
                    <button
                      key={pick(investment, ['id'], `${symbol}-${type}`)}
                      type="button"
                      className="flex w-full items-center justify-between gap-3 border-b border-line/60 px-3 py-2 text-left last:border-b-0 hover:bg-paper-sunken"
                      onClick={() => applySuggestion(investment)}
                    >
                      <div>
                        <p className="text-sm font-semibold text-ink">{symbol}</p>
                        <p className="text-xs text-ink-faint">{name}</p>
                      </div>
                      <span className="shrink-0 rounded-full bg-paper-sunken px-2 py-1 text-[11px] font-semibold uppercase tracking-wide text-ink-faint">
                        {type}
                      </span>
                    </button>
                  )
                })}
              </div>
            )}
          </div>
        </Field>

        {/* Company / Asset Name Field */}
        <Field label="Company / Holding Name" error={errors.name}>
          <input
            className={clsx(inputClass, errors.name && invalidInputClass)}
            placeholder="e.g. Airtel Communication Ltd"
            value={form.name}
            onChange={(e) => set('name', e.target.value)}
          />
        </Field>

        <div className="grid grid-cols-2 gap-3">
          <Field label="Type" error={errors.type}>
            <select className={clsx(inputClass, errors.type && invalidInputClass)} value={form.type} onChange={(e) => set('type', e.target.value)}>
              {TYPES.map((t) => (
                <option key={t} value={t}>{t}</option>
              ))}
            </select>
          </Field>
          <Field label="Country / market" error={errors.country}>
            <select className={clsx(inputClass, errors.country && invalidInputClass)} value={form.country} onChange={(e) => set('country', e.target.value)}>
              {COUNTRIES.map((c) => (
                <option key={c} value={c}>{c}</option>
              ))}
            </select>
          </Field>
        </div>

        <Field label="Currency" error={errors.currency}>
          <select className={clsx(inputClass, errors.currency && invalidInputClass)} value={form.currency} onChange={(e) => set('currency', e.target.value)}>
            {CURRENCIES.map((c) => (
              <option key={c} value={c}>{c}</option>
            ))}
          </select>
        </Field>

        <div className="grid grid-cols-2 gap-3">
          <Field label="Quantity / units" error={errors.quantity}>
            <input
              type="number"
              step="any"
              className={clsx(inputClass, errors.quantity && invalidInputClass)}
              value={form.quantity}
              onChange={(e) => set('quantity', e.target.value)}
            />
          </Field>
          <Field label="Purchase price / unit" error={errors.purchasePrice}>
            <input
              type="number"
              step="any"
              className={clsx(inputClass, errors.purchasePrice && invalidInputClass)}
              value={form.purchasePrice}
              onChange={(e) => set('purchasePrice', e.target.value)}
            />
          </Field>
        </div>

        <Field label="Current price / unit" hint="Leave blank to use the purchase price initially" error={errors.currentPrice}>
          <input
            type="number"
            step="any"
            className={clsx(inputClass, errors.currentPrice && invalidInputClass)}
            value={form.currentPrice}
            onChange={(e) => set('currentPrice', e.target.value)}
          />
        </Field>
        {formError && <p className="text-sm text-brick">{formError}</p>}
      </form>
    </Drawer>
  )
}