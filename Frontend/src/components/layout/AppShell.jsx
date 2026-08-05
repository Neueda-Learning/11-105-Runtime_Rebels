import { NavLink, Outlet } from 'react-router-dom'
import { LayoutDashboard, Wallet, Flag, ArrowLeftRight, Settings } from 'lucide-react'
import clsx from 'clsx'
import { useApp } from '../../context/AppContext.jsx'
import ThemeSwitcher from '../../context/ThemeSwitcher.jsx'

const NAV = [
  { to: '/', label: 'Dashboard', icon: LayoutDashboard, end: true },
  { to: '/investments', label: 'Investments', icon: Wallet },
  { to: '/transactions', label: 'Transactions', icon: ArrowLeftRight },
  { to: '/milestones', label: 'Milestones', icon: Flag },
  { to: '/settings', label: 'Settings', icon: Settings },
]

export default function AppShell() {
  return (
    <div className="min-h-screen bg-paper text-ink">
      <div className="mx-auto flex max-w-8xl">
        <DesktopSidebar />
        <div className="min-h-screen flex-1 pb-24 md:pb-0">
          <Topbar />
          <main className="px-4 py-6 sm:px-6 lg:px-8">
            <Outlet />
          </main>
        </div>
      </div>
      <MobileTabBar />
    </div>
  )
}

function DesktopSidebar() {
  return (
    <aside className="sticky top-0 hidden h-screen w-60 shrink-0 flex-col border-r border-line bg-gradient-to-b from-paper-raised/70 via-paper/55 to-paper/35 px-4 py-6 md:flex">
      <div className="mb-8 flex items-center gap-2 px-2">
        <div className="flex h-8 w-8 items-center justify-center rounded-lg bg-gradient-to-br from-rose-soft to-violet-soft font-display text-sm font-semibold text-[#2b1132]">
          W
        </div>
        <span className="font-display text-base tracking-tight">Wealth Ledger</span>
      </div>
      <nav className="flex flex-1 flex-col gap-1">
        {NAV.map((item) => (
          <NavLink
            key={item.to}
            to={item.to}
            end={item.end}
            className={({ isActive }) =>
              clsx(
                'flex items-center gap-3 rounded-lg px-3 py-2.5 text-sm font-medium transition',
                isActive
                  ? 'bg-gradient-to-r from-rose/20 to-violet/20 text-ink'
                  : 'text-ink-faint hover:bg-paper-sunken hover:text-ink'
              )
            }
          >
            <item.icon className="h-4 w-4" />
            {item.label}
          </NavLink>
        ))}
      </nav>
      <p className="px-3 text-[11px] text-ink-faint">Portfolio Manager · v0.1.0</p>
    </aside>
  )
}

function MobileTabBar() {
  return (
    <nav className="fixed inset-x-0 bottom-0 z-30 flex items-center justify-around border-t border-line bg-paper-raised/95 px-2 py-2 backdrop-blur md:hidden">
      {NAV.map((item) => (
        <NavLink
          key={item.to}
          to={item.to}
          end={item.end}
          className={({ isActive }) =>
            clsx(
              'flex flex-col items-center gap-1 rounded-lg px-3 py-1.5 text-[10px] font-medium',
              isActive ? 'text-rose-deep dark:text-rose' : 'text-ink-faint'
            )
          }
        >
          <item.icon className="h-5 w-5" />
          {item.label}
        </NavLink>
      ))}
    </nav>
  )
}

function Topbar() {
  const { theme, setTheme } = useApp()

  return (
    <header className="sticky top-0 z-20 flex items-center justify-between border-b border-line bg-paper/80 px-4 py-3 backdrop-blur sm:px-6 lg:px-8">
      <div className="md:hidden flex items-center gap-2">
        <div className="flex h-7 w-7 items-center justify-center rounded-lg bg-gradient-to-br from-rose-soft to-violet-soft font-display text-xs font-semibold text-[#2b1132]">
          W
        </div>
        <span className="font-display text-sm">Wealth Ledger</span>
      </div>
      <div className="hidden md:block" />
      <ThemeSwitcher theme={theme} setTheme={setTheme} compact />
    </header>
  )
}
