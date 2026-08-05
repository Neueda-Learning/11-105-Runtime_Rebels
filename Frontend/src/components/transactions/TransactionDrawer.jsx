import { useEffect, useState } from 'react'
import { Drawer, Field, Button, inputClass } from '../ui.jsx'
import { pick } from '../../utils/format.js'

const TX_TYPES = ['BUY', 'SELL', 'DEPOSIT', 'WITHDRAW', 'INTEREST']

const empty = {
  investmentId: '',
  type: 'BUY',
  quantity: '',
  price: '',
  amount: '',
  date: new Date().toISOString().slice(0, 10),
}

export default function TransactionDrawer({ open, onClose, onSubmit, investments = [] }) {
  const [form, setForm] = useState(empty)

  useEffect(() => {
    if (open) setForm({ ...empty, investmentId: investments[0] ? pick(investments[0], ['id']) : '' })
  }, [open]) // eslint-disable-line react-hooks/exhaustive-deps

  function set(key, value) {
    setForm((f) => ({ ...f, [key]: value }))
  }

  const needsQuantityPrice = form.type === 'BUY' || form.type === 'SELL'

  function submit(e) {
    e.preventDefault()
    onSubmit(form.investmentId, {
      type: form.type,
      quantity: needsQuantityPrice ? Number(form.quantity) : undefined,
      price: needsQuantityPrice ? Number(form.price) : undefined,
      amount: form.amount ? Number(form.amount) : undefined,
      date: form.date,
    })
  }

  return (
    <Drawer
      open={open}
      onClose={onClose}
      title="Record transaction"
      footer={
        <div className="flex justify-end gap-2">
          <Button variant="ghost" onClick={onClose}>Cancel</Button>
          <Button onClick={submit}>Save transaction</Button>
        </div>
      }
    >
      <form onSubmit={submit}>
        <Field label="Investment">
          <select className={inputClass} value={form.investmentId} onChange={(e) => set('investmentId', e.target.value)}>
            {investments.map((inv) => (
              <option key={pick(inv, ['id'])} value={pick(inv, ['id'])}>
                {pick(inv, ['name', 'symbol'], 'Unnamed')}
              </option>
            ))}
          </select>
        </Field>
        <Field label="Type">
          <select className={inputClass} value={form.type} onChange={(e) => set('type', e.target.value)}>
            {TX_TYPES.map((t) => (
              <option key={t} value={t}>{t}</option>
            ))}
          </select>
        </Field>
        {needsQuantityPrice ? (
          <div className="grid grid-cols-2 gap-3">
            <Field label="Quantity">
              <input required type="number" step="any" className={inputClass} value={form.quantity} onChange={(e) => set('quantity', e.target.value)} />
            </Field>
            <Field label="Price / unit">
              <input required type="number" step="any" className={inputClass} value={form.price} onChange={(e) => set('price', e.target.value)} />
            </Field>
          </div>
        ) : (
          <Field label="Amount">
            <input required type="number" step="any" className={inputClass} value={form.amount} onChange={(e) => set('amount', e.target.value)} />
          </Field>
        )}
        <Field label="Date">
          <input required type="date" className={inputClass} value={form.date} onChange={(e) => set('date', e.target.value)} />
        </Field>
      </form>
    </Drawer>
  )
}
