import CountUp from 'react-countup'
import { motion } from 'framer-motion'
import { pick, formatPercent, formatCurrency } from '../../utils/format.js'
import { Skeleton } from '../ui.jsx'

export default function NetWorthHero({ data, loading, currency }) {
  if (loading) {
    return (
      <div className="rounded-xl2 border border-line bg-aurora p-8 shadow-glass">
        <Skeleton className="h-3 w-32 bg-white/10" />
        <Skeleton className="mt-4 h-12 w-64 bg-white/10" />
        <Skeleton className="mt-4 h-4 w-40 bg-white/10" />
      </div>
    )
  }

  const netWorth = pick(data, ['currentValue', 'totalCurrentValue', 'portfolioValue', 'netWorth', 'totalValue'], 0)
  const invested = pick(data, ['totalInvested', 'investedAmount', 'totalInvestedAmount'], 0)
  const todayChange = pick(data, ['todayGainLoss', 'todaysGainLoss', 'dayChange'], 0)
  const todayChangePct = pick(data, ['todayGainLossPercentage', 'todaysGainLossPercentage', 'dayChangePercentage'], 0)
  const overallPct = pick(data, ['overallReturnPercentage', 'totalReturnPercentage', 'returnPercentage'], 0)
  const asOf = pick(data, ['asOf', 'lastUpdated', 'updatedAt'])

  const isUp = Number(todayChange) >= 0

  return (
    <motion.div
      initial={{ opacity: 0, y: 10 }}
      animate={{ opacity: 1, y: 0 }}
      transition={{ duration: 0.5, ease: 'easeOut' }}
      className="
relative
overflow-hidden
rounded-3xl
border
border-rose-200/60
bg-gradient-to-br
from-[#5b3848]
via-[#39212E]
to-[#24141D]
p-6
shadow-[0_15px_40px_rgba(215,120,165,0.18)]
sm:p-8
"
    >
      <div className="pointer-events-none absolute inset-0 opacity-[0.06] [background-image:radial-gradient(circle_at_1px_1px,white_1px,transparent_0)] [background-size:18px_18px]" />

      <div className="relative">
        <div className="flex items-center justify-between">
          <p className="text-[11px] font-semibold uppercase tracking-[0.16em] text-white/50">
            Net Worth · {currency}
          </p>
          {asOf && (
            <p className="font-mono text-[11px] text-white/40">
              as of {new Date(asOf).toLocaleTimeString('en-IN', { hour: '2-digit', minute: '2-digit' })}
            </p>
          )}
        </div>

        <div className="mt-3 flex items-baseline gap-1 font-display text-4xl font-medium text-white sm:text-5xl">
          <CountUp
            end={Number(netWorth) || 0}
            duration={1.4}
            separator=","
            prefix={currencySymbol(currency)}
            className="font-tabular"
          />
        </div>

        <div className="mt-1 h-px w-24 bg-gradient-to-r from-gold via-gold/40 to-transparent" />

        <div className="mt-4 flex flex-wrap items-center gap-x-5 gap-y-2 text-sm">
          <span className={isUp ? 'text-jade-soft' : 'text-brick-soft'}>
            {isUp ? '↑' : '↓'} {formatCurrency(Math.abs(todayChange), currency)} today
            {todayChangePct !== undefined && ` (${formatPercent(todayChangePct)})`}
          </span>
          <span className="text-white/50">
            Invested {formatCurrency(invested, currency, { decimals: 0 })}
          </span>
          <span className="text-gold-soft">{formatPercent(overallPct)} overall</span>
        </div>
      </div>
    </motion.div>
  )
}

function currencySymbol(code) {
  const symbols = { INR: '₹', USD: '$', GBP: '£', EUR: '€', JPY: '¥', CNY: '¥', AED: 'AED ' }
  return symbols[code] || ''
}
