import { useEffect, useState, useRef } from 'react'
import clsx from 'clsx'
import { Drawer, Field, Button, inputClass, invalidInputClass } from '../ui.jsx'
import { pick, formatCurrency } from '../../utils/format.js'
import {
  VALIDATION_MESSAGES,
  getApiErrorMessage,
  isPositiveNumber,
  isValidDate,
  isValidSelection,
  mapApiFieldErrors,
  parseNumber,
} from '../../utils/validation.js'

const TX_TYPES = ['BUY', 'SELL', 'DEPOSIT', 'WITHDRAW', 'INTEREST']

const empty = {
  investmentId: '',
  type: 'BUY',
  quantity: '',
  price: '',
  amount: '',
  transactionDate: new Date().toISOString().slice(0, 10),
}

function validate(form, investments, needsQuantityPrice) {
  const errors = {}

  if (!isValidSelection(form.investmentId)) {
    errors.investmentId = VALIDATION_MESSAGES.selectOption
  }

  if (!isValidSelection(form.type)) {
    errors.type = VALIDATION_MESSAGES.selectOption
  }

  if (!isValidDate(form.transactionDate)) {
    errors.transactionDate = form.transactionDate ? VALIDATION_MESSAGES.invalid : VALIDATION_MESSAGES.required
  }

  if (needsQuantityPrice) {
    if (!isPositiveNumber(form.quantity)) {
      errors.quantity = form.quantity ? VALIDATION_MESSAGES.invalidAmount : VALIDATION_MESSAGES.required
    }
    if (!isPositiveNumber(form.price)) {
      errors.price = form.price ? VALIDATION_MESSAGES.invalidAmount : VALIDATION_MESSAGES.required
    }
  } else if (!isPositiveNumber(form.amount)) {
    errors.amount = form.amount ? VALIDATION_MESSAGES.invalidAmount : VALIDATION_MESSAGES.required
  }

  const selectedInvestment = investments.find((inv) => String(pick(inv, ['id'])) === String(form.investmentId))
  const selectedType = pick(selectedInvestment, ['type', 'investmentType'])
  if (selectedType) {
    if (['BUY', 'SELL'].includes(form.type) && !['STOCK', 'ETF'].includes(selectedType)) {
      errors.type = 'Please select BUY/SELL only for STOCK or ETF investments.'
    }
    if (['DEPOSIT', 'WITHDRAW'].includes(form.type) && selectedType !== 'CASH') {
      errors.type = 'Please select DEPOSIT/WITHDRAW only for CASH investments.'
    }
    if (form.type === 'INTEREST' && selectedType !== 'FD') {
      errors.type = 'Please select INTEREST only for FD investments.'
    }
  }

  return errors
}

