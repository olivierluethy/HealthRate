# Swiss Health Insurance Premium Calculator API

A production-quality REST API that calculates Swiss mandatory health-insurance
(**Krankenkasse**) premiums, built with **Java 21** and **Quarkus 3**. It ships
with a clean, Swiss-styled single-page frontend, an OpenAPI/Swagger UI, a
PostgreSQL-backed calculation history and a comprehensive test suite.

> Built as a portfolio project to demonstrate idiomatic Java/Quarkus backend
> engineering for the Swiss insurance domain.

---

## Business Context: How Swiss Health-Insurance Premiums Work

In Switzerland, basic health insurance (*Grundversicherung*, governed by the
**KVG/LAMal**) is mandatory for every resident. While the covered services are
identical across insurers, the monthly premium varies based on a handful of
well-defined factors — exactly the factors this API models:

| Factor | Effect on premium |
| --- | --- |
| **Canton & region** (*Kanton*) | Premiums differ strongly by canton because health-care costs are regional. Modelled as a multiplier per canton (e.g. Genf `1.25`, Appenzell Innerrhoden `0.95`). |
| **Age group** (*Altersgruppe*) | Three bands: *Kind* (0–18), *Junger Erwachsener* (19–25) and *Erwachsener* (26+). Children and young adults pay reduced premiums. |
| **Franchise** (annual deductible) | Choosing a higher deductible (CHF 300 → 2'500) lowers the premium. The base CHF 300 has no discount; CHF 2'500 grants the largest reduction (−32 %). |
| **Accident coverage** (*Unfalleinschluss*) | Persons not covered through an employer can include accident coverage for a +8 % surcharge. |

The premium is assembled multiplicatively:

```
monthlyPremium = basePremium
               × kantonFactor       (regional cost level)
               × franchiseDiscount  (deductible-based discount)
               × unfallSurcharge    (accident coverage surcharge)
               × ageMultiplier      (age-group reduction)
```

> ⚠️ All factors and base premiums are **illustrative** values inspired by the
> 2024 Swiss market and are intended for demonstration only — not a binding offer.

---

## Tech Stack

| Layer | Technology |
| --- | --- |
| Language | Java 21 |
| Framework | Quarkus 3.9.4 |
| REST | Quarkus REST (RESTEasy Reactive) + Jackson |
| Persistence | Hibernate ORM with Panache |
| Database | PostgreSQL 16 (Docker) · H2 in-memory (tests) |
| Validation | Hibernate Validator (Jakarta Bean Validation) |
| API docs | SmallRye OpenAPI + Swagger UI |
| Testing | JUnit 5 + RestAssured |
| Frontend | Single `index.html` · Tailwind CSS (CDN) · Vanilla JS |
| Build | Maven |

---

## Prerequisites

- **JDK 21**
- **Maven 3.8+**
- **Docker** (for the PostgreSQL container)

## Quick Start

```bash
# 1. Start PostgreSQL
docker compose up -d

# 2. Run the application in dev mode (live reload)
mvn quarkus:dev

# 3. Open the app
#    Frontend:    http://localhost:8080
#    Swagger UI:  http://localhost:8080/q/swagger-ui
#    OpenAPI:     http://localhost:8080/api/openapi
```

That's it — the schema is created automatically on startup.

---

## API Documentation

Base URL: `http://localhost:8080`

### `POST /api/premium/calculate`

Calculate the premium for a single franchise level.

```bash
curl -s http://localhost:8080/api/premium/calculate \
  -H 'Content-Type: application/json' \
  -d '{ "canton": "LU", "age": 32, "franchise": 1000, "unfalleinschluss": false }'
```

```json
{
  "monthlyPremium": 324.56,
  "yearlyPremium": 3894.72,
  "ageGroup": "ERWACHSENER",
  "canton": "Luzern",
  "franchise": 1000,
  "breakdown": {
    "basePremium": 370.00,
    "kantonFactor": 1.02,
    "franchiseDiscount": -14,
    "unfallSurcharge": 0,
    "ageMultiplier": 1.0
  }
}
```

### `POST /api/premium/compare`

Calculate all six franchise levels for the same person, sorted by premium ascending.

```bash
curl -s http://localhost:8080/api/premium/compare \
  -H 'Content-Type: application/json' \
  -d '{ "canton": "LU", "age": 32, "unfalleinschluss": false }'
```

Returns an array of six results (cheapest first).

### `GET /api/premium/history`

Return the 20 most recent calculations, newest first.

```bash
curl -s http://localhost:8080/api/premium/history
```

### `GET /api/cantons`

List all 26 Swiss cantons with their code, name, regional factor and base premium.

```bash
curl -s http://localhost:8080/api/cantons
```

### Error responses

All client errors return a uniform JSON body with HTTP `400`:

```json
{ "error": "Age must be between 0 and 120" }
```

```bash
# Negative age → 400
curl -s http://localhost:8080/api/premium/calculate \
  -H 'Content-Type: application/json' \
  -d '{ "canton": "LU", "age": -5, "franchise": 1000, "unfalleinschluss": false }'

# Unknown canton → 400
curl -s http://localhost:8080/api/premium/calculate \
  -H 'Content-Type: application/json' \
  -d '{ "canton": "XX", "age": 32, "franchise": 1000, "unfalleinschluss": false }'
```

---

## Architecture Decisions

**Clean, layered package structure** with a strict dependency direction
(`resource → service → domain/entity`):

```
ch.praemienrechner
├── domain/      enums & value objects (Canton, AgeGroup, Franchise)
├── entity/      Panache entity (PremiumCalculation)
├── service/     business logic (PremiumCalculatorService, CalculationHistoryService)
├── resource/    REST endpoints (PremiumResource, CantonResource)
├── dto/         request/response objects
└── exception/   JSON error mapping
```

- **Why Quarkus?** Fast startup and low memory footprint (ideal for containerised
  cloud deployments), live-reload dev mode for productivity, first-class
  Hibernate/Panache and OpenAPI integration, and a standards-based (Jakarta EE /
  MicroProfile) programming model that Swiss enterprises are familiar with.

- **Why Panache?** It removes boilerplate DAO/repository code via the
  active-record pattern (`PremiumCalculation.persist()`, `findAll(...)`), letting
  the persistence layer stay tiny and readable while still using standard JPA
  underneath.

- **Why enums for the domain?** The Swiss premium rules are a small, fixed,
  well-known set of values (26 cantons, 3 age groups, 6 franchise levels). Enums
  make these **type-safe**, keep the business data (factors, multipliers) next to
  the concept, and make illegal states unrepresentable — far safer than passing
  raw strings/ints around.

- **Decoupled service layer.** `PremiumCalculatorService` contains **pure
  calculation logic** with no persistence or framework dependencies, so it is
  unit-testable with plain JUnit (no container start-up). Persistence is isolated
  in `CalculationHistoryService`, and the REST resources contain **no business
  logic** — they only validate, delegate and return.

- **Money handling.** Premiums use `BigDecimal` with explicit `HALF_UP` rounding
  to two decimals, avoiding floating-point rounding surprises in currency values.

- **Uniform error contract.** `ExceptionMapper`s translate domain
  (`IllegalArgumentException`) and validation (`ConstraintViolationException`)
  failures into a consistent `{ "error": "..." }` response.

---

## Testing

```bash
# Unit tests only (fast, no Docker required)
mvn test

# Unit + integration tests (RestAssured against a running Quarkus instance)
mvn verify
```

Tests run against an **in-memory H2 database** (test profile), so no Docker or
PostgreSQL instance is required to run them.

**`PremiumCalculatorServiceTest`** (pure unit tests):
- Age-group boundaries (0, 18 → Kind; 19, 25 → Junger; 26 → Erwachsener)
- Franchise discount percentages for all six levels
- Monotonicity: higher franchise ⇒ lower premium
- Accident surcharge on/off (and exact +8 % effect)
- Age-multiplier ordering (Kind < Junger < Erwachsener)
- **Exact end-to-end amount**: LU, age 32, franchise 1000 → `CHF 324.56`/month
- Compare returns six results sorted ascending
- Invalid canton / franchise throw `IllegalArgumentException`

**`PremiumResourceIT`** (RestAssured integration tests):
- `POST /calculate` returns `200` with the correct structure & values
- Validation: negative age → `400` with JSON error
- Unknown canton → `400`; invalid franchise → `400`
- `POST /compare` returns six sorted levels
- `GET /history` and `GET /cantons` behave as documented

---

## Screenshots

> _Placeholder — add screenshots of the calculator and comparison views here._

| Prämienrechner | Franchise-Vergleich |
| --- | --- |
| _`docs/screenshot-calculator.png`_ | _`docs/screenshot-compare.png`_ |

---

## Author

**Olivier Lüthy**
Backend project showcasing Java 21 / Quarkus for the Swiss insurance domain.
✉️ olivier.luethy@gmx.net
