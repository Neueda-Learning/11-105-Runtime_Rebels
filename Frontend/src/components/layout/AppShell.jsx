import { NavLink, Outlet } from 'react-router-dom'
import { LayoutDashboard, Wallet, Flag, ArrowLeftRight, Settings, LogOut } from 'lucide-react'
import clsx from 'clsx'
import { useState, useRef, useEffect } from 'react'
import { useApp } from '../../context/AppContext.jsx'
import ThemeSwitcher from '../../context/ThemeSwitcher.jsx'
import { Button } from '../ui.jsx'

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
        <div className="min-h-screen min-w-0 flex-1 overflow-hidden pb-24 md:pb-0">
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
  const { theme, setTheme, user, signOut } = useApp()
  const [open, setOpen] = useState(false)
  const ref = useRef(null)

  // close dropdown when clicking outside
  useEffect(() => {
    function handleClick(e) {
      if (ref.current && !ref.current.contains(e.target)) setOpen(false)
    }
    document.addEventListener('mousedown', handleClick)
    return () => document.removeEventListener('mousedown', handleClick)
  }, [])

  return (
    <header className="sticky top-0 z-20 flex items-center justify-between border-b border-line bg-paper/80 px-4 py-3 backdrop-blur sm:px-6 lg:px-8">
      <div className="md:hidden flex items-center gap-2">
        <div className="flex h-7 w-7 items-center justify-center rounded-lg bg-gradient-to-br from-rose-soft to-violet-soft font-display text-xs font-semibold text-[#2b1132]">
          W
        </div>
        <span className="font-display text-sm">Wealth Ledger</span>
      </div>
      <div className="ml-auto flex items-center gap-3">
        <ThemeSwitcher theme={theme} setTheme={setTheme} compact />
        <div ref={ref} className="relative">
          <button
            onClick={() => setOpen(v => !v)}
            className="cursor-pointer rounded-full focus:outline-none focus-visible:ring-2 focus-visible:ring-rose"
          >
            {user?.avatarUrl ? (
              <img src={user.avatarUrl} alt={user.displayName || user.email} className="h-9 w-9 rounded-full border border-line object-cover" />
            ) : (
              <div className="flex h-9 w-9 items-center justify-center rounded-full bg-paper-sunken text-xs font-semibold text-ink-soft">
                {(user?.displayName || user?.email || 'U').slice(0, 1).toUpperCase()}
              </div>
            )}
          </button>
          {open && (
            <div className="absolute right-0 mt-2 w-44 rounded-xl border border-line bg-paper-raised shadow-lg">
              <div className="border-b border-line px-4 py-2.5">
                <p className="truncate text-sm font-semibold text-ink">{user?.displayName || 'User'}</p>
                <p className="truncate text-xs text-ink-faint">{user?.email}</p>
              </div>
              <button
                onClick={() => { setOpen(false); signOut() }}
                className="flex w-full items-center gap-2 px-4 py-2.5 text-sm text-ink-faint hover:bg-paper-sunken hover:text-ink rounded-b-xl transition"
              >
                <LogOut className="h-3.5 w-3.5" /> Sign out
              </button>
            </div>
          )}
        </div>
      </div>
    </header>
  )
}