export default function TransactionDrawer({ open, onClose, onSubmit, investments = [] }) {
  const [form, setForm] = useState(empty)
  const [errors, setErrors] = useState({})
  const [formError, setFormError] = useState('')
  const [saving, setSaving] = useState(false)
  const [searchInput, setSearchInput] = useState('')
  const [showSuggestions, setShowSuggestions] = useState(false)
  const searchRef = useRef(null)

  useEffect(() => {
    if (open) {
      setForm({ ...empty, investmentId: '' })
      setErrors({})
      setFormError('')
      setSaving(false)
      setSearchInput('')
      setShowSuggestions(false)
    }
  }, [open, investments])

  // close suggestions when clicking outside
  useEffect(() => {
    function handleClick(e) {
      if (searchRef.current && !searchRef.current.contains(e.target)) setShowSuggestions(false)
    }
    document.addEventListener('mousedown', handleClick)
    return () => document.removeEventListener('mousedown', handleClick)
  }, [])

  function set(key, value) {
    setErrors((prev) => ({ ...prev, [key]: '', ...(key === 'investmentId' ? { type: '' } : {}) }))
    setFormError('')
    setForm((f) => ({ ...f, [key]: value }))
  }

  function selectInvestment(inv) {
    const id = pick(inv, ['id'])
    const symbol = pick(inv, ['symbol'])
    const currentPrice = pick(inv, ['currentPrice'])
    setErrors((prev) => ({ ...prev, investmentId: '', type: '', price: '' }))
    setFormError('')
    setForm((f) => ({ ...f, investmentId: id, price: currentPrice != null ? String(currentPrice) : f.price }))
    setSearchInput(symbol)
    setShowSuggestions(false)
  }

  const filtered = investments.filter(inv =>
    pick(inv, ['symbol']).toLowerCase().startsWith(searchInput.toLowerCase())
  )

  const selectedInvestment = investments.find((inv) => String(pick(inv, ['id'])) === String(form.investmentId))
  const selectedCurrency = pick(selectedInvestment, ['currency']) || ''

  const needsQuantityPrice = form.type === 'BUY' || form.type === 'SELL'

  const computedAmount =
    needsQuantityPrice && isPositiveNumber(form.quantity) && isPositiveNumber(form.price)
      ? parseNumber(form.quantity) * parseNumber(form.price)
      : null

  async function submit(e) {
    e.preventDefault()

    const nextErrors = validate(form, investments, needsQuantityPrice)
    if (Object.values(nextErrors).some(Boolean)) {
      setErrors(nextErrors)
      return
    }

    const payload = {
      type: form.type,
      quantity: needsQuantityPrice ? parseNumber(form.quantity) : 0,
      price: needsQuantityPrice ? parseNumber(form.price) : 0,
      amount: form.amount ? parseNumber(form.amount) : parseNumber(form.quantity) * parseNumber(form.price),
      transactionDate: form.transactionDate,
    }

    try {
      setSaving(true)
      setErrors({})
      setFormError('')
      await onSubmit(form.investmentId, payload)
    } catch (error) {
      setErrors(
        mapApiFieldErrors(error, {
          quantity: 'quantity',
          price: 'price',
          amount: 'amount',
          transactionDate: 'transactionDate',
          type: 'type',
        })
      )
      setFormError(getApiErrorMessage(error, 'Unable to save transaction. Please review the form and try again.'))
    } finally {
      setSaving(false)
    }
  }

  return (
    <Drawer
      open={open}
      onClose={onClose}
      title="Record transaction"
      footer={
        <div className="flex justify-end gap-2">
          <Button variant="ghost" onClick={onClose} disabled={saving}>Cancel</Button>
          <Button onClick={submit} disabled={saving}>{saving ? 'Saving...' : 'Save transaction'}</Button>
        </div>
      }
    >
      <form onSubmit={submit} noValidate className="overflow-visible">
        <div className="relative">
          <Field label="Investment" error={errors.investmentId}>
            <div ref={searchRef} className="relative">
              <input
                type="text"
                placeholder="Search by symbol..."
                className={clsx(inputClass, errors.investmentId && invalidInputClass)}
                value={searchInput}
                onChange={(e) => {
                  setSearchInput(e.target.value)
                  setShowSuggestions(true)
                  if (!e.target.value) setErrors((p) => ({ ...p, investmentId: '' }))
                }}
                onFocus={() => setShowSuggestions(true)}
                autoComplete="off"
              />
              {showSuggestions && filtered.length > 0 && (
                <div className="absolute top-full left-0 right-0 mt-1 max-h-64 overflow-y-auto rounded-lg border border-line bg-paper-raised shadow-lg z-[100] pointer-events-auto">
                  {filtered.map((inv) => (
                    <InvestmentSuggestion key={pick(inv, ['id'])} investment={inv} onClick={() => selectInvestment(inv)} />
                  ))}
                </div>
              )}
              {showSuggestions && searchInput && filtered.length === 0 && (
                <div className="absolute top-full left-0 right-0 mt-1 rounded-lg border border-line bg-paper-raised p-3 text-sm text-ink-faint z-[100]">
                  No matching investments found
                </div>
              )}
            </div>
          </Field>
        </div>
        <Field label="Type" error={errors.type}>
          <select className={clsx(inputClass, errors.type && invalidInputClass)} value={form.type} onChange={(e) => set('type', e.target.value)}>
            {TX_TYPES.map((t) => (
              <option key={t} value={t}>{t}</option>
            ))}
          </select>
        </Field>
        {needsQuantityPrice ? (
          <div className="space-y-3">
            <div className="grid grid-cols-2 gap-3">
              <Field label="Quantity" error={errors.quantity}>
                <input type="number" step="any" className={clsx(inputClass, errors.quantity && invalidInputClass)} value={form.quantity} onChange={(e) => set('quantity', e.target.value)} />
              </Field>
              <Field label={`Price / unit${selectedCurrency ? ` (${selectedCurrency})` : ''}`} error={errors.price}>
                <input type="number" step="any" className={clsx(inputClass, errors.price && invalidInputClass)} value={form.price} onChange={(e) => set('price', e.target.value)} />
              </Field>
            </div>
            {computedAmount != null && (
              <div className="flex items-center justify-between rounded-lg bg-paper-sunken px-4 py-2.5">
                <span className="text-sm text-ink-faint">Total amount</span>
                <span className="font-semibold text-ink">
                  {formatCurrency(computedAmount, selectedCurrency || 'USD')}
                </span>
              </div>
            )}
          </div>
        ) : (
          <Field label="Amount" error={errors.amount}>
            <input type="number" step="any" className={clsx(inputClass, errors.amount && invalidInputClass)} value={form.amount} onChange={(e) => set('amount', e.target.value)} />
          </Field>
        )}
        <Field label="Date" error={errors.transactionDate}>
          <input type="date" className={clsx(inputClass, errors.transactionDate && invalidInputClass)} value={form.transactionDate} onChange={(e) => set('transactionDate', e.target.value)} />
        </Field>
        {formError && <p className="text-sm text-brick">{formError}</p>}
      </form>
    </Drawer>
  )
}

function InvestmentSuggestion({ investment, onClick }) {
  const symbol = pick(investment, ['symbol'])
  const name = pick(investment, ['name'])
  const currentPrice = pick(investment, ['currentPrice'])
  const avgBuyPrice = pick(investment, ['avgBuyPrice'])
  const currency = pick(investment, ['currency'])
  const type = pick(investment, ['type', 'investmentType'])

  return (
    <button
      type="button"
      onClick={onClick}
      className="w-full px-3 py-2 text-left hover:bg-paper-sunken transition border-b border-line last:border-b-0 flex items-start justify-between gap-2 text-sm"
    >
      <div className="flex-1 min-w-0">
        <div className="flex items-center gap-2">
          <span className="font-semibold text-ink whitespace-nowrap">{symbol}</span>
          <span className="text-xs text-ink-faint whitespace-nowrap">{type}</span>
        </div>
        <p className="text-xs text-ink-faint truncate">{name}</p>
      </div>
      <div className="text-right text-xs space-y-0.5 flex-shrink-0">
        <div className="whitespace-nowrap">
          <span className="text-ink-faint">Now: </span>
          <span className="text-ink font-medium">{currentPrice} {currency}</span>
        </div>
        <div className="whitespace-nowrap">
          <span className="text-ink-faint">Buy: </span>
          <span className="text-ink font-medium">{avgBuyPrice} {currency}</span>
        </div>
      </div>
    </button>
  )
}
