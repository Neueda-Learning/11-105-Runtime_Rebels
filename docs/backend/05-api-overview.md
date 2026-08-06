# API Capabilities

The backend exposes endpoints that cover the complete v1 customer journey: maintain holdings, record activity, view consolidated wealth, and track goals.

Interactive API docs are available at /swagger-ui.html when the application is running.

## Endpoint groups

| Method | Path | Business Purpose |
|---|---|---|
| GET | /api/investments | List investments with optional filters |
| GET | /api/investments/{id} | Get one investment |
| POST | /api/investments | Create an investment |
| PUT | /api/investments/{id} | Update descriptive fields |
| PATCH | /api/investments/{id}/price | Update market price/value |
| DELETE | /api/investments/{id} | Delete an investment |
| GET | /api/investments/{id}/transactions | List transactions for one investment |
| POST | /api/investments/{id}/transactions | Record BUY/SELL/DEPOSIT/WITHDRAW/INTEREST |
| GET | /api/transactions | List all transactions |
| GET | /api/dashboard | Consolidated dashboard |
| GET | /api/dashboard/performance?from=&to= | Wealth history for charting |
| POST | /api/dashboard/snapshot | Trigger manual snapshot |
| GET | /api/milestones | List milestones and progress |
| POST | /api/milestones | Create milestone |
| DELETE | /api/milestones/{id} | Delete milestone |
| GET | /api/settings/base-currency | Get base currency |
| PUT | /api/settings/base-currency | Update base currency |
| GET | /api/exchange-rates | List exchange rates |
| PUT | /api/exchange-rates/{code} | Create or update one rate |

## API principles

- API contracts are independent from database table structures
- Validation is handled at request boundaries
- Business logic is centralized in service layer functions
- Monetary values are stored in original currency and converted for customer-facing totals
