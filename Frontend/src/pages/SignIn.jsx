import { useState } from 'react'
import { ShieldCheck } from 'lucide-react'
import { useApp } from '../context/AppContext.jsx'
import { Button, Card } from '../components/ui.jsx'

export default function SignIn() {
  const { signIn, push } = useApp()
  const [loading, setLoading] = useState(false)

  const handleSignIn = async () => {
    setLoading(true)
    try {
      await signIn()
    } catch (error) {
      push(error.message || 'Sign-in failed.', 'error')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="relative flex min-h-screen items-center justify-center overflow-hidden px-4 py-10">
      <div className="absolute inset-0 bg-[radial-gradient(circle_at_top_left,_rgba(232,92,141,0.2),_transparent_35%),radial-gradient(circle_at_bottom_right,_rgba(128,90,213,0.18),_transparent_30%)]" />
      <Card className="relative w-full max-w-md border-white/30 bg-paper-raised/90 p-8 shadow-[0_30px_80px_rgba(84,28,82,0.12)]">
        <div className="mb-8 flex items-center gap-3">
          <div className="flex h-12 w-12 items-center justify-center rounded-2xl bg-gradient-to-br from-rose-soft to-violet-soft text-[#2b1132]">
            <ShieldCheck className="h-6 w-6" />
          </div>
          <div>
            <p className="font-display text-2xl text-ink">Wealth Ledger</p>
            <p className="text-sm text-ink-faint">Personal portfolio access with simple sign-in.</p>
          </div>
        </div>

        <div className="space-y-4">
          <div className="rounded-2xl border border-line/70 bg-paper px-4 py-4 text-sm text-ink-soft">
            Continue with local authentication to access your portfolio dashboard.
          </div>

          <Button type="button" className="w-full" onClick={handleSignIn} disabled={loading}>
            {loading ? 'Signing you in...' : 'Sign in'}
          </Button>

          <p className="text-center text-xs text-ink-faint">No external OAuth provider is required.</p>
        </div>
      </Card>
    </div>
  )
}
