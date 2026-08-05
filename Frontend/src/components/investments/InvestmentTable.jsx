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
    <>
      <div className="space-y-3 md:hidden">
        {investments.map((inv, idx) => {
          const item = mapInvestment(inv)
          return (
            <div
              key={item.id ?? idx}
              className="rounded-xl2 border border-line bg-paper-raised/90 p-4 shadow-glass-sm"
            >
              <div className="flex items-start justify-between gap-3">
                <div className="min-w-0">
                  <p className="truncate font-medium text-ink">{item.name}</p>
                  <p className="mt-1 text-xs text-ink-faint">{item.country} · {item.currency}</p>
                </div>
                <Badge tone="violet">{item.type}</Badge>
              </div>

              <div className="mt-4 grid grid-cols-2 gap-3 text-sm">
                <Metric label="Invested" value={formatCurrency(item.invested, item.currency, { decimals: 0 })} />
                <Metric label="Current" value={formatCurrency(item.current, item.currency, { decimals: 0 })} emphasize />
                <Metric
                  label="P/L"
                  value={formatPercent(item.plPct)}
                  valueClassName={`font-tabular font-semibold ${classForChange(item.plPct)}`}
                />
                <Metric label="Market" value={item.country} />
              </div>

              <div className="mt-4 flex items-center justify-end gap-1 border-t border-line pt-3">
                <IconBtn title="Refresh price" onClick={() => onRefreshPrice(inv)}>
                  <RefreshCw className="h-4 w-4" />
                </IconBtn>
                <IconBtn title="Edit" onClick={() => onEdit(inv)}>
                  <Pencil className="h-4 w-4" />
                </IconBtn>
                <IconBtn title="Delete" tone="brick" onClick={() => onDelete(inv)}>
                  <Trash2 className="h-4 w-4" />
                </IconBtn>
              </div>
            </div>
          )
        })}
      </div>

      <div className="hidden overflow-x-auto rounded-xl2 border border-line md:block">
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
              const item = mapInvestment(inv)

              return (
                <tr key={item.id ?? idx} className="border-b border-line last:border-0 hover:bg-paper-sunken/40">
                  <td className="px-4 py-3 font-medium text-ink">{item.name}</td>
                  <td className="px-4 py-3">
                    <Badge tone="violet">{item.type}</Badge>
                  </td>
                  <td className="px-4 py-3 text-ink-faint">{item.country}</td>
                  <td className="px-4 py-3 text-right font-tabular text-ink-faint">
                    {formatCurrency(item.invested, item.currency, { decimals: 0 })}
                  </td>
                  <td className="px-4 py-3 text-right font-tabular text-ink">
                    {formatCurrency(item.current, item.currency, { decimals: 0 })}
                  </td>
                  <td className={`px-4 py-3 text-right font-tabular font-semibold ${classForChange(item.plPct)}`}>
                    {formatPercent(item.plPct)}
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
    </>
  )
}

function mapInvestment(inv) {
  return {
    id: pick(inv, ['id']),
    name: pick(inv, ['name', 'symbol'], 'Unnamed'),
    type: pick(inv, ['type', 'investmentType'], '—'),
    country: pick(inv, ['country', 'market'], '—'),
    currency: pick(inv, ['currency'], 'INR'),
    invested: pick(inv, ['investedAmount', 'totalInvested'], 0),
    current: pick(inv, ['currentValue', 'currentAmount'], 0),
    plPct: pick(inv, ['gainLossPercentage', 'returnPercentage'], 0),
  }
}

function Metric({ label, value, valueClassName = 'font-tabular text-ink-faint', emphasize = false }) {
  return (
    <div className="rounded-xl bg-paper-sunken/55 px-3 py-2">
      <p className="text-[11px] font-semibold uppercase tracking-wide text-ink-faint">{label}</p>
      <p className={`${emphasize ? 'mt-1 font-semibold text-ink' : 'mt-1'} ${valueClassName}`}>{value}</p>
    </div>
  )
}

function IconBtn({ children, onClick, title, tone }) {
  const toneClass = tone === 'brick' ? 'hover:text-brick' : 'hover:text-ink'
  return (
    <button
      title={title}
      onClick={onClick}
      className={`flex h-9 w-9 items-center justify-center rounded-lg text-ink-faint hover:bg-paper-sunken ${toneClass}`}
    >
      {children}
    </button>
  )
}
