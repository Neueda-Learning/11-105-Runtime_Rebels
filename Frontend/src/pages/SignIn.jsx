import { useEffect, useRef, useState } from 'react'
import { ShieldCheck } from 'lucide-react'
import { useApp } from '../context/AppContext.jsx'
import { Button, Card } from '../components/ui.jsx'

const GOOGLE_CLIENT_ID = import.meta.env.VITE_GOOGLE_CLIENT_ID

export default function SignIn() {
  const { signInWithGoogleToken, push } = useApp()
  const googleButtonRef = useRef(null)
  const [loading, setLoading] = useState(false)

  useEffect(() => {
    if (!GOOGLE_CLIENT_ID) {
      return
    }

    const google = window.google
    if (!google?.accounts?.id || !googleButtonRef.current) {
      return
    }

    google.accounts.id.initialize({
      client_id: GOOGLE_CLIENT_ID,
      callback: async ({ credential }) => {
        if (!credential) return
        setLoading(true)
        try {
          await signInWithGoogleToken(credential)
        } catch (error) {
          push(error.message || 'Sign-in failed.', 'error')
        } finally {
          setLoading(false)
        }
      },
    })

    google.accounts.id.renderButton(googleButtonRef.current, {
      theme: 'outline',
      size: 'large',
      shape: 'pill',
      text: 'signin_with',
      width: 280,
    })
  }, [push, signInWithGoogleToken])

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
            <p className="text-sm text-ink-faint">Personal portfolio access with Google sign-in.</p>
          </div>
        </div>

        <div className="space-y-4">
          <div className="rounded-2xl border border-line/70 bg-paper px-4 py-4 text-sm text-ink-soft">
            Each Google account gets its own investments, milestones, settings, and dashboard history.
          </div>

          {GOOGLE_CLIENT_ID ? (
            <div className="flex justify-center" ref={googleButtonRef} />
          ) : (
            <div className="rounded-2xl border border-brick/30 bg-brick/5 px-4 py-4 text-sm text-brick">
              Set VITE_GOOGLE_CLIENT_ID in the frontend environment to enable Google sign-in.
            </div>
          )}

          {loading && (
            <p className="text-center text-sm text-ink-faint">Signing you in...</p>
          )}

          <Button
            type="button"
            variant="ghost"
            className="w-full"
            onClick={() => window.location.reload()}
          >
            Reload Google sign-in
          </Button>
        </div>
      </Card>
    </div>
  )
}
