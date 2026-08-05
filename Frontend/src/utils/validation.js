const NUMBER_PATTERN = /^-?\d+(\.\d+)?$/

export const VALIDATION_MESSAGES = {
  required: 'This field is required.',
  invalid: 'Please enter a valid value.',
  invalidEmail: 'Please enter a valid email address.',
  invalidAmount: 'Please enter a valid amount.',
  selectOption: 'Please select an option.',
}

export function isBlank(value) {
  return String(value ?? '').trim().length === 0
}

export function isValidSelection(value) {
  return !isBlank(value)
}

export function toTrimmedString(value) {
  return String(value ?? '').trim()
}

export function parseNumber(value) {
  const raw = String(value ?? '').trim()
  if (!raw || !NUMBER_PATTERN.test(raw)) return null
  const parsed = Number(raw)
  return Number.isFinite(parsed) ? parsed : null
}

export function isPositiveNumber(value) {
  const parsed = parseNumber(value)
  return parsed !== null && parsed > 0
}

export function isValidDate(value) {
  const raw = String(value ?? '').trim()
  if (!/^\d{4}-\d{2}-\d{2}$/.test(raw)) return false

  const [year, month, day] = raw.split('-').map(Number)
  const date = new Date(year, month - 1, day)
  return (
    date.getFullYear() === year &&
    date.getMonth() === month - 1 &&
    date.getDate() === day
  )
}

export function hasValidationErrors(errors) {
  return Object.values(errors).some(Boolean)
}

export function getApiErrorMessage(error, fallback = 'Something went wrong. Please try again.') {
  if (!error) return fallback
  const detail = Array.isArray(error.details) && error.details.length > 0 ? error.details[0] : null
  return detail || error.message || fallback
}

export function mapApiFieldErrors(error, mapping = {}) {
  const fieldErrors = error?.fieldErrors || {}
  const mapped = {}

  Object.entries(fieldErrors).forEach(([field, message]) => {
    const target = mapping[field] || field
    mapped[target] = message || VALIDATION_MESSAGES.invalid
  })

  return mapped
}

