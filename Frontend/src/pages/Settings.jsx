import { useState, useEffect } from 'react'
import clsx from 'clsx'
import { Save } from 'lucide-react'
import { useAsync } from '../api/hooks.js'
import { listExchangeRates, setExchangeRate, setBaseCurrency } from '../api/client.js'
import { useApp } from '../context/AppContext.jsx'
import { Card, SectionHeading, Button, Field, inputClass, invalidInputClass, ErrorState, Skeleton, EmptyState } from '../components/ui.jsx'
import ThemeSwitcher from '../context/ThemeSwitcher.jsx'
import { pick } from '../utils/format.js'
import {
  VALIDATION_MESSAGES,
  getApiErrorMessage,
  isPositiveNumber,
  isValidSelection,
  mapApiFieldErrors,
  parseNumber,
} from '../utils/validation.js'

const CURRENCIES = ['INR', 'USD', 'GBP', 'EUR', 'CNY', 'JPY', 'AED']

export default function Settings() {
  const { baseCurrency, setBaseCurrency: setBaseCurrencyCtx, theme, setTheme, push } = useApp()
  const { data, loading, error, refetch } = useAsync(listExchangeRates, [])
  const rates = Array.isArray(data) ? data : pick(data, ['rates', 'exchangeRates'], []) || []
  
  const [draftBase, setDraftBase] = useState(baseCurrency)
  const [editingRates, setEditingRates] = useState({})
  const [baseError, setBaseError] = useState('')
  const [rateErrors, setRateErrors] = useState({})
  const [savingBase, setSavingBase] = useState(false)
  const [savingRateCode, setSavingRateCode] = useState('')

  // Keep draftBase in sync when baseCurrency loads asynchronously from AppContext
  useEffect(() => {
    setDraftBase(baseCurrency)
  }, [baseCurrency])

  async function saveBaseCurrency() {
    if (!isValidSelection(draftBase)) {
      setBaseError(VALIDATION_MESSAGES.selectOption)
      return
    }

    try {
      setSavingBase(true)
      setBaseError('')
      await setBaseCurrency({ baseCurrency: draftBase })
      setBaseCurrencyCtx(draftBase)
      push('Base currency updated.')
      refetch()
    } catch (e) {
      const fieldErrors = mapApiFieldErrors(e, { baseCurrency: 'baseCurrency' })
      setBaseError(fieldErrors.baseCurrency || getApiErrorMessage(e, 'Unable to update base currency.'))
      push(getApiErrorMessage(e, 'Unable to update base currency.'), 'error')
    } finally {
      setSavingBase(false)
    }
  }

  async function saveRate(code) {
    const value = editingRates[code]

    if (value === undefined || String(value).trim() === '') {
      setRateErrors((prev) => ({ ...prev, [code]: VALIDATION_MESSAGES.required }))
      return
    }

    if (!isPositiveNumber(value)) {
      setRateErrors((prev) => ({ ...prev, [code]: VALIDATION_MESSAGES.invalidAmount }))
      return
    }

    try {
      setSavingRateCode(code)
      setRateErrors((prev) => ({ ...prev, [code]: '' }))
      await setExchangeRate(code, { rateToBase: parseNumber(value) })
      push(`Exchange rate for ${code} updated.`)
      setEditingRates((s) => ({ ...s, [code]: '' }))
      refetch()
    } catch (e) {
      const fieldErrors = mapApiFieldErrors(e, { rateToBase: code })
      setRateErrors((prev) => ({
        ...prev,
        [code]: fieldErrors[code] || getApiErrorMessage(e, `Unable to update rate for ${code}.`),
      }))
      push(getApiErrorMessage(e, `Unable to update rate for ${code}.`), 'error')
    } finally {
      setSavingRateCode('')
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
          <Field label="Currency" className="mb-0" error={baseError}>
            <select
              className={clsx(inputClass, baseError && invalidInputClass)}
              value={draftBase}
              onChange={(e) => {
                setBaseError('')
                setDraftBase(e.target.value)
              }}
            >
              {CURRENCIES.map((c) => (
                <option key={c} value={c}>{c}</option>
              ))}
            </select>
          </Field>
          <Button
            className="!bg-none !bg-pink-500 !text-white shadow-glass-sm ring-1 ring-pink-400/40 hover:!bg-pink-600 dark:!bg-pink-500 dark:hover:!bg-pink-600 dark:ring-pink-400/35"
            onClick={saveBaseCurrency}
            disabled={savingBase}
          >
            <Save className="h-4 w-4" /> {savingBase ? 'Saving...' : 'Save'}
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
                      className={clsx(inputClass, 'w-32', rateErrors[code] && invalidInputClass)}
                      value={editingRates[code] ?? ''}
                      onChange={(e) => {
                        setRateErrors((prev) => ({ ...prev, [code]: '' }))
                        setEditingRates((s) => ({ ...s, [code]: e.target.value }))
                      }}
                    />
                    <Button variant="subtle" size="sm" onClick={() => saveRate(code)} disabled={savingRateCode === code}>
                      {savingRateCode === code ? 'Updating...' : 'Update'}
                    </Button>
                  </div>
                  {rateErrors[code] && <p className="w-full text-right text-xs text-brick">{rateErrors[code]}</p>}
                </li>
              )
            })}
          </ul>
        )}
      </Card>
    </div>
  )
}
