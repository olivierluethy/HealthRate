# 🇨🇭 Swiss Health Insurance Premium Calculator

A small web application that calculates how much a person would pay each month for
basic Swiss health insurance (*Krankenkasse*), based on where they live, how old
they are, and which options they choose.

> **In one sentence:** type in a canton, an age and a few options → instantly see
> the monthly and yearly premium, with a clear breakdown of how the number was
> calculated.

This project was built as a portfolio piece to demonstrate modern Java backend
engineering (Java 21 + Quarkus) for the Swiss insurance industry.

---

## 📖 Table of Contents

1. [What this app does](#-what-this-app-does)
2. [How premiums work in Switzerland (the business idea)](#-how-premiums-work-in-switzerland-the-business-idea)
3. [How the app is built (in plain language)](#-how-the-app-is-built-in-plain-language)
4. [**How to set it up and run it**](#-how-to-set-it-up-and-run-it)  ← *start here to try it*
5. [How to use it once it's running](#-how-to-use-it-once-its-running)
6. [Troubleshooting](#-troubleshooting)
7. [For technical reviewers](#-for-technical-reviewers)
8. [Author](#-author)

---

## 🎯 What this app does

In Switzerland, health insurance is mandatory and the price (the **premium**) depends
on a handful of factors. This app turns those rules into a working calculator.

It has **two main features**, shown side by side on one page:

| Feature | What it does |
| --- | --- |
| **Prämienrechner** (Premium Calculator) | You enter canton, age, deductible (*Franchise*) and whether you want accident coverage. It shows your **monthly** and **yearly** premium plus a breakdown. |
| **Franchise-Vergleich** (Deductible Comparison) | It calculates all six deductible levels at once, so you can see how much you'd save by choosing a higher deductible. |

**Example:** A 32-year-old in the canton of Lucerne (*Luzern*), with a CHF 1'000
deductible and no accident coverage, gets a premium of **CHF 324.56 per month
(CHF 3'894.72 per year)**.

> ⚠️ The prices are realistic but **illustrative** — this is a demonstration project,
> not a real insurance offer.

---

## 🧮 How premiums work in Switzerland (the business idea)

Everyone in Switzerland must have basic health insurance, and the medical services
covered are the same everywhere. But the **monthly price differs from person to
person**, based on four things — and the app models exactly these:

1. **Where you live (Canton).** Healthcare costs more in some regions than others.
   Geneva is expensive; Appenzell is cheaper. Each canton has a "price factor."
2. **Your age group.**
   - *Kind* (Child): 0–18 years → pays the least
   - *Junger Erwachsener* (Young adult): 19–25 years → pays a bit less
   - *Erwachsener* (Adult): 26+ years → pays the full price
3. **Your deductible (*Franchise*).** This is how much you agree to pay yourself
   each year before insurance starts covering costs. Choosing a **higher
   deductible lowers your monthly premium** (CHF 300 = full price, CHF 2'500 = the
   biggest discount, −32 %).
4. **Accident coverage (*Unfalleinschluss*).** If your employer doesn't already cover
   accidents, you can add it for a **+8 %** surcharge.

The final price is simply these factors multiplied together:

```
Monthly premium =  Base price
                ×  Canton factor        (e.g. Lucerne = 1.02)
                ×  Deductible discount   (e.g. CHF 1'000 = −14 %)
                ×  Accident surcharge    (+8 % if chosen)
                ×  Age factor            (children & young adults pay less)
```

The app shows this whole breakdown to the user, so the final number is never a
mystery.

---

## 🏗️ How the app is built (in plain language)

You don't need to understand code to get the big picture. The app has three parts
that talk to each other:

```
   ┌─────────────────────┐        ┌────────────────────────┐        ┌──────────────┐
   │   The web page      │  asks  │   The "brain" (server) │ stores │   Database   │
   │ (what you see in    │ ─────► │  does the calculations │ ─────► │ (remembers   │
   │  the browser)       │ ◄───── │  and applies the rules │ ◄───── │  past calc-  │
   │  Buttons & forms    │ answer │                        │  reads │  ulations)   │
   └─────────────────────┘        └────────────────────────┘        └──────────────┘
        Frontend                    Backend (Java/Quarkus)              PostgreSQL
```

- **The web page (frontend):** a clean, simple screen with dropdowns and buttons.
  When you click *"Prämie berechnen"*, it sends your inputs to the server.
- **The brain (backend):** a Java program that knows all the Swiss insurance rules.
  It receives your inputs, calculates the premium and sends the result back.
- **The database:** every calculation is saved, so the app can show a history of
  the most recent ones (this proves the data is stored properly, like in a real
  system).

The app also publishes an **interactive API documentation** page (Swagger UI) where
the calculations can be tried out directly — useful for developers and a sign of
professional, well-documented software.

---

## 🚀 How to set it up and run it

This is the important part. Follow the steps in order — it takes about 5 minutes.

### Step 1 — Install the three things you need

You need these installed once on your computer. Click the links to download them:

| Tool | Why | Where to get it |
| --- | --- | --- |
| **Java 21** (JDK) | Runs the application | <https://adoptium.net/temurin/releases/?version=21> |
| **Maven** | Builds and starts the application | <https://maven.apache.org/install.html> |
| **Docker Desktop** | Runs the database in a container | <https://www.docker.com/products/docker-desktop/> |

> **How to check they're installed.** Open a terminal (on Windows: *PowerShell*;
> on Mac/Linux: *Terminal*) and run these — each should print a version number:
> ```bash
> java -version      # should say 21.x
> mvn -version       # should also report Java 21.x
> docker --version
> ```
> If `java -version` shows a number other than 21, see [Troubleshooting](#-troubleshooting).

### Step 2 — Get the project onto your computer

If you received the project as a folder, just open a terminal **inside that folder**.
If it's on GitHub, download it (the green *Code → Download ZIP* button) and unzip it,
or clone it:

```bash
git clone <repository-url>
cd HealthRate
```

### Step 3 — Start it with three commands

Run these one after another, from inside the project folder:

```bash
# 1. Start the database (runs quietly in the background)
docker compose up -d

# 2. Build and start the application (this prints a lot of text — that's normal)
mvn quarkus:dev
```

When you see a line like **`Listening on: http://localhost:8080`**, it's ready. 🎉

### Step 4 — Open it in your browser

| What | Address |
| --- | --- |
| 👉 **The app itself** | <http://localhost:8080> |
| The interactive API docs (Swagger) | <http://localhost:8080/q/swagger-ui> |

### Step 5 — How to stop it

- To stop the **application**: go back to the terminal and press **`Ctrl + C`**.
- To stop the **database**: run `docker compose down`.

That's it. To start it again later, just repeat Step 3.

---

## 🖱️ How to use it once it's running

1. Open <http://localhost:8080>.
2. **To calculate one premium** (left panel):
   - Pick a **Kanton** (canton), enter an **Alter** (age), choose a **Franchise**
     (deductible), and optionally switch on **Unfalleinschluss** (accident coverage).
   - Click **"Prämie berechnen"** → the monthly and yearly price appear, with the
     full breakdown underneath.
3. **To compare deductibles** (right panel):
   - Enter canton and age, then click **"Alle Franchisen vergleichen"**.
   - A table shows all six deductible levels and how much you'd **save per year**
     compared to the standard CHF 300 deductible.

---

## 🔧 Troubleshooting

**"`release version 21 not supported`" or the wrong Java version**
Your computer is using an older Java. Make sure Java 21 is installed (Step 1) and
that your terminal uses it. On Mac/Linux you can point to it for the current
session with:
```bash
export JAVA_HOME=/path/to/jdk-21
```
Then run `mvn -version` again to confirm it now says 21.

**"`port is already allocated`" or "`Bind for 0.0.0.0:5432 failed`"**
Another program (often another database) is already using the database port `5432`.
Either stop that program, or change the port in the file `docker-compose.yml`
(e.g. to `"5433:5432"`) and update the matching line in
`src/main/resources/application.properties` to use `5433`.

**"`Port 8080 already in use`"**
Another app is using the web port. Stop it, or change `quarkus.http.port` in
`src/main/resources/application.properties` to a free port (e.g. `8081`) and open
that address instead.

**The page loads but says "Error restarting Quarkus"**
The application started before the database was ready. Make sure
`docker compose up -d` finished and the database is healthy, then simply reload the
page — the app retries automatically.

---

## 👩‍💻 For technical reviewers

<details>
<summary>Click to expand the technical details</summary>

### Tech stack

| Layer | Technology |
| --- | --- |
| Language | Java 21 |
| Framework | Quarkus 3.9.4 |
| REST | Quarkus REST (RESTEasy Reactive) + Jackson |
| Persistence | Hibernate ORM with Panache |
| Database | PostgreSQL 16 (Docker) · H2 in-memory (tests) |
| Validation | Hibernate Validator (Jakarta Bean Validation) |
| API docs | SmallRye OpenAPI + Swagger UI |
| Testing | JUnit 5 + RestAssured (29 tests) |
| Frontend | Single `index.html` · Tailwind CSS · Vanilla JS |

### Project structure

```
ch.praemienrechner
├── domain/      enums & rules (Canton, AgeGroup, Franchise)
├── entity/      database entity (PremiumCalculation)
├── service/     business logic (PremiumCalculatorService, CalculationHistoryService)
├── resource/    REST endpoints (PremiumResource, CantonResource)
├── dto/         request/response objects
└── exception/   uniform JSON error handling
```

### Key design decisions

- **Strict layering** (`resource → service → domain`): REST classes contain *no*
  business logic — they only validate input, delegate, and return.
- **Pure, framework-free calculation logic** in `PremiumCalculatorService`, so it can
  be unit-tested with plain JUnit (no server start-up needed).
- **Type-safe domain** modelled with enums — the 26 cantons, 3 age groups and 6
  deductible levels are fixed, known sets, so illegal values are impossible.
- **`BigDecimal` for money** with explicit rounding, avoiding floating-point errors.
- **Uniform error contract**: invalid input always returns `400` with
  `{ "error": "..." }`.

### API endpoints

| Method | Path | Purpose |
| --- | --- | --- |
| `POST` | `/api/premium/calculate` | Calculate one premium |
| `POST` | `/api/premium/compare` | Compare all six deductible levels |
| `GET` | `/api/premium/history` | Last 20 calculations |
| `GET` | `/api/cantons` | All 26 cantons with their factors |

Example:
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

### Running the tests

```bash
mvn test      # 21 fast unit tests (no Docker needed — uses in-memory H2)
mvn verify    # adds 8 RestAssured integration tests
```

</details>

---

## 👤 Author

**Olivier Lüthy**
Portfolio project demonstrating Java 21 / Quarkus backend engineering for the Swiss
insurance domain.
✉️ olivier.luethy@gmx.net
