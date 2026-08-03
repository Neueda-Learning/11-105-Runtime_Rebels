# Portfolio Manager — Frontend

A React + Tailwind frontend for the Portfolio Manager Spring Boot API, built around the
"Wealth Command Center" brief: a mobile-first dashboard that answers *how much do I have,
how much did I make today, and what am I close to* within a few seconds.

## Design direction

- **Palette** — deep ink-navy surface with a muted antique gold accent (wealth/milestones),
  jade for gains, brick-rose for losses, and a soft violet for secondary data. Not the default
  "cream + terracotta" or "black + neon" AI-generated look — this leans private-banking ledger.
- **Type** — `Fraunces` (display serif) for the net worth figure and headings, `Manrope` for UI
  text, `IBM Plex Mono` for tabular/ticker data.
- **Signature element** — the Net Worth hero card: an aurora-gradient "ledger" panel with an
  animated count-up figure, a thin gold hairline underline, and an "as of HH:MM" ticker
  timestamp, tying together the private-banking aesthetic and the "I check every morning" habit
  from the customer interview.
- Dark / light / system theme, mobile bottom tab bar + desktop sidebar (mobile-first, per the
  customer's "I wake up and check" note).

## Getting started

```bash
npm install
npm run dev
```

The dev server runs on `http://localhost:5173` and proxies any `/api/*` request to
`http://localhost:8080` (your Spring Boot app) — see `vite.config.js`. No CORS setup needed
locally. For a separate production deployment, set `VITE_API_BASE_URL` (see `.env.example`).

## Pages ↔ endpoints

| Page | Endpoints used |
|---|---|
| Dashboard | `GET /api/dashboard`, `GET /api/dashboard/performance`, `POST /api/dashboard/snapshot`, `GET /api/transactions` |
| Investments | `GET/POST /api/investments`, `PUT/DELETE /api/investments/{id}`, `PATCH /api/investments/{id}/price` |
| Transactions | `GET /api/transactions`, `GET/POST /api/investments/{investmentId}/transactions` |
| Milestones | `GET/POST /api/milestones`, `DELETE /api/milestones/{id}` |
| Settings | `GET/PUT /api/settings/base-currency`, `GET /api/exchange-rates`, `PUT /api/exchange-rates/{currencyCode}` |

## ⚠️ Reconcile with your actual DTOs

The Swagger screenshot you shared listed schema **names**
(`DashboardResponse`, `InvestmentResponse`, `MilestoneResponse`, `TransactionResponse`,
`AllocationItem`, `PortfolioSnapshot`, `PerformancePointResponse`, `ExchangeRate`, etc.) but not
their expanded field lists, so I couldn't read exact JSON key names off the doc.

To avoid the UI breaking silently on a field-name mismatch, every component reads response data
through a small `pick(obj, [...candidateKeys])` helper (`src/utils/format.js`) that tries a few
common naming variants, e.g.:

```js
const netWorth = pick(data, ['currentValue', 'portfolioValue', 'netWorth', 'totalValue'], 0)
```

**Once your backend is running**, hit `/v3/api-docs` (or expand the schemas in Swagger UI) and
compare real field names against the candidate arrays in:

- `src/components/dashboard/NetWorthHero.jsx`
- `src/components/dashboard/DashboardWidgets.jsx`
- `src/components/dashboard/PerformanceChart.jsx`
- `src/components/investments/InvestmentTable.jsx`, `InvestmentDrawer.jsx`
- `src/pages/Transactions.jsx`, `Milestones.jsx`, `Settings.jsx`

Add your real field name to the relevant array (or trim the list down) — no need to touch
rendering logic. The same applies to request payload shapes in the drawer forms
(`InvestmentDrawer.jsx`, `TransactionDrawer.jsx`, `Milestones.jsx`) — field names there
(`quantity`, `purchasePrice`, `currentPrice`, `targetAmount`, etc.) are best guesses from the
customer requirements doc and should be checked against `InvestmentRequest`, `MilestoneRequest`
and `TransactionRequest` in your Swagger doc.

## Stack

React 18 · React Router · Tailwind CSS · Framer Motion · Recharts · react-countup · lucide-react
