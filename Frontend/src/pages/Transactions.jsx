import { useState } from 'react'
import { Plus, ArrowDownLeft, ArrowUpRight, Banknote, PiggyBank } from 'lucide-react'
import { useAsync } from '../api/hooks.js'
import { listAllTransactions, listInvestments, createTransaction } from '../api/client.js'
import { useApp } from '../context/AppContext.jsx'
import { Button, Card, ErrorState, EmptyState, Skeleton } from '../components/ui.jsx'
import TransactionDrawer from '../components/transactions/TransactionDrawer.jsx'
import { pick, formatCurrency, formatDate } from '../utils/format.js'

const TYPE_META = {
  BUY: { icon: ArrowDownLeft, tone: 'text-jade' },
  SELL: { icon: ArrowUpRight, tone: 'text-brick' },
  DEPOSIT: { icon: PiggyBank, tone: 'text-violet' },
  WITHDRAW: { icon: ArrowUpRight, tone: 'text-brick' },
  INTEREST: { icon: Banknote, tone: 'text-rose-deep dark:text-rose-soft' },
}

export default function Transactions() {
  const { baseCurrency, push } = useApp()
  const [open, setOpen] = useState(false)

  const tx = useAsync(() => listAllTransactions({ sort: 'desc' }), [])
  const inv = useAsync(() => listInvestments(), [])

  const transactions = Array.isArray(tx.data) ? tx.data : pick(tx.data, ['transactions', 'content'], []) || []
  const investments = Array.isArray(inv.data) ? inv.data : pick(inv.data, ['investments', 'content'], []) || []

  async function handleSubmit(investmentId, payload) {
    try {
      console.log('Submitting transaction:', investmentId, payload)
      await createTransaction(investmentId, payload)
      push('Transaction recorded.')
      setOpen(false)
      tx.refetch()
    } catch (e) {
      push(e.message, 'error')
    }
  }

  if (tx.error) return <ErrorState message={tx.error.message} onRetry={tx.refetch} />

  return (
    <div className="space-y-5">
      <div className="flex flex-wrap items-center justify-between gap-3">
        <div>
          <h1 className="font-display text-2xl text-ink">Transactions</h1>
          <p className="text-sm text-ink-faint">Buys, sells, deposits, withdrawals and interest — your full ledger.</p>
        </div>
        <Button
          className="!bg-none !bg-pink-500 !text-white shadow-glass-sm ring-1 ring-pink-400/40 hover:!bg-pink-600 dark:!bg-pink-500 dark:hover:!bg-pink-600 dark:ring-pink-400/35"
          onClick={() => setOpen(true)}
          disabled={investments.length === 0}
        >
          <Plus className="h-4 w-4" /> Record transaction
        </Button>
      </div>

      <Card className="p-0">
        {tx.loading ? (
          <div className="space-y-2 p-5">
            {Array.from({ length: 6 }).map((_, i) => <Skeleton key={i} className="h-12 w-full" />)}
          </div>
        ) : transactions.length === 0 ? (
          <div className="p-5">
            <EmptyState title="No transactions yet" description="Record a buy, sell, deposit or interest entry to start building your ledger." />
          </div>
        ) : (
          <ul className="divide-y divide-line">
            {transactions.map((t, idx) => {
              const type = pick(t, ['type', 'transactionType'], 'BUY')
              const meta = TYPE_META[type] || TYPE_META.BUY
              const Icon = meta.icon
              const name = pick(t, ['investmentName', 'name'], 'Investment')
              const amount = pick(t, ['amount', 'totalAmount'], null)
              const quantity = pick(t, ['quantity'], null)
              const price = pick(t, ['price'], null)
              const date = pick(t, ['date', 'transactionDate'])
              const currency = pick(t, ['currency'], baseCurrency)

              return (
                <li key={pick(t, ['id']) ?? idx} className="flex items-center justify-between gap-3 px-5 py-3.5">
                  <div className="flex items-center gap-3">
                    <span className="flex h-9 w-9 items-center justify-center rounded-full bg-paper-sunken">
                      <Icon className={`h-4 w-4 ${meta.tone}`} />
                    </span>
                    <div>
                      <p className="text-sm font-medium text-ink">
                        {type} · {name}
                      </p>
                      <p className="text-xs text-ink-faint">
                        {formatDate(date)}
                        {quantity !== null && price !== null && ` · ${quantity} @ ${formatCurrency(price, currency)}`}
                      </p>
                    </div>
                  </div>
                  {amount !== null && (
                    <span className="font-tabular text-sm text-ink">{formatCurrency(amount, currency, { decimals: 0 })}</span>
                  )}
                </li>
              )
            })}
          </ul>
        )}
      </Card>

      <TransactionDrawer open={open} onClose={() => setOpen(false)} onSubmit={handleSubmit} investments={investments} />
    </div>
  )
}
