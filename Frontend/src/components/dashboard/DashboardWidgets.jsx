import { PieChart, Pie, Cell, ResponsiveContainer, Tooltip } from 'recharts'
import { TrendingUp, TrendingDown, Flag, Clock, Trophy, Sparkles, ArrowDownLeft, ArrowUpRight, PiggyBank, Banknote } from 'lucide-react'
import { Card, SectionHeading, Skeleton, EmptyState } from '../ui.jsx'
import { pick, formatPercent, formatCurrency, formatDateTime, classForChange } from '../../utils/format.js'
import { DEFAULT_MILESTONE_IMAGE, getMilestoneImage } from '../../utils/milestones.js'

const ALLOC_COLORS = ['#E649A1', '#B48CFF', '#F39BC7', '#8E63F4', '#F06292']

const TYPE_META = {
  BUY: { icon: ArrowDownLeft, tone: 'text-jade' },
  SELL: { icon: ArrowUpRight, tone: 'text-brick' },
  DEPOSIT: { icon: PiggyBank, tone: 'text-violet' },
  WITHDRAW: { icon: ArrowUpRight, tone: 'text-brick' },
  INTEREST: { icon: Banknote, tone: 'text-rose-deep dark:text-rose' },
}

export function MoversCard({ investments = [], loading, currency }) {
  if (loading) return <Card><Skeleton className="h-40 w-full" /></Card>

  const withChange = investments
    .map((inv) => {
      // Pick name/symbol
      const name = pick(inv, ['name', 'investmentName', 'symbol', 'title'], 'Unnamed')
      
      // Check for daily change first; fallback to overall P/L % if daily change is 0 or undefined
      const change = Number(
        pick(inv, [
          'todayGainLossPercent',
          'todayChangePercentage', 
          'dayChangePercentage', 
          'unrealizedPlPercent', 
          'pnlPercentage', 
          'gainLossPercent', 
          'returnPercentage'
        ], 0)
      )

      return { name, change }
    })
    .filter((i) => !Number.isNaN(i.change) && i.change !== 0)

  // Sort gainers (highest change to lowest) and losers (lowest change to highest)
  const winners = [...withChange].sort((a, b) => b.change - a.change).filter((i) => i.change > 0).slice(0, 3)
  const losers = [...withChange].sort((a, b) => a.change - b.change).filter((i) => i.change < 0).slice(0, 3)

  return (
    <Card>
      <SectionHeading eyebrow="Performance" title="Winners & losers" />
      {winners.length === 0 && losers.length === 0 ? (
        <p className="py-6 text-center text-sm text-ink-faint">No price movement recorded yet.</p>
      ) : (
        <div className="grid grid-cols-2 gap-4">
          <MoverList title="Winners" items={winners} tone="jade" Icon={TrendingUp} />
          <MoverList title="Losers" items={losers} tone="brick" Icon={TrendingDown} />
        </div>
      )}
    </Card>
  )
}

function MoverList({ title, items, tone, Icon }) {
  const iconClass = tone === 'jade' ? 'text-jade' : 'text-brick'
  return (
    <div>
      <p className="mb-2 flex items-center gap-1.5 text-xs font-semibold uppercase tracking-wide text-ink-faint">
        <Icon className={`h-3.5 w-3.5 ${iconClass}`} /> {title}
      </p>
      <ul className="space-y-2">
        {items.length === 0 && <li className="text-xs text-ink-faint">None today</li>}
        {items.map((i) => (
          <li key={i.name} className="flex items-center justify-between text-sm">
            <span className="truncate pr-2 text-ink">{i.name}</span>
            <span className={`shrink-0 font-tabular font-semibold ${classForChange(i.change)}`}>
              {formatPercent(i.change)}
            </span>
          </li>
        ))}
      </ul>
    </div>
  )
}

// export function AllocationCard({ allocation = [], loading }) {
//   if (loading) return <Card><Skeleton className="h-56 w-full" /></Card>

//   const data = allocation.map((a) => ({
//     name: pick(a, ['type', 'assetType', 'category','label'], 'Other'),
//     value: Number(pick(a, ['percentage', 'allocationPercentage', 'percent',], 0)),
//     amount: pick(a, ['currentValue', 'amount', 'value'], 0),
//   }))

