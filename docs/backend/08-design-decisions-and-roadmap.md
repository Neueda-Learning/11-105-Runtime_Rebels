# Decisions, Trade-offs, and Roadmap

## Key decisions

- One common holdings model was chosen to keep dashboard totals simple and consistent
- Explicit SQL was preferred for predictable behavior while requirements continue to evolve
- Versioned migrations were used for safe, traceable schema changes
- Demo data was isolated to development profile to protect production integrity
- Authentication was intentionally deferred for single-customer v1 scope
- Automated market feeds were deferred in favor of manual price refresh for faster delivery

## Trade-offs

- A universal holdings model introduces a few nullable type-specific fields
- Manual price updates reduce integration complexity but add operational effort
- Read-time currency conversion supports current-view totals but not historical FX locking

## Roadmap

- Authentication and multi-user support
- Live market data integration
- Additional asset classes such as mutual funds, bonds, crypto, and real estate
- Pagination and sorting for larger portfolios
- Exchange-rate history for better historical accuracy
- Milestone alerts and notifications

## Agile planning note

These enhancements are intentionally staged so the team can prioritize based on customer feedback and adoption patterns.
