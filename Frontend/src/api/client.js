import axios from 'axios'

// In dev, Vite proxies /api -> http://localhost:8080 (see vite.config.js).
// In prod, set VITE_API_BASE_URL to your deployed Spring Boot host.
const http = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '/api',
  headers: { 'Content-Type': 'application/json' },
})

function parseFieldErrors(details = []) {
  return details.reduce((acc, detail) => {
    if (typeof detail !== 'string') return acc
    const [field, ...rest] = detail.split(':')
    if (!field || rest.length === 0) return acc
    acc[field.trim()] = rest.join(':').trim()
    return acc
  }, {})
}

http.interceptors.response.use(
  (res) => res,
  (err) => {
    const data = err?.response?.data
    const details = Array.isArray(data?.details) ? data.details : []
    const message =
      data?.message ||
      data?.error ||
      err?.message ||
      'Something went wrong talking to the server.'
    const enriched = new Error(message)
    enriched.status = err?.response?.status
    enriched.details = details
    enriched.fieldErrors = parseFieldErrors(details)
    return Promise.reject(enriched)
  }
)

/* ---------------------------- Dashboard ---------------------------- */
export const getDashboard = () => http.get('/dashboard').then((r) => r.data)
export const getPerformance = (params = {}) =>
  http.get('/dashboard/performance', { params }).then((r) => r.data)
export const triggerSnapshot = () => http.post('/dashboard/snapshot').then((r) => r.data)

/* --------------------------- Investments ---------------------------- */
export async function listInvestments(params = {}) {
  try {
    const response = await http.get('/investments', { params })
    return response.data
  } catch (error) {
    const requestedType = String(params?.type || '').toUpperCase()
    const backendRejectedCommodityFilter =
      requestedType === 'COMMODITY' &&
      String(error?.message || '').includes("Method parameter 'type'")

    if (!backendRejectedCommodityFilter) {
      throw error
    }

    // Fallback path for older backend builds: fetch all and filter commodities client-side.
    const fallbackResponse = await http.get('/investments')
    const payload = fallbackResponse.data
    const rows = Array.isArray(payload)
      ? payload
      : Array.isArray(payload?.investments)
        ? payload.investments
        : Array.isArray(payload?.content)
          ? payload.content
          : []
    return rows.filter((row) => String(row?.type || '').toUpperCase() === 'COMMODITY')
  }
}
export const getInvestment = (id) => http.get(`/investments/${id}`).then((r) => r.data)
export const createInvestment = (payload) =>
  http.post('/investments', payload).then((r) => r.data)
export const updateInvestment = (id, payload) =>
  http.put(`/investments/${id}`, payload).then((r) => r.data)
export const refreshInvestmentPrice = (id, payload) =>
  http.patch(`/investments/${id}/price`, payload).then((r) => r.data)
export const deleteInvestment = (id) => http.delete(`/investments/${id}`).then((r) => r.data)

/* --------------------------- Commodities ---------------------------- */
export const listCommodities = () => http.get('/commodities').then((r) => r.data)
export const getCommodity = (id) => http.get(`/commodities/${id}`).then((r) => r.data)
export const createCommodity = (payload) => http.post('/commodities', payload).then((r) => r.data)
export const updateCommodity = (id, payload) => http.put(`/commodities/${id}`, payload).then((r) => r.data)
export const deleteCommodity = (id) => http.delete(`/commodities/${id}`).then((r) => r.data)

/* ----------------------------- Currency ------------------------------ */
export const getBaseCurrency = () => http.get('/settings/base-currency').then((r) => r.data)
export const setBaseCurrency = (payload) =>
  http.put('/settings/base-currency', payload).then((r) => r.data)
export const listExchangeRates = () => http.get('/exchange-rates').then((r) => r.data)
export const setExchangeRate = (currencyCode, payload) =>
  http.put(`/exchange-rates/${currencyCode}`, payload).then((r) => r.data)

/* ---------------------------- Milestones ----------------------------- */
export const listMilestones = () => http.get('/milestones').then((r) => r.data)
export const createMilestone = (payload) =>
  http.post('/milestones', payload).then((r) => r.data)
export const deleteMilestone = (id) => http.delete(`/milestones/${id}`).then((r) => r.data)

/* --------------------------- Transactions ----------------------------- */
export const listAllTransactions = (params = {}) =>
  http.get('/transactions', { params }).then((r) => r.data)
export const listTransactionsForInvestment = (investmentId) =>
  http.get(`/investments/${investmentId}/transactions`).then((r) => r.data)
export const createTransaction = (investmentId, payload) =>
  http.post(`/investments/${investmentId}/transactions`, payload).then((r) => r.data)

export default http
