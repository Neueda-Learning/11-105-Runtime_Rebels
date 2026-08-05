import { useState, useEffect } from 'react'
import { Save } from 'lucide-react'
import { useAsync } from '../api/hooks.js'
import { listExchangeRates, setExchangeRate, setBaseCurrency } from '../api/client.js'
import { useApp } from '../context/AppContext.jsx'
import { Card, SectionHeading, Button, Field, inputClass, ErrorState, Skeleton, EmptyState } from '../components/ui.jsx'
import ThemeSwitcher from '../components/theme/ThemeSwitcher.jsx'
import { pick } from '../utils/format.js'

const CURRENCIES = ['INR', 'USD', 'GBP', 'EUR', 'CNY', 'JPY', 'AED']

export default function Settings() {
  const { baseCurrency, setBaseCurrency: setBaseCurrencyCtx, theme, setTheme, push } = useApp()
  const { data, loading, error, refetch } = useAsync(listExchangeRates, [])
  const rates = Array.isArray(data) ? data : pick(data, ['rates', 'exchangeRates'], []) || []
  
  const [draftBase, setDraftBase] = useState(baseCurrency)
  const [editingRates, setEditingRates] = useState({})

  // Keep draftBase in sync when baseCurrency loads asynchronously from AppContext
  useEffect(() => {
    setDraftBase(baseCurrency)
  }, [baseCurrency])

  async function saveBaseCurrency() {
    try {
      // Fix: Send 'baseCurrency' matching the backend controller
      await setBaseCurrency({ baseCurrency: draftBase })
      setBaseCurrencyCtx(draftBase)
      push('Base currency updated.')
      // Refetch exchange rates since changing base currency recalculates all rates
      refetch()
    } catch (e) {
      push(e.message, 'error')
    }
  }

  async function saveRate(code) {
    const value = editingRates[code]
    if (value === undefined || value === '') return
    try {
      // Fix: Send 'rateToBase' matching ExchangeRateRequest DTO
      await setExchangeRate(code, { rateToBase: Number(value) })
      push(`Exchange rate for ${code} updated.`)
      setEditingRates((s) => ({ ...s, [code]: '' }))
      refetch()
    } catch (e) {
      push(e.message, 'error')
    }
  }

  return (
    <div className="space-y-6">
      <div>
        <h1 className="font-display text-2xl text-ink">Settings</h1>
        <p className="text-sm text-ink-faint">Base currency and conversion rates used across your dashboard.</p>
      </div>

      <Card>
        <SectionHeading eyebrow="Appearance" title="Theme" />
        <p className="mb-4 text-sm text-ink-faint">
          Choose your preferred look. System follows your device preference automatically.
        </p>
        <ThemeSwitcher theme={theme} setTheme={setTheme} />
      </Card>

      <Card>
        <SectionHeading eyebrow="Preference" title="Base currency" />
        <p className="mb-4 text-sm text-ink-faint">
          All totals, P/L and milestones are shown in this currency, converted from each investment's original currency.
        </p>
        <div className="flex flex-wrap items-center gap-3">
          <Field label="Currency" className="mb-0">
            <select className={inputClass} value={draftBase} onChange={(e) => setDraftBase(e.target.value)}>
              {CURRENCIES.map((c) => (
                <option key={c} value={c}>{c}</option>
              ))}
            </select>
          </Field>
          <Button
            className="!bg-none !bg-pink-500 !text-white shadow-glass-sm ring-1 ring-pink-400/40 hover:!bg-pink-600 dark:!bg-pink-500 dark:hover:!bg-pink-600 dark:ring-pink-400/35"
            onClick={saveBaseCurrency}
          >
            <Save className="h-4 w-4" /> Save
          </Button>
        </div>
      </Card>

      <Card>
        <SectionHeading eyebrow="Conversion" title="Exchange rates" />
        {error ? (
          <ErrorState message={error.message} onRetry={refetch} />
        ) : loading ? (
          <div className="space-y-2">
            {Array.from({ length: 4 }).map((_, i) => <Skeleton key={i} className="h-12 w-full" />)}
          </div>
        ) : rates.length === 0 ? (
          <EmptyState title="No exchange rates configured" description="Add a rate for each foreign currency you invest in, relative to your base currency." />
        ) : (
          <ul className="divide-y divide-line">
            {rates.map((r, idx) => {
              const code = pick(r, ['currencyCode', 'currency'], '—')
              // Fix: Read 'rateToBase' first
              const rate = pick(r, ['rateToBase', 'rate', 'exchangeRate'], '')
              return (
                <li key={code ?? idx} className="flex flex-wrap items-center justify-between gap-3 py-3">
                  <div>
                    <p className="text-sm font-medium text-ink">{code}</p>
                    <p className="text-xs text-ink-faint">1 {code} = ? {baseCurrency}</p>
                  </div>
                  <div className="flex items-center gap-2">
                    <input
                      type="number"
                      step="any"
                      placeholder={String(rate)}
                      className={`${inputClass} w-32`}
                      value={editingRates[code] ?? ''}
                      onChange={(e) => setEditingRates((s) => ({ ...s, [code]: e.target.value }))}
                    />
                    <Button variant="subtle" size="sm" onClick={() => saveRate(code)}>Update</Button>
                  </div>
                </li>
              )
            })}
          </ul>
        )}
      </Card>
    </div>
  )
}
