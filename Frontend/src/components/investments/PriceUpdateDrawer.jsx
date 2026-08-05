import { useEffect, useMemo, useState } from 'react'
import clsx from 'clsx'
import { Drawer, Field, Button, inputClass, invalidInputClass } from '../ui.jsx'
import {
  VALIDATION_MESSAGES,
  getApiErrorMessage,
  isPositiveNumber,
  mapApiFieldErrors,
  parseNumber,
} from '../../utils/validation.js'

function validate(form) {
  const errors = {}

  if (!isPositiveNumber(form.currentPrice)) {
    errors.currentPrice = form.currentPrice ? VALIDATION_MESSAGES.invalidAmount : VALIDATION_MESSAGES.required
  }

  return errors
}

export default function PriceUpdateDrawer({ open, investment, onClose, onSubmit }) {
  const current = useMemo(() => String(investment?.currentPrice ?? investment?.price ?? ''), [investment])
  const [form, setForm] = useState({ currentPrice: current })
  const [errors, setErrors] = useState({})
  const [formError, setFormError] = useState('')
  const [saving, setSaving] = useState(false)

  useEffect(() => {
    if (open) {
      setForm({ currentPrice: current })
      setErrors({})
      setFormError('')
      setSaving(false)
    }
  }, [open, current])

  function setValue(value) {
    setForm({ currentPrice: value })
    setFormError('')
    setErrors((prev) => ({ ...prev, currentPrice: '' }))
  }

  async function submit(e) {
    e.preventDefault()
    const nextErrors = validate(form)
    if (nextErrors.currentPrice) {
      setErrors(nextErrors)
      return
    }

    try {
      setSaving(true)
      setFormError('')
      setErrors({})
      await onSubmit({ currentPrice: parseNumber(form.currentPrice) })
      onClose()
    } catch (error) {
      setErrors(mapApiFieldErrors(error, { currentValue: 'currentPrice', currentPrice: 'currentPrice' }))
      setFormError(getApiErrorMessage(error, 'Unable to update price. Please try again.'))
    } finally {
      setSaving(false)
    }
  }

  return (
    <Drawer
      open={open}
      onClose={onClose}
      title="Update market price"
      footer={
        <div className="flex justify-end gap-2">
          <Button variant="ghost" onClick={onClose} disabled={saving}>Cancel</Button>
          <Button onClick={submit} disabled={saving}>{saving ? 'Updating...' : 'Update price'}</Button>
        </div>
      }
    >
      <form onSubmit={submit} className="space-y-1" noValidate>
        <Field
          label="Latest market price / unit"
          hint="Enter a positive amount."
          error={errors.currentPrice}
        >
          <input
            type="number"
            step="any"
            className={clsx(inputClass, errors.currentPrice && invalidInputClass)}
            value={form.currentPrice}
            onChange={(e) => setValue(e.target.value)}
          />
        </Field>
        {formError && <p className="mt-2 text-sm text-brick">{formError}</p>}
      </form>
    </Drawer>
  )
}


