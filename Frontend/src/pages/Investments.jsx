import { useState } from 'react'
import { Plus } from 'lucide-react'
import { useAsync } from '../api/hooks.js'
import {
  listInvestments,
  createInvestment,
  updateInvestment,
  deleteInvestment,
  refreshInvestmentPrice,
} from '../api/client.js'
import { useApp } from '../context/AppContext.jsx'
import { Button, ConfirmDialog, ErrorState } from '../components/ui.jsx'
import InvestmentTable from '../components/investments/InvestmentTable.jsx'
import InvestmentDrawer from '../components/investments/InvestmentDrawer.jsx'
import PriceUpdateDrawer from '../components/investments/PriceUpdateDrawer.jsx'
import { pick } from '../utils/format.js'
import { getApiErrorMessage } from '../utils/validation.js'

const TYPE_FILTERS = ['ALL', 'STOCK', 'ETF', 'FD', 'CASH']

export default function Investments() {
  const { push } = useApp()
  const [typeFilter, setTypeFilter] = useState('ALL')
  const [drawer, setDrawer] = useState({ open: false, initial: null })
  const [confirmDelete, setConfirmDelete] = useState(null)
  const [priceDrawer, setPriceDrawer] = useState({ open: false, investment: null })

  const params = typeFilter === 'ALL' ? {} : { type: typeFilter }
  const { data, loading, error, refetch } = useAsync(() => listInvestments(params), [typeFilter])
  const investments = Array.isArray(data) ? data : pick(data, ['investments', 'content'], []) || []

  async function handleSubmit(payload) {
    try {
      if (drawer.initial) {
        await updateInvestment(pick(drawer.initial, ['id']), payload)
        push('Investment updated.')
      } else {
        await createInvestment(payload)
        push('Investment added to your portfolio.')
      }
      setDrawer({ open: false, initial: null })
      refetch()
    } catch (e) {
      push(getApiErrorMessage(e, 'Unable to save investment.'), 'error')
      throw e
    }
  }

  async function handleDelete() {
    try {
      await deleteInvestment(pick(confirmDelete, ['id']))
      push('Investment removed.')
      setConfirmDelete(null)
      refetch()
    } catch (e) {
      push(getApiErrorMessage(e, 'Unable to remove investment.'), 'error')
    }
  }

  async function handleRefreshPrice(payload) {
    const id = pick(priceDrawer.investment, ['id'])
    if (!id) {
      const error = new Error('Please select a valid investment.')
      push(error.message, 'error')
      throw error
    }
    try {
      await refreshInvestmentPrice(id, payload)
      push('Price updated.')
      setPriceDrawer({ open: false, investment: null })
      refetch()
    } catch (e) {
      push(getApiErrorMessage(e, 'Unable to update market price.'), 'error')
      throw e
    }
  }

  if (error) return <ErrorState message={error.message} onRetry={refetch} />

  return (
    <div className="space-y-5">
      <div className="flex flex-wrap items-center justify-between gap-3">
        <div>
          <h1 className="font-display text-2xl text-ink">Investments</h1>
          <p className="text-sm text-ink-faint">Every stock, ETF, fixed deposit and cash holding, in one place.</p>
        </div>
        <Button
          className="!bg-none !bg-pink-500 !text-white shadow-glass-sm ring-1 ring-pink-400/40 hover:!bg-pink-600 dark:!bg-pink-500 dark:hover:!bg-pink-600 dark:ring-pink-400/35"
          onClick={() => setDrawer({ open: true, initial: null })}
        >
          <Plus className="h-4 w-4" /> Add investment
        </Button>
      </div>

      <div className="flex gap-1 overflow-x-auto rounded-full bg-paper-sunken p-1 w-fit">
        {TYPE_FILTERS.map((t) => (
          <button
            key={t}
            onClick={() => setTypeFilter(t)}
            className={`whitespace-nowrap rounded-full px-3.5 py-1.5 text-xs font-semibold transition ${
              typeFilter === t ? 'bg-paper-raised text-ink shadow-glass-sm' : 'text-ink-faint'
            }`}
          >
            {t === 'ALL' ? 'All types' : t}
          </button>
        ))}
      </div>

      <InvestmentTable
        investments={investments}
        loading={loading}
        onEdit={(inv) => setDrawer({ open: true, initial: inv })}
        onDelete={(inv) => setConfirmDelete(inv)}
        onRefreshPrice={(inv) => setPriceDrawer({ open: true, investment: inv })}
      />

      <InvestmentDrawer
        open={drawer.open}
        initial={drawer.initial}
        onClose={() => setDrawer({ open: false, initial: null })}
        onSubmit={handleSubmit}
      />

      <PriceUpdateDrawer
        open={priceDrawer.open}
        investment={priceDrawer.investment}
        onClose={() => setPriceDrawer({ open: false, investment: null })}
        onSubmit={handleRefreshPrice}
      />

      <ConfirmDialog
        open={Boolean(confirmDelete)}
        title="Remove this investment?"
        description="This can't be undone. Historical transactions tied to it will remain in your ledger."
        onConfirm={handleDelete}
        onCancel={() => setConfirmDelete(null)}
      />
    </div>
  )
}
