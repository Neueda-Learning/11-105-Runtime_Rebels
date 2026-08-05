import { useEffect, useMemo, useState } from 'react'
import { Drawer, Field, Button, inputClass } from '../ui.jsx'
import { formatCurrency, pick } from '../../utils/format.js'

const TX_TYPES = ['BUY', 'SELL', 'DEPOSIT', 'WITHDRAW', 'INTEREST']

const empty = {
  investmentId: '',
  type: 'BUY',
  quantity: '',
  price: '',
  amount: '',
  transactionDate: new Date().toISOString().slice(0, 10),
}

export default function TransactionDrawer({ open, onClose, onSubmit, investments = [] }) {
  const [form, setForm] = useState(empty)
  const [companyQuery, setCompanyQuery] = useState('')

  useEffect(() => {
    if (!open) return

    const first = investments[0]
    const firstId = first ? String(pick(first, ['id'])) : ''
    setForm({ ...empty, investmentId: firstId })
    setCompanyQuery(first ? getCompanyLabel(first) : '')
  }, [open]) // eslint-disable-line react-hooks/exhaustive-deps

  function set(key, value) {
    setForm((f) => ({ ...f, [key]: value }))
  }

  const needsQuantityPrice = form.type === 'BUY' || form.type === 'SELL'

  const selectedInvestment = useMemo(
    () => investments.find((inv) => String(pick(inv, ['id'])) === String(form.investmentId)),
    [investments, form.investmentId]
  )

  const suggestions = useMemo(() => {
    const query = normalize(companyQuery)
    const mapped = investments.map((inv) => {
      const name = pick(inv, ['name', 'companyName', 'symbol'], 'Unnamed')
      const symbol = pick(inv, ['symbol', 'ticker'], '')
      return {
        id: String(pick(inv, ['id'])),
        investment: inv,
        name,
        symbol,
        currentPrice: Number(pick(inv, ['currentPrice', 'price', 'marketPrice', 'latestPrice'], 0)),
        currency: pick(inv, ['currency'], 'INR'),
        searchName: normalize(name),
        searchSymbol: normalize(symbol),
      }
    })

    if (!query) return mapped.slice(0, 8)
    return mapped
      .filter((item) => item.searchName.startsWith(query) || item.searchSymbol.startsWith(query))
      .slice(0, 8)
  }, [investments, companyQuery])

  useEffect(() => {
    if (!selectedInvestment || !needsQuantityPrice) return
    const marketPrice = Number(
      pick(selectedInvestment, ['currentPrice', 'price', 'marketPrice', 'latestPrice'], 0)
    )
    if (marketPrice > 0) {
      setForm((f) => ({ ...f, price: String(marketPrice) }))
    }
  }, [selectedInvestment, needsQuantityPrice])

  const selectedCurrency = pick(selectedInvestment, ['currency'], 'INR')
  const quantityNum = Number(form.quantity || 0)
  const priceNum = Number(form.price || 0)
  const amountNum = Number(form.amount || 0)
  const computedAmount = needsQuantityPrice ? quantityNum * priceNum : amountNum
  const avgBuyPrice = Number(pick(selectedInvestment, ['avgBuyPrice', 'purchasePrice'], 0))
  const availableQty = Number(pick(selectedInvestment, ['quantity'], 0))
  const estimatedSellPl = form.type === 'SELL' ? (priceNum - avgBuyPrice) * quantityNum : 0

  function submit(e) {
    e.preventDefault()
    if (!form.investmentId) return

    onSubmit(form.investmentId, {
      type: form.type,
      quantity: needsQuantityPrice ? Number(form.quantity) : 0,
      price: needsQuantityPrice ? Number(form.price) : 0,
      amount: needsQuantityPrice ? computedAmount : amountNum,
      transactionDate: form.transactionDate,
    })
  }

  function chooseInvestment(item) {
    set('investmentId', item.id)
    setCompanyQuery(getCompanyLabel(item.investment))
    if (needsQuantityPrice && item.currentPrice > 0) {
      set('price', String(item.currentPrice))
    }
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
        <Field label="Company" hint="Type company name or symbol. Matching holdings are listed with current price.">
          <input
            className={inputClass}
            value={companyQuery}
            placeholder="Start typing, e.g. AAPL or Apple"
            onChange={(e) => setCompanyQuery(e.target.value)}
          />

          <div className="mt-2 max-h-44 overflow-y-auto rounded-lg border border-line bg-paper-raised">
            {suggestions.length === 0 ? (
              <p className="px-3 py-2 text-xs text-ink-faint">No matching companies for this prefix.</p>
            ) : (
              suggestions.map((item) => (
                <button
                  key={item.id}
                  type="button"
                  onClick={() => chooseInvestment(item)}
                  className={`flex w-full items-center justify-between gap-2 px-3 py-2 text-left text-sm hover:bg-paper-sunken ${
                    String(form.investmentId) === String(item.id) ? 'bg-paper-sunken' : ''
                  }`}
                >
                  <span className="min-w-0 truncate text-ink">
                    {item.name}
                    {item.symbol ? ` (${item.symbol})` : ''}
                  </span>
                  <span className="shrink-0 font-tabular text-xs text-ink-faint">
                    {formatCurrency(item.currentPrice, item.currency)}
                  </span>
                </button>
              ))
            )}
          </div>
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
            <Field label="Price / unit" hint="Auto-filled from current market price of selected company.">
              <input required type="number" step="any" className={inputClass} value={form.price} onChange={(e) => set('price', e.target.value)} />
            </Field>
          </div>
        ) : (
          <Field label="Amount">
            <input required type="number" step="any" className={inputClass} value={form.amount} onChange={(e) => set('amount', e.target.value)} />
          </Field>
        )}
        <Field label="Date">
          <input required type="date" className={inputClass} value={form.transactionDate} onChange={(e) => set('transactionDate', e.target.value)} />
        </Field>

        <div className="rounded-lg border border-line bg-paper-sunken/50 p-3 text-sm">
          <p className="mb-2 font-semibold text-ink">Calculation preview</p>

          {needsQuantityPrice ? (
            <>
              <p className="text-ink-faint">
                Amount = Quantity x Price = {quantityNum || 0} x {priceNum || 0}
              </p>
              <p className="mt-1 font-tabular text-ink">
                Transaction amount: {formatCurrency(computedAmount, selectedCurrency)}
              </p>

              {form.type === 'SELL' && (
                <>
                  <p className="mt-2 text-ink-faint">
                    Estimated realized P/L = (Sell Price - Avg Buy Price) x Quantity
                  </p>
                  <p className="font-tabular text-ink">
                    ({priceNum || 0} - {avgBuyPrice || 0}) x {quantityNum || 0} = {formatCurrency(estimatedSellPl, selectedCurrency)}
                  </p>
                  {availableQty > 0 && quantityNum > availableQty && (
                    <p className="mt-1 text-xs text-brick">Entered quantity exceeds available shares ({availableQty}).</p>
                  )}
                </>
              )}
            </>
          ) : (
            <>
              <p className="text-ink-faint">{form.type} amount</p>
              <p className="mt-1 font-tabular text-ink">{formatCurrency(amountNum, selectedCurrency)}</p>
            </>
          )}
        </div>
      </form>
    </Drawer>
  )
}

function normalize(value) {
  return String(value || '').trim().toLowerCase()
}

function getCompanyLabel(inv) {
  const name = pick(inv, ['name', 'companyName', 'symbol'], 'Unnamed')
  const symbol = pick(inv, ['symbol', 'ticker'], '')
  return symbol && symbol !== name ? `${name} (${symbol})` : String(name)
}
