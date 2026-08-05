import { AreaChart, Area, XAxis, YAxis, Tooltip, ResponsiveContainer, CartesianGrid } from 'recharts'
import { Card, SectionHeading, Skeleton, EmptyState } from '../ui.jsx'
import { pick, formatCompact, formatDate } from '../../utils/format.js'
import { TrendingUp } from 'lucide-react'

export default function PerformanceChart({ points = [], loading, currency, range, onRangeChange }) {
  if (loading) return <Card><Skeleton className="h-64 w-full" /></Card>

  const data = points.map((p) => ({
    date: pick(p, ['date', 'snapshotDate']),
    value: Number(pick(p, ['portfolioValue', 'value', 'currentValue','overallPlBase','totalInvestedBase','totalValueBase'], 0)),
  }))

  return (
    <Card>
      <SectionHeading
        eyebrow="Growth"
        title="Portfolio performance"
        action={
          <div className="flex gap-1 rounded-full bg-paper-sunken p-1">
            {['1M', '3M', '1Y', 'ALL'].map((r) => (
              <button
                key={r}
                onClick={() => onRangeChange?.(r)}
                className={`rounded-full px-3 py-1 text-xs font-semibold transition ${
                  range === r ? 'bg-paper-raised text-ink shadow-glass-sm' : 'text-ink-faint'
                }`}
              >
                {r}
              </button>
            ))}
          </div>
        }
      />
      {data.length === 0 ? (
        <EmptyState icon={TrendingUp} title="Not enough history yet" description="Once a few daily snapshots are recorded, your growth curve will appear here." />
      ) : (
        <div className="h-64 w-full">
          <ResponsiveContainer width="100%" height="100%">
            <AreaChart data={data} margin={{ left: -20, right: 8, top: 8 }}>
               <defs>
                 <linearGradient id="perfFill" x1="0" y1="0" x2="0" y2="1">
                    <stop offset="0%" stopColor="#E649A1" stopOpacity={0.42} />
                    <stop offset="55%" stopColor="#B48CFF" stopOpacity={0.2} />
                    <stop offset="100%" stopColor="#B48CFF" stopOpacity={0} />
                 </linearGradient>
               </defs>
              <CartesianGrid strokeDasharray="3 3" stroke="rgb(var(--line))" vertical={false} />
              <XAxis
                dataKey="date"
                tickFormatter={(d) => formatDate(d)}
                tick={{ fontSize: 11, fill: 'rgb(var(--ink-faint))' }}
                axisLine={false}
                tickLine={false}
                minTickGap={40}
              />
              <YAxis
                tickFormatter={(v) => formatCompact(v, currency)}
                tick={{ fontSize: 11, fill: 'rgb(var(--ink-faint))' }}
                axisLine={false}
                tickLine={false}
                width={64}
              />
              <Tooltip
                labelFormatter={(d) => formatDate(d)}
                formatter={(v) => [formatCompact(v, currency), 'Value']}
                contentStyle={{
                  borderRadius: 12,
                  border: '1px solid rgb(var(--line))',
                  background: 'rgb(var(--paper-raised))',
                  fontSize: 12,
                }}
              />
                <Area type="monotone" dataKey="value" stroke="#C92E86" strokeWidth={2.3} fill="url(#perfFill)" />
            </AreaChart>
          </ResponsiveContainer>
        </div>
      )}
    </Card>
  )
}