//   return (
//     <Card>
//       <SectionHeading eyebrow="Diversification" title="Asset allocation" />
//       {data.length === 0 ? (
//         <EmptyState title="No allocation yet" description="Add investments to see how your wealth is spread across asset types." />
//       ) : (
//         <div className="flex items-center gap-4">
//           <div className="h-40 w-40 shrink-0">
//             <ResponsiveContainer width="100%" height="100%">
//               <PieChart>
//                 <Pie data={data} dataKey="value" nameKey="name" innerRadius={48} outerRadius={72} paddingAngle={3}>
//                   {data.map((_, i) => (
//                     <Cell key={i} fill={ALLOC_COLORS[i % ALLOC_COLORS.length]} stroke="none" />
//                   ))}
//                 </Pie>
//                 <Tooltip
//                   formatter={(value, name) => [`${value}%`, name]}
//                   contentStyle={{ borderRadius: 12, border: '1px solid rgb(var(--line))', background: 'rgb(var(--paper-raised))' }}
//                 />
//               </PieChart>
//             </ResponsiveContainer>
//           </div>
//           <ul className="flex-1 space-y-2">
//             {data.map((d, i) => (
//               <li key={d.name} className="flex items-center justify-between text-sm">
//                 <span className="flex items-center gap-2 text-ink">
//                   <span className="h-2 w-2 rounded-full" style={{ background: ALLOC_COLORS[i % ALLOC_COLORS.length] }} />
//                   {d.name}
//                 </span>
//                 <span className="font-tabular text-ink-faint">{d.value}%</span>
//               </li>
//             ))}
//           </ul>
//         </div>
//       )}
//     </Card>
//   )
// }



export function NextMilestoneCard({
  milestone,
  loading,
  currency = "INR",
}) {
  if (loading) {
    return (
      <Card>
        <Skeleton className="h-40 w-full" />
      </Card>
    );
  }

  if (!milestone) {
    return (
      <Card>
        <EmptyState
          icon={Trophy}
          title="No milestone available"
          description="Your next portfolio milestone will appear here."
        />
      </Card>
    );
  }
  
  const {
    name,
    thresholdValueBase,
    comparisonLabel,
    progressPercent,
    achieved,
  } = milestone;
  const imageUrl = getMilestoneImage(name)

  return (
    <Card className="relative overflow-hidden">
      {/* Background Glow */}
      <div className="absolute -right-8 -top-8 h-28 w-28 rounded-full bg-rose/10 blur-3xl" />

      <div className="relative grid gap-5 lg:grid-cols-[minmax(0,1.3fr)_220px] lg:items-stretch">
        <div className="space-y-4">
          <div>
            <p className="mb-1 text-[11px] font-semibold uppercase tracking-[0.14em] text-ink-faint">
              Achievement
            </p>
            <h2 className="font-display text-lg text-ink">Next Milestone</h2>
          </div>

          <div className="flex items-start gap-3 rounded-2xl border border-line/70 bg-paper-sunken/70 p-4">
            <div className="rounded-xl bg-rose/10 p-3">
              <Sparkles className="h-5 w-5 text-rose" />
            </div>

            <div className="flex-1">
              <h3 className="text-lg font-semibold text-ink">
                {name}
              </h3>

              <p className="mt-1 text-sm text-ink-faint">
                {comparisonLabel}
              </p>
            </div>
          </div>
        </div>

        <div className="relative overflow-hidden rounded-2xl border border-line/70 bg-paper-sunken min-h-44">
          <img
            src={imageUrl}
            alt={name}
            className="h-full w-full object-cover"
            onError={(event) => {
              event.currentTarget.src = DEFAULT_MILESTONE_IMAGE
            }}
          />
          <div className="absolute inset-0 bg-gradient-to-t from-[#1b0a20]/55 via-transparent to-transparent" />
        </div>
      </div>

      <div className="mt-5 space-y-4">
        <div className="rounded-xl border border-line bg-paper-sunken p-4">
          <p className="text-xs uppercase tracking-wide text-ink-faint">
            Target Value
          </p>

          <p className="mt-1 text-2xl font-bold text-rose-deep">
            {formatCurrency(thresholdValueBase, currency, {
              decimals: 0,
            })}
          </p>
        </div>

        <div>
          <div className="mb-2 flex items-center justify-between text-sm">
            <span className="text-ink-faint">Progress</span>

            <span className="font-semibold text-rose-deep">
              {progressPercent.toFixed(1)}%
            </span>
          </div>

          <div className="h-3 overflow-hidden rounded-full bg-paper-sunken">
            <div
              className="h-full rounded-full bg-gradient-to-r from-rose-deep to-rose transition-all duration-700"
              style={{
                width: `${Math.min(progressPercent, 100)}%`,
              }}
            />
          </div>
        </div>

        {achieved && (
          <div className="rounded-xl border border-jade/30 bg-jade/10 p-3 text-sm font-medium text-jade">
            🎉 Congratulations! You've achieved this milestone.
          </div>
        )}
      </div>
    </Card>
  );
}

