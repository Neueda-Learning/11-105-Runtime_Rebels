# Portfolio Manager User Stories

This folder contains customer-ready user stories for GitHub tracking.
Recommendation: create one GitHub Issue per story.

## US-001 Consolidated Dashboard

User story:
As a customer, I want to see all my investments in one dashboard, so that I can understand my total wealth in one place.

Acceptance criteria:
- Given I hold multiple asset types, when I open the dashboard, then I can see total invested and total current value.
- Given investments are in different currencies, when values are shown, then totals are converted into my selected base currency.
- Given there are no investments, when I open the dashboard, then I see a clear empty-state message.

Priority: High
Estimate: 5 story points

## US-002 Add and Manage Investments

User story:
As a customer, I want to add and update my investments, so that my portfolio remains accurate.

Acceptance criteria:
- Given valid investment details, when I create an investment, then it is saved with type, symbol, country, and currency.
- Given an existing investment, when I edit supported fields, then updates are saved and visible.
- Given I no longer need a holding, when I delete it, then it is removed from active views.

Priority: High
Estimate: 5 story points

## US-003 Record Transactions

User story:
As a customer, I want to record buy, sell, deposit, withdraw, and interest transactions, so that my portfolio history stays complete.

Acceptance criteria:
- Given an investment exists, when I add a transaction, then it appears in history with date, type, and amount.
- Given a BUY transaction, when it is saved, then quantity and weighted average cost are recalculated.
- Given a SELL transaction, when it is saved, then realized P/L is calculated and stored.

Priority: High
Estimate: 8 story points

## US-004 Realized and Unrealized Profit and Loss

User story:
As a customer, I want realized and unrealized P/L displayed separately, so that I can judge performance accurately.

Acceptance criteria:
- Given completed SELL transactions, when I view the dashboard, then realized P/L is displayed.
- Given active holdings, when I view the dashboard, then unrealized P/L is displayed.
- Given both values exist, when I view overall performance, then total P/L and P/L percent are displayed.

Priority: High
Estimate: 5 story points

## US-005 Daily Gain/Loss and Performance History

User story:
As a customer, I want daily gain/loss and trend history, so that I can track portfolio movement over time.

Acceptance criteria:
- Given snapshots exist, when I open dashboard, then today gain/loss amount and percent are shown.
- Given a date range, when I request performance history, then daily chart points are returned.
- Given today snapshot is missing, when scheduler or manual trigger runs, then today snapshot is created.

Priority: Medium
Estimate: 5 story points

## US-006 Allocation Insights

User story:
As a customer, I want allocation by type, country, and currency, so that I can understand concentration risk.

Acceptance criteria:
- Given active investments exist, when allocation is computed, then each group has value and percentage.
- Given allocation is displayed, when totals are checked, then percentages total near 100 percent.
- Given base-currency reporting, when values are shown, then all groups are shown in base currency.

Priority: Medium
Estimate: 3 story points

## US-007 Milestones and Goal Tracking

User story:
As a customer, I want milestones for wealth targets, so that I can track progress against goals.

Acceptance criteria:
- Given a threshold is reached, when dashboard is refreshed, then milestone is marked achieved with achieved date.
- Given multiple milestones, when dashboard loads, then next milestone and progress are displayed.
- Given custom milestones, when I add or delete one, then list updates correctly.

Priority: Medium
Estimate: 3 story points

## US-008 Base Currency and Exchange Rates

User story:
As a customer, I want to set base currency and exchange rates, so that all reporting reflects my preferred currency.

Acceptance criteria:
- Given a configured base currency, when dashboard values load, then conversion uses that base currency.
- Given an exchange rate update, when dashboard recalculates, then latest rates are reflected.
- Given settings are opened, when I view exchange rates, then configured rates are listed.

Priority: High
Estimate: 5 story points

## US-009 API Documentation for Integration

User story:
As a frontend developer, I want clear API documentation, so that I can integrate endpoints with confidence.

Acceptance criteria:
- Given backend is running, when Swagger UI is opened, then all active endpoints are visible.
- Given an endpoint contract, when tested via Swagger, then request and response structures are clear.
- Given invalid input, when request fails validation, then error messages are meaningful.

Priority: Medium
Estimate: 3 story points

## US-010 Demo-Ready Local Environment

User story:
As a team member, I want one-command startup with optional mock data, so that I can demo quickly.

Acceptance criteria:
- Given Docker is installed, when compose is run, then app and database start successfully.
- Given dev profile is active, when migrations run, then mock data is loaded.
- Given service startup completed, when health endpoint is checked, then status is UP.

Priority: Medium
Estimate: 3 story points

## Suggested Labels

- story
- backend
- frontend
- dashboard
- api
- high-priority

## Suggested Milestones

- Sprint 1: Core investments and transactions
- Sprint 2: Dashboard and currency conversion
- Sprint 3: Milestones, snapshots, and hardening
