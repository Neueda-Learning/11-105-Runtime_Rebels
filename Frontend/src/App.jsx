import { Routes, Route } from 'react-router-dom'
import AppShell from './components/layout/AppShell.jsx'
import Dashboard from './pages/Dashboard.jsx'
import Investments from './pages/Investments.jsx'
import Transactions from './pages/Transactions.jsx'
import Milestones from './pages/Milestones.jsx'
import Settings from './pages/Settings.jsx'
import SignIn from './pages/SignIn.jsx'
import { useApp } from './context/AppContext.jsx'

export default function App() {
  const { authReady, isAuthenticated } = useApp()

  if (!authReady) {
    return <div className="flex min-h-screen items-center justify-center text-sm text-ink-faint">Checking your session...</div>
  }

  if (!isAuthenticated) {
    return <SignIn />
  }

  return (
    <Routes>
      <Route element={<AppShell />}>
        <Route path="/" element={<Dashboard />} />
        <Route path="/investments" element={<Investments />} />
        <Route path="/transactions" element={<Transactions />} />
        <Route path="/milestones" element={<Milestones />} />
        <Route path="/settings" element={<Settings />} />
      </Route>
    </Routes>
  )
}
