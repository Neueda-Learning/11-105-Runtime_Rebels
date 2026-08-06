# How Calculations and Flows Work

This section explains how the backend keeps portfolio numbers accurate and consistent.

## Core calculation rules

### Weighted average price after a buy

- newQty = oldQty + boughtQty
- newAvg = ((oldQty x oldAvg) + (boughtQty x boughtPrice)) / newQty
- investedAmount = newQty x newAvg
- currentValue = newQty x currentPrice

### Realized profit/loss on sell

- realizedPl = soldQty x (sellPrice - avgBuyPrice)
- newQty = oldQty - soldQty
- investedAmount = newQty x avgBuyPrice
- currentValue = newQty x currentPrice
- status becomes CLOSED if newQty equals zero

### Dashboard consolidation

- totalInvested = total cost basis across active investments
- totalCurrentValue = latest value across active investments
- totalPreviousValue = previous snapshot value across active investments
- unrealizedPL = totalCurrentValue - totalInvested
- realizedPL = total realized gain/loss from completed sell events
- overallPL = unrealizedPL + realizedPL
- overallPLPercent = (overallPL / totalInvested) x 100
- todayGainLoss = totalCurrentValue - totalPreviousValue
- todayGainLossPercent = (todayGainLoss / totalPreviousValue) x 100

### Currency conversion model

Amounts stay in original investment currency for data integrity, then are converted to the customer base currency when dashboards and reports are generated.

## Key business flows

### Add investment and then buy more

```mermaid
sequenceDiagram
    actor Customer
    participant IC as InvestmentController
    participant IS as InvestmentService
    participant IR as InvestmentRepository
    participant TC as TransactionController
    participant TS as TransactionService
    participant TR as TransactionRepository

    Customer->>IC: POST /api/investments
    IC->>IS: create
    IS->>IR: save investment

    Customer->>TC: POST /api/investments/{id}/transactions (BUY)
    TC->>TS: record
    TS->>IR: load and update investment
    TS->>TR: save transaction
```

### Build single dashboard view

```mermaid
sequenceDiagram
    actor Customer
    participant DC as DashboardController
    participant DS as DashboardService
    participant IR as InvestmentRepository
    participant TR as TransactionRepository
    participant CS as CurrencyService
    participant MS as MilestoneService

    Customer->>DC: GET /api/dashboard
    DC->>DS: getDashboard
    DS->>IR: findAllActive
    DS->>CS: convert amounts to base
    DS->>TR: realized P/L transactions
    DS->>MS: milestone status and next milestone
    DS-->>DC: dashboard payload
```

### Daily snapshot for trend and day-change metrics

```mermaid
flowchart LR
    T[Scheduled Trigger] --> SS[SnapshotService.captureToday]
    SS --> DS[DashboardService.getDashboard]
    SS --> PR[PortfolioSnapshotRepository.upsert]
    SS --> IR[InvestmentRepository.rollCurrentValueIntoPrevious]
```
