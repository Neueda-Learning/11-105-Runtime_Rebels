import { useState } from 'react'
import { Plus, Flag, Trash2 } from 'lucide-react'
import { useAsync } from '../api/hooks.js'

const MILESTONE_IMAGES = [
  { keywords: ['ferrari', 'lamborghini', 'porsche', 'car', 'vehicle', 'auto', 'supercar', 'sports car', 'bmw', 'mercedes', 'audi', 'mclaren', 'bugatti'], url: 'https://images.unsplash.com/photo-1544636331-e26879cd4d9b?w=600&q=80' },
  { keywords: ['house', 'home', 'apartment', 'flat', 'property', 'real estate', 'condo', 'villa', 'mansion', 'bungalow', 'down payment'], url: 'https://images.unsplash.com/photo-1570129477492-45c003edd2be?w=600&q=80' },
  { keywords: ['travel', 'vacation', 'trip', 'holiday', 'world tour', 'flight', 'cruise', 'adventure', 'explore', 'backpack'], url: 'https://images.unsplash.com/photo-1488085061387-422e29b40080?w=600&q=80' },
  { keywords: ['retire', 'retirement', 'freedom', 'financial freedom', 'fire', 'independent', 'independence', 'passive income'], url: 'https://images.unsplash.com/photo-1507525428034-b723cf961d3e?w=600&q=80' },
  { keywords: ['education', 'college', 'university', 'degree', 'school', 'mba', 'phd', 'study', 'tuition', 'scholarship'], url: 'https://images.unsplash.com/photo-1523050854058-8df90110c9f1?w=600&q=80' },
  { keywords: ['wedding', 'marriage', 'ring', 'bride', 'honeymoon', 'anniversary'], url: 'https://images.unsplash.com/photo-1519741497674-611481863552?w=600&q=80' },
  { keywords: ['business', 'startup', 'company', 'office', 'entrepreneur', 'invest', 'fund', 'venture'], url: 'https://images.unsplash.com/photo-1497366216548-37526070297c?w=600&q=80' },
  { keywords: ['baby', 'child', 'kid', 'family', 'children', 'college fund', 'education fund'], url: 'https://images.unsplash.com/photo-1476703993599-0035a21b17a9?w=600&q=80' },
  { keywords: ['health', 'medical', 'hospital', 'surgery', 'wellness', 'fitness', 'gym'], url: 'https://images.unsplash.com/photo-1571019613454-1cb2f99b2d8b?w=600&q=80' },
  { keywords: ['yacht', 'boat', 'sail', 'ship', 'ocean', 'sea'], url: 'https://images.unsplash.com/photo-1567899378494-47b22a2ae96a?w=600&q=80' },
  { keywords: ['watch', 'rolex', 'luxury', 'jewel', 'diamond', 'gold'], url: 'https://images.unsplash.com/photo-1523170335258-f5ed11844a49?w=600&q=80' },
]

const DEFAULT_MILESTONE_IMAGE = 'https://images.unsplash.com/photo-1579621970563-ebec7560ff3e?w=600&q=80'

function getMilestoneImage(name) {
  if (!name) return DEFAULT_MILESTONE_IMAGE
  const lower = name.toLowerCase()
  for (const entry of MILESTONE_IMAGES) {
    if (entry.keywords.some((kw) => lower.includes(kw))) return entry.url
  }
  return DEFAULT_MILESTONE_IMAGE
}

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
        <Button
          className="!bg-none !bg-pink-500 !text-white shadow-glass-sm ring-1 ring-pink-400/40 hover:!bg-pink-600 dark:!bg-pink-500 dark:hover:!bg-pink-600 dark:ring-pink-400/35"
          onClick={() => setOpen(true)}
        >
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
          action={
            <Button
              className="!bg-none !bg-pink-500 !text-white shadow-glass-sm ring-1 ring-pink-400/40 hover:!bg-pink-600 dark:!bg-pink-500 dark:hover:!bg-pink-600 dark:ring-pink-400/35"
              onClick={() => setOpen(true)}
            >
              <Plus className="h-4 w-4" /> Add your first milestone
            </Button>
          }
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
            const imageUrl = getMilestoneImage(name)

            return (
              <Card key={id ?? idx} className="relative overflow-hidden !p-0">
                {/* Image banner */}
                <div className="relative h-36 w-full overflow-hidden">
                  <img
                    src={imageUrl}
                    alt={name}
                    className="h-full w-full object-cover"
                    onError={(e) => { e.currentTarget.src = DEFAULT_MILESTONE_IMAGE }}
                  />
                  <div className="absolute inset-0 bg-gradient-to-t from-black/60 to-transparent" />
                  {achieved && (
                    <span className="absolute left-3 top-3 rounded-full bg-jade px-2 py-0.5 text-xs font-semibold text-white">
                      Achieved 🎉
                    </span>
                  )}
                  <button
                    onClick={() => setConfirmDelete(m)}
                    className="absolute right-3 top-3 flex h-7 w-7 items-center justify-center rounded-full bg-black/40 text-white hover:bg-brick/80"
                  >
                    <Trash2 className="h-3.5 w-3.5" />
                  </button>
                </div>

                {/* Card content */}
                <div className="p-4">
                  <p className="font-display text-base text-ink">{name}</p>
                  <p className="text-sm mb-2 text-ink-faint">{comparisonLabel}</p>
                  <p className="text-xs text-ink">Target {formatCurrency(target, baseCurrency, { decimals: 0 })}</p>

                  <div className="mt-4 h-2 w-full overflow-hidden rounded-full bg-paper-sunken">
                    <div
                      className={`h-full rounded-full ${achieved ? 'bg-jade' : 'bg-gradient-to-r from-rose-deep to-violet'}`}
                      style={{ width: `${Math.min(100, Math.max(0, progress))}%` }}
                    />
                  </div>
                  <div className="mt-2 flex items-center justify-between text-xs">
                    <span className="text-ink-faint">
                      {achieved ? 'Achieved 🎉' : `${formatCurrency(target*(1-progress/100), baseCurrency, { decimals: 0 })} to go`}
                    </span>
                    <span className="font-mono font-semibold text-rose-deep dark:text-violet-soft">{progress.toFixed(0)}%</span>
                  </div>
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
            <Button
              className="!bg-none !bg-pink-500 !text-white shadow-glass-sm ring-1 ring-pink-400/40 hover:!bg-pink-600 dark:!bg-pink-500 dark:hover:!bg-pink-600 dark:ring-pink-400/35"
              onClick={handleCreate}
            >
              Add milestone
            </Button>
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
