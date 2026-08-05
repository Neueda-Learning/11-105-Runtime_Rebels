import { useEffect, useState } from 'react'
import clsx from 'clsx'
import { Drawer, Field, Button, inputClass, invalidInputClass } from '../ui.jsx'
import { pick } from '../../utils/format.js'
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

  useEffect(() => {
    if (open) {
      setForm({ ...empty, investmentId: investments[0] ? pick(investments[0], ['id']) : '' })
      setErrors({})
      setFormError('')
      setSaving(false)
    }
  }, [open, investments])

  function set(key, value) {
    setErrors((prev) => ({ ...prev, [key]: '', ...(key === 'investmentId' ? { type: '' } : {}) }))
    setFormError('')
    setForm((f) => ({ ...f, [key]: value }))
  }

  const needsQuantityPrice = form.type === 'BUY' || form.type === 'SELL'

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
      <form onSubmit={submit} noValidate>
        <Field label="Investment" error={errors.investmentId}>
          <select className={clsx(inputClass, errors.investmentId && invalidInputClass)} value={form.investmentId} onChange={(e) => set('investmentId', e.target.value)}>
            {investments.map((inv) => (
              <option key={pick(inv, ['id'])} value={pick(inv, ['id'])}>
                {pick(inv, ['name', 'symbol'], 'Unnamed')}
              </option>
            ))}
          </select>
        </Field>
        <Field label="Type" error={errors.type}>
          <select className={clsx(inputClass, errors.type && invalidInputClass)} value={form.type} onChange={(e) => set('type', e.target.value)}>
            {TX_TYPES.map((t) => (
              <option key={t} value={t}>{t}</option>
            ))}
          </select>
        </Field>
        {needsQuantityPrice ? (
          <div className="grid grid-cols-2 gap-3">
            <Field label="Quantity" error={errors.quantity}>
              <input type="number" step="any" className={clsx(inputClass, errors.quantity && invalidInputClass)} value={form.quantity} onChange={(e) => set('quantity', e.target.value)} />
            </Field>
            <Field label="Price / unit" error={errors.price}>
              <input type="number" step="any" className={clsx(inputClass, errors.price && invalidInputClass)} value={form.price} onChange={(e) => set('price', e.target.value)} />
            </Field>
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
