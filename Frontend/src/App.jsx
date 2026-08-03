import { Routes, Route } from 'react-router-dom'
import AppShell from './components/layout/AppShell.jsx'
import Dashboard from './pages/Dashboard.jsx'
import Investments from './pages/Investments.jsx'
import Transactions from './pages/Transactions.jsx'
import Milestones from './pages/Milestones.jsx'
import Settings from './pages/Settings.jsx'

export default function App() {
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
