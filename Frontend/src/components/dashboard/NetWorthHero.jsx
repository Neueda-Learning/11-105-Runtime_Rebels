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
border-rose/45
bg-pink-400
dark:from-[#3a1a45]
dark:via-[#2b1238]
dark:to-[#1f0d2c]
p-6
shadow-[0_20px_45px_rgba(176,74,227,0.35)]
dark:shadow-[0_22px_55px_rgba(64,18,90,0.5)]
sm:p-8
"
    >
      <div className="pointer-events-none absolute -left-12 -top-14 h-40 w-40 rounded-full bg-violet-soft/25 dark:bg-violet-soft/20 blur-3xl" />
      <div className="pointer-events-none absolute -bottom-16 right-0 h-48 w-48 rounded-full bg-rose-soft/25 dark:bg-rose-soft/20 blur-3xl" />
      <div className="pointer-events-none absolute inset-0 opacity-[0.06] [background-image:radial-gradient(circle_at_1px_1px,white_1px,transparent_0)] [background-size:18px_18px]" />

      <div className="relative">
        <div className="flex items-center justify-between">
          <p className="text-[11px] font-semibold uppercase tracking-[0.16em] text-white/50 dark:text-white/60">
            Net Worth · {currency}
          </p>
          {asOf && (
            <p className="font-mono text-[11px] text-white/40 dark:text-white/45">
              as of {new Date(asOf).toLocaleTimeString('en-IN', { hour: '2-digit', minute: '2-digit' })}
            </p>
          )}
        </div>

        <div className="mt-3 flex items-baseline gap-1 font-display text-4xl font-medium text-white dark:text-white sm:text-5xl">
          <CountUp
            end={Number(netWorth) || 0}
            duration={1.4}
            separator=","
            prefix={currencySymbol(currency)}
            className="font-tabular"
          />
        </div>

        <div className="mt-1 h-px w-24 bg-gradient-to-r from-rose via-rose/40 to-transparent dark:hidden" />

        <div className="mt-5 grid gap-2 text-sm sm:grid-cols-3">
          <div className="rounded-xl border border-white/20 bg-white/10 px-3 py-2 backdrop-blur-sm dark:border-white/15 dark:bg-white/10">
            <p className="text-[10px] uppercase tracking-[0.14em] text-white/55 dark:text-white/55">Today</p>
            <p className="mt-1 font-semibold text-[#14532d] dark:text-jade-soft">
              <span className="text-white dark:text-current">{isUp ? '↑' : '↓'}</span>{' '}
              {formatCurrency(Math.abs(todayChange), currency)}
              {todayChangePct !== undefined && ` (${formatPercent(todayChangePct)})`}
            </p>
          </div>
          <div className="rounded-xl border border-white/20 bg-white/10 px-3 py-2 backdrop-blur-sm dark:border-white/15 dark:bg-white/10">
            <p className="text-[10px] uppercase tracking-[0.14em] text-white/55 dark:text-white/55">Invested</p>
            <p className="mt-1 font-semibold text-white/90 dark:text-white/90">
              {formatCurrency(invested, currency, { decimals: 0 })}
            </p>
          </div>
          <div className="rounded-xl border border-white/20 bg-white/10 px-3 py-2 backdrop-blur-sm dark:border-white/15 dark:bg-white/10">
            <p className="text-[10px] uppercase tracking-[0.14em] text-white/55 dark:text-white/55">Overall</p>
            <p className="mt-1 font-semibold text-white/90 dark:text-white/90">{formatPercent(overallPct)}</p>
          </div>
        </div>
      </div>
    </motion.div>
  )
}

function currencySymbol(code) {
  const symbols = { INR: '₹', USD: '$', GBP: '£', EUR: '€', JPY: '¥', CNY: '¥', AED: 'AED ' }
  return symbols[code] || ''
}