export function MilestoneSpotlight({ milestones = [], loading, currency }) {
  if (loading) return <Card><Skeleton className="h-32 w-full" /></Card>

  const next = [...milestones]
    .map((m) => ({
      name: pick(m, ['name', 'title', 'label'], 'Milestone'),
      progress: Number(pick(m, ['progressPercentage', 'progress'], 0)),
      remaining: pick(m, ['amountRemaining', 'remainingAmount'], null),
    }))
    .filter((m) => m.progress < 100)
    .sort((a, b) => b.progress - a.progress)[0]

  return (
    <Card>
      <SectionHeading eyebrow="Milestone" title="Next up" />
      {!next ? (
        <EmptyState icon={Flag} title="No milestones yet" description="Set an aspirational milestone to make progress feel real." />
      ) : (
       <div>
           <div className="flex items-center gap-2 text-sm font-medium text-ink">
             <Flag className="h-4 w-4 text-rose" />
             {next.name}
           </div>
           {next.remaining !== null && (
             <p className="mt-1 text-xs text-ink-faint">
               {formatCurrency(next.remaining, currency, { decimals: 0 })} to go
             </p>
           )}
           <div className="mt-3 h-2 w-full overflow-hidden rounded-full bg-paper-sunken">
             <div
               className="h-full rounded-full bg-gradient-to-r from-rose-deep to-rose"
               style={{ width: `${Math.min(100, Math.max(0, next.progress))}%` }}
             />
           </div>
           <p className="mt-1.5 text-right font-mono text-xs text-rose-deep dark:text-rose">
             {next.progress.toFixed(0)}%
           </p>
         </div>
      )}
    </Card>
  )
}

export function RecentActivityCard({ transactions = [], loading, currency }) {
  if (loading) return <Card><Skeleton className="h-48 w-full" /></Card>

  const items = transactions.slice(0, 6)
  // console.log('RecentActivityCard items:', items) // Debugging line

  return (
    <Card>
      <SectionHeading eyebrow="Ledger" title="Recent activity" />
      {items.length === 0 ? (
        <EmptyState icon={Clock} title="No activity yet" description="Buys, sells, deposits and interest will show up here." />
      ) : (
        <ul className="space-y-3">
          {items.map((t, idx) => {
            const type = pick(t, ['type', 'transactionType'], 'ACTIVITY')
            const meta = TYPE_META[type] || TYPE_META.BUY
            const Icon = meta.icon
            const name = pick(t, ['investmentSymbol', 'name', 'symbol'], 'Investment')
            const amount = pick(t, ['amount', 'value'], null)
            const date = pick(t, ['date', 'transactionDate', 'createdAt'])
            return (
              <li key={pick(t, ['id']) ?? idx} className="flex items-center justify-between text-sm">
                <div className="flex items-center gap-3">
                  <span className="flex h-8 w-8 items-center justify-center rounded-full bg-paper-sunken text-[10px] font-bold uppercase text-ink-faint">
                    <Icon className={`h-4 w-4 ${meta.tone}`} />
                  </span>
                  <div>
                    <p className="text-ink">
                      {name}
                      {" "}
                      <span className="capitalize text-ink-faint">({String(type).toLowerCase()})</span>
                    </p>
                    <p className="text-xs text-ink-faint">{formatDateTime(date)}</p>
                  </div>
                </div>
                {amount !== null && (
                  <span className="font-tabular text-ink">{formatCurrency(amount, currency, { decimals: 0 })}</span>
                )}
              </li>
            )
          })}
        </ul>
      )}
    </Card>
  )
}
