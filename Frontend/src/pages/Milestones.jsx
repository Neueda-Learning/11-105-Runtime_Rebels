import { useState } from 'react'
import { Plus, Flag, Trash2 } from 'lucide-react'
import { useAsync } from '../api/hooks.js'
import { listMilestones, createMilestone, deleteMilestone } from '../api/client.js'
import { useApp } from '../context/AppContext.jsx'
import { Card, Button, Drawer, Field, inputClass, ConfirmDialog, ErrorState, EmptyState, Skeleton } from '../components/ui.jsx'
import { pick, formatCurrency, formatDate } from '../utils/format.js'

export default function Milestones() { 
  const { baseCurrency, push } = useApp()
  const [open, setOpen] = useState(false)
  const [confirmDelete, setConfirmDelete] = useState(null)
  
  const [form, setForm] = useState({ name: '', thresholdValueBase: '' })
  const { data, loading, error, refetch } = useAsync(listMilestones, [])

  const milestones = Array.isArray(data) ? data : pick(data, ['milestones', 'content'], []) || []
  console.log(milestones)

  async function handleCreate(e) {
    e.preventDefault()
    try {
      await createMilestone({ name: form.name, thresholdValueBase: Number(form.thresholdValueBase),
        comparisonLabel: 'New Milestone', thresholdDate: formatDate(new Date(), 'yyyy-MM-dd')
       })
      push('Milestone added.')
      setForm({ name: '', thresholdValueBase: '' })
      setOpen(false)
      refetch()
    } catch (err) {
      push(err.message, 'error')
    }
  }

  async function handleDelete() {
    try {
      await deleteMilestone(pick(confirmDelete, ['id']))
      push('Milestone removed.')
      setConfirmDelete(null)
      refetch()
    } catch (err) {
      push(err.message, 'error')
    }
  }

  if (error) return <ErrorState message={error.message} onRetry={refetch} />

  return (
    <div className="space-y-5">
      <div className="flex flex-wrap items-center justify-between gap-3">
        <div>
          <h1 className="font-display text-2xl text-ink">Milestones</h1>
          <p className="text-sm text-ink-faint">Turn wealth into something tangible — track progress toward what matters.</p>
        </div>
        <Button onClick={() => setOpen(true)}>
          <Plus className="h-4 w-4" /> New milestone
        </Button>
      </div>

      {loading ? (
        <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-3">
          {Array.from({ length: 3 }).map((_, i) => <Skeleton key={i} className="h-40 w-full" />)}
        </div>
      ) : milestones.length === 0 ? (
        <EmptyState
          icon={Flag}
          title="No milestones yet"
          description="Add an aspirational milestone — a home down payment, a car, financial freedom — and watch your progress climb."
          action={<Button onClick={() => setOpen(true)}><Plus className="h-4 w-4" /> Add your first milestone</Button>}
        />
      ) : (
        <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-3">
          {milestones.map((m, idx) => {
            const id = pick(m, ['id'])
            const name = pick(m, ['name', 'title'], 'Milestone')
            const comparisonLabel = pick(m,['comparisonLabel'])
            const target = pick(m, ['thresholdValueBase'], 0)
            const progress = Number(pick(m, ['progressPercent', 'progress'], 0))
            const remaining = Math.min(100, Math.max(0, progress))
            const achieved = progress >= 100
            

            return (
              <Card key={id ?? idx} className="relative overflow-hidden">
                <button
                  onClick={() => setConfirmDelete(m)}
                  className="absolute right-3 top-3 flex h-7 w-7 items-center justify-center rounded-full text-ink-faint hover:bg-paper-sunken hover:text-brick"
                >
                  <Trash2 className="h-3.5 w-3.5" />
                </button>
                <Flag className={`h-5 w-5 ${achieved ? 'text-gold' : 'text-ink-faint'}`} />
                <p className="mt-3 font-display text-base text-ink">{name}</p>
                <p className="text-sm mb-2 text-ink-faint">{comparisonLabel}</p>
                <p className="text-xs text-ink">Target {formatCurrency(target, baseCurrency, { decimals: 0 })}</p>

                <div className="mt-4 h-2 w-full overflow-hidden rounded-full bg-paper-sunken">
                  <div
                    className={`h-full rounded-full ${achieved ? 'bg-jade' : 'bg-gradient-to-r from-gold-deep to-gold'}`}
                    style={{ width: `${Math.min(100, Math.max(0, progress))}%` }}
                  />
                </div>
                <div className="mt-2 flex items-center justify-between text-xs">
                  <span className="text-ink-faint">
                    {achieved ? 'Achieved 🎉' : `${formatCurrency(target*(1-progress/100), baseCurrency, { decimals: 0 })} to go`}
                  </span>
                  <span className="font-mono font-semibold text-gold-deep dark:text-gold">{progress.toFixed(0)}%</span>
                </div>
              </Card>
            )
          })}
        </div>
      )}

      <Drawer
        open={open}
        onClose={() => setOpen(false)}
        title="New milestone"
        footer={
          <div className="flex justify-end gap-2">
            <Button variant="ghost" onClick={() => setOpen(false)}>Cancel</Button>
            <Button onClick={handleCreate}>Add milestone</Button>
          </div>
        }
      >
        <form onSubmit={handleCreate}>
          <Field label="Name" hint="e.g. Ferrari 296 GTB, Down payment, Financial freedom">
            <input
              required
              className={inputClass}
              value={form.name}
              onChange={(e) => setForm((f) => ({ ...f, name: e.target.value }))}
            />
          </Field>
          <Field label={`Target amount (${baseCurrency})`}>
            <input
              required
              type="number"
              step="any"
              className={inputClass}
              value={form.thresholdValueBase}
              onChange={(e) => setForm((f) => ({ ...f, thresholdValueBase: e.target.value }))}
            />
          </Field>
        </form>
      </Drawer>

      <ConfirmDialog
        open={Boolean(confirmDelete)}
        title="Remove this milestone?"
        onConfirm={handleDelete}
        onCancel={() => setConfirmDelete(null)}
      />
    </div>
  )
}
