import { Pencil, Trash2, RefreshCw } from 'lucide-react'
import { pick, formatCurrency, formatPercent, classForChange } from '../../utils/format.js'
import { Badge, EmptyState, Skeleton } from '../ui.jsx'
import { Wallet } from 'lucide-react'

export default function InvestmentTable({ investments, loading, onEdit, onDelete, onRefreshPrice }) {
  if (loading) {
    return (
      <div className="space-y-2">
        {Array.from({ length: 5 }).map((_, i) => (
          <Skeleton key={i} className="h-14 w-full" />
        ))}
      </div>
    )
  }

  if (investments.length === 0) {
    return (
      <EmptyState
        icon={Wallet}
        title="No investments yet"
        description="Add your first stock, ETF, fixed deposit or cash holding to start tracking your portfolio."
      />
    )
  }

  return (
    <div className="overflow-x-auto rounded-xl2 border border-line">
      <table className="w-full min-w-[720px] text-sm">
        <thead>
          <tr className="border-b border-line bg-paper-sunken/60 text-left text-xs font-semibold uppercase tracking-wide text-ink-faint">
            <th className="px-4 py-3">Name</th>
            <th className="px-4 py-3">Type</th>
            <th className="px-4 py-3">Market</th>
            <th className="px-4 py-3 text-right">Invested</th>
            <th className="px-4 py-3 text-right">Current value</th>
            <th className="px-4 py-3 text-right">P/L</th>
            <th className="px-4 py-3" />
          </tr>
        </thead>
        <tbody>
          {investments.map((inv, idx) => {
            const id = pick(inv, ['id'])
            const name = pick(inv, ['name', 'symbol'], 'Unnamed')
            const type = pick(inv, ['type', 'investmentType'], '—')
            const country = pick(inv, ['country', 'market'], '—')
            const currency = pick(inv, ['currency'], 'INR')
            const invested = pick(inv, ['investedAmount', 'totalInvested'], 0)
            const current = pick(inv, ['currentValue', 'currentAmount'], 0)
            const plPct = pick(inv, ['gainLossPercentage', 'returnPercentage'], 0)

            return (
              <tr key={id ?? idx} className="border-b border-line last:border-0 hover:bg-paper-sunken/40">
                <td className="px-4 py-3 font-medium text-ink">{name}</td>
                <td className="px-4 py-3">
                  <Badge tone="violet">{type}</Badge>
                </td>
                <td className="px-4 py-3 text-ink-faint">{country}</td>
                <td className="px-4 py-3 text-right font-tabular text-ink-faint">
                  {formatCurrency(invested, currency, { decimals: 0 })}
                </td>
                <td className="px-4 py-3 text-right font-tabular text-ink">
                  {formatCurrency(current, currency, { decimals: 0 })}
                </td>
                <td className={`px-4 py-3 text-right font-tabular font-semibold ${classForChange(plPct)}`}>
                  {formatPercent(plPct)}
                </td>
                <td className="px-4 py-3">
                  <div className="flex items-center justify-end gap-1">
                    <IconBtn title="Refresh price" onClick={() => onRefreshPrice(inv)}>
                      <RefreshCw className="h-3.5 w-3.5" />
                    </IconBtn>
                    <IconBtn title="Edit" onClick={() => onEdit(inv)}>
                      <Pencil className="h-3.5 w-3.5" />
                    </IconBtn>
                    <IconBtn title="Delete" tone="brick" onClick={() => onDelete(inv)}>
                      <Trash2 className="h-3.5 w-3.5" />
                    </IconBtn>
                  </div>
                </td>
              </tr>
            )
          })}
        </tbody>
      </table>
    </div>
  )
}

function IconBtn({ children, onClick, title, tone }) {
  const toneClass = tone === 'brick' ? 'hover:text-brick' : 'hover:text-ink'
  return (
    <button
      title={title}
      onClick={onClick}
      className={`flex h-8 w-8 items-center justify-center rounded-lg text-ink-faint hover:bg-paper-sunken ${toneClass}`}
    >
      {children}
    </button>
  )
}
