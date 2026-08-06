import { useState } from 'react'
import { RefreshCcw } from 'lucide-react'
import { useAsync } from '../api/hooks.js'
import { getDashboard, getPerformance, listAllTransactions, triggerSnapshot, listInvestments } from '../api/client.js'
import { useApp } from '../context/AppContext.jsx'
import { pick } from '../utils/format.js'
import NetWorthHero from '../components/dashboard/NetWorthHero.jsx'
import PerformanceChart from '../components/dashboard/PerformanceChart.jsx'
import { MoversCard, MilestoneSpotlight, RecentActivityCard, NextMilestoneCard } from '../components/dashboard/DashboardWidgets.jsx'
import { Button, ErrorState } from '../components/ui.jsx'

import AllocationCard from '../components/dashboard/AllocationCard.jsx'

const RANGE_DAYS = { '1M': 30, '3M': 90, '1Y': 365, ALL: undefined }

export default function Dashboard() {
  const { baseCurrency, push } = useApp()
  const [range, setRange] = useState('3M')

  const dashboard = useAsync(getDashboard, [])
  // console.log('dashboard.data', dashboard.data);
  const performance = useAsync(() => getPerformance(rangeParams(range)), [range])
  // console.log('performance.data',performance.data);
  const activity = useAsync(() => listAllTransactions({ limit: 6, sort: 'desc' }), [])
  // console.log('activity.data',activity.data);

  const investmentsAsync = useAsync(listInvestments, [])
  const data = dashboard.data || {}
  const dashboardMovers = pick(data, ['topMovers', 'investments'], []) || []
  // console.log('data',data.nextMilestone)

  const allInvestments = Array.isArray(investmentsAsync.data)
    ? investmentsAsync.data
    : pick(investmentsAsync.data, ['investments', 'content'], []) || []

  // const investments = dashboardMovers.length > 0 ? dashboardMovers : allInvestments

  const allocation = pick(data, ['allocation', 'assetAllocation', 'allocationByCountry', 'allocationByCurrency', 'allocationByType'], []) || []
  // console.log('allocation', allocation)
  const milestones = pick(data, ['milestones', 'upcomingMilestones'], []) || []

  const performancePoints = Array.isArray(performance.data)
    ? performance.data
    : pick(performance.data, ['points', 'history'], []) || []
  const transactions = Array.isArray(activity.data)
    ? activity.data
    : pick(activity.data, ['transactions', 'content'], []) || []
  // console.log(transactions)

  async function handleSnapshot() {
    try {
      await triggerSnapshot()
      push('Today\u2019s wealth snapshot has been recorded.')
      dashboard.refetch()
      investmentsAsync.refetch()
    } catch (e) {
      push(e.message, 'error')
    }
  }

  if (dashboard.error) {
    return <ErrorState message={dashboard.error.message} onRetry={dashboard.refetch} />
  }

  return (
    <div className="space-y-6">
      <div className="flex flex-wrap items-center justify-between gap-3">
        <div>
          <h1 className="font-display text-2xl text-ink">Good {timeOfDayGreeting()}</h1>
          <p className="text-sm text-ink-faint">Here's where your wealth stands right now.</p>
        </div>
        <Button variant="ghost" size="sm" onClick={handleSnapshot}>
          <RefreshCcw className="h-3.5 w-3.5" /> Snapshot today
        </Button>
      </div>

      <div className="grid grid-cols-1 gap-6 lg:grid-cols-2">
        <NetWorthHero data={data} loading={dashboard.loading} currency={baseCurrency} />
        <NextMilestoneCard
          milestone={data.nextMilestone}
          totalCurrentValue={data.totalCurrentValue}
          currency={data.baseCurrency}
          loading={dashboard.loading}
        />
      </div>

      <PerformanceChart
        points={performancePoints}
        loading={performance.loading}
        currency={baseCurrency}
        range={range}
        onRangeChange={setRange}
      />

      <div className="grid grid-cols-1 gap-6 lg:grid-cols-2">
        <AllocationCard allocations={{
          type: data.allocationByType,
          country: data.allocationByCountry,
          currency: data.allocationByCurrency
        }}
          baseCurrency={data.baseCurrency}
          loading={dashboard.loading} />
        <MoversCard investments={allInvestments} loading={dashboard.loading} currency={baseCurrency} />

      </div>

      <RecentActivityCard transactions={transactions} loading={activity.loading} currency={baseCurrency} />
    </div>
  )
}

function rangeParams(range) {
  const days = RANGE_DAYS[range]
  if (!days) return {}
  const to = new Date()
  const from = new Date()
  from.setDate(from.getDate() - days)
  return { from: from.toISOString().slice(0, 10), to: to.toISOString().slice(0, 10) }
}

function timeOfDayGreeting() {
  const h = new Date().getHours()
  if (h < 12) return 'morning'
  if (h < 17) return 'afternoon'
  return 'evening'
}
