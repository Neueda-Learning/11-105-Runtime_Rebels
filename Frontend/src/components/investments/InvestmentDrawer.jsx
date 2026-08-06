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

const TYPES = ['STOCK', 'ETF', 'FD', 'CASH', 'COMMODITY']
const COMMODITY_TYPES = ['GOLD', 'SILVER', 'CRUDE_OIL', 'NATURAL_GAS', 'COPPER', 'PLATINUM', 'OTHER']
const COUNTRIES = ['INDIA', 'US', 'UK', 'EUROPE', 'CHINA']
const CURRENCIES = ['INR', 'USD', 'GBP', 'EUR', 'CNY', 'JPY', 'AED']

const empty = {
  symbol: '',
  name: '',
  type: 'STOCK',
  country: 'INDIA',
  currency: 'INR',
  market: '',
  commodityType: 'GOLD',
  quantity: '',
  purchasePrice: '',
  currentPrice: '',
  purchaseDate: new Date().toISOString().slice(0, 10),
}

function validate(form) {
  const errors = {}
  const isCommodity = form.type === 'COMMODITY'

  if (!toTrimmedString(form.symbol)) errors.symbol = VALIDATION_MESSAGES.required
  if (!toTrimmedString(form.name)) errors.name = VALIDATION_MESSAGES.required
  if (!isValidSelection(form.type)) errors.type = VALIDATION_MESSAGES.selectOption
  if (!isValidSelection(form.country)) errors.country = VALIDATION_MESSAGES.selectOption
  if (!isValidSelection(form.currency)) errors.currency = VALIDATION_MESSAGES.selectOption

  if (isCommodity && !toTrimmedString(form.market)) {
    errors.market = VALIDATION_MESSAGES.required
  }

  if (isCommodity && !isValidSelection(form.commodityType)) {
    errors.commodityType = VALIDATION_MESSAGES.selectOption
  }

  if (!isPositiveNumber(form.quantity)) {
    errors.quantity = form.quantity ? VALIDATION_MESSAGES.invalidAmount : VALIDATION_MESSAGES.required
  }

  if (!isPositiveNumber(form.purchasePrice)) {
    errors.purchasePrice = form.purchasePrice ? VALIDATION_MESSAGES.invalidAmount : VALIDATION_MESSAGES.required
  }

  if (String(form.currentPrice ?? '').trim() && !isPositiveNumber(form.currentPrice)) {
    errors.currentPrice = VALIDATION_MESSAGES.invalidAmount
  }

  if (isCommodity && !toTrimmedString(form.purchaseDate)) {
    errors.purchaseDate = VALIDATION_MESSAGES.required
  }

  return errors
}

export default function InvestmentDrawer({ open, onClose, onSubmit, initial }) {
  const [form, setForm] = useState(empty)
  const [errors, setErrors] = useState({})
  const [formError, setFormError] = useState('')
  const [saving, setSaving] = useState(false)
  const isEdit = Boolean(initial)
  const isCommodity = form.type === 'COMMODITY'

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
      market: toTrimmedString(form.market),
      commodityType: form.commodityType,
      quantity: parseNumber(form.quantity),
      avgBuyPrice: parseNumber(form.purchasePrice),
      currentPrice: String(form.currentPrice ?? '').trim()
        ? parseNumber(form.currentPrice)
        : parseNumber(form.purchasePrice),
      purchaseDate: form.purchaseDate || null,
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
          market: 'market',
          commodityType: 'commodityType',
          currentPrice: 'currentPrice',
          purchaseDate: 'purchaseDate',
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
        <Field label={isCommodity ? 'Commodity symbol / code' : 'Symbol'} error={errors.symbol}>
          <input
            className={clsx(inputClass, errors.symbol && invalidInputClass)}
            placeholder={isCommodity ? 'e.g. GOLD or CRUDE_OIL' : 'e.g. Airtel Ltd or AAPL'}
            value={form.symbol}
            onChange={(e) => set('symbol', e.target.value)}
          />
        </Field>

        <Field label={isCommodity ? 'Commodity Name' : 'Company / Holding Name'} error={errors.name}>
          <input
            className={clsx(inputClass, errors.name && invalidInputClass)}
            placeholder={isCommodity ? 'e.g. Gold 24K' : 'e.g. Airtel Communication Ltd'}
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

        {isCommodity && (
          <div className="grid grid-cols-2 gap-3">
            <Field label="Commodity type" error={errors.commodityType}>
              <select
                className={clsx(inputClass, errors.commodityType && invalidInputClass)}
                value={form.commodityType}
                onChange={(e) => set('commodityType', e.target.value)}
              >
                {COMMODITY_TYPES.map((c) => (
                  <option key={c} value={c}>{c}</option>
                ))}
              </select>
            </Field>
            <Field label="Market / exchange" error={errors.market}>
              <input
                className={clsx(inputClass, errors.market && invalidInputClass)}
                placeholder="e.g. MCX"
                value={form.market}
                onChange={(e) => set('market', e.target.value)}
              />
            </Field>
          </div>
        )}

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
        {isCommodity && (
          <Field label="Purchase date" error={errors.purchaseDate}>
            <input
              type="date"
              className={clsx(inputClass, errors.purchaseDate && invalidInputClass)}
              value={form.purchaseDate}
              onChange={(e) => set('purchaseDate', e.target.value)}
            />
          </Field>
        )}
        {formError && <p className="text-sm text-brick">{formError}</p>}
      </form>
    </Drawer>
  )
}