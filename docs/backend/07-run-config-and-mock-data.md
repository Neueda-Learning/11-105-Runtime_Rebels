# Run and Configuration Guide

Use this guide when you want to run the backend for demos, validation, or development.

## Option A: Docker Compose (recommended)

1. Copy environment file
2. Start services

Example commands:

- cp .env.example .env
- docker compose up --build

Available endpoints:

- API: http://localhost:8080
- Swagger: http://localhost:8080/swagger-ui.html
- Health: http://localhost:8080/actuator/health

To load realistic demo data:

- SPRING_PROFILES_ACTIVE=dev docker compose up --build

## Option B: Local runtime

1. Build with Maven
2. Run the jar with DB environment overrides

Example commands:

- mvn clean package
- java -jar target/portfolio-manager.jar --DB_URL=jdbc:mysql://localhost:3306/portfolio_manager --DB_USERNAME=portfolio_user --DB_PASSWORD=portfolio_pass

## Configuration variables

| Variable | Default | Purpose |
|---|---|---|
| DB_URL | jdbc:mysql://localhost:3306/portfolio_manager | JDBC URL |
| DB_USERNAME | portfolio_user | DB username |
| DB_PASSWORD | portfolio_pass | DB password |
| SERVER_PORT | 8080 | App port |
| BASE_CURRENCY | INR | Initial base currency |
| SNAPSHOT_CRON | 0 5 0 * * * | Daily snapshot schedule |
| SPRING_PROFILES_ACTIVE | none | Use dev to enable mock data |

## Mock data strategy

- Mock migration V3 runs only in development profile
- Sample data reflects multi-country and multi-currency customer scenarios
- Includes both open and closed positions for complete P/L behavior
- Production profile remains clean with only schema and baseline reference data
