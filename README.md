# F1 Pulse — AI-Powered Formula 1 Intelligence Platform

![Java](https://img.shields.io/badge/Java-17-ED8B00?style=flat-square&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-6DB33F?style=flat-square&logo=springboot&logoColor=white)
![React](https://img.shields.io/badge/React-18-61DAFB?style=flat-square&logo=react&logoColor=black)
![Python](https://img.shields.io/badge/Python-3.9-3776AB?style=flat-square&logo=python&logoColor=white)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-14-4169E1?style=flat-square&logo=postgresql&logoColor=white)
![XGBoost](https://img.shields.io/badge/XGBoost-ML-FF6600?style=flat-square)
![JWT](https://img.shields.io/badge/JWT-Auth-000000?style=flat-square&logo=jsonwebtokens)

> F1 Pulse predicts race outcomes using a 3-model ML ensemble (XGBoost + Random Forest + Linear Regression) orchestrated from a Spring Boot backend via subprocess — with conflict detection that flags high-uncertainty races instead of hiding them.

🔗 **Live Demo:** [Coming Soon — deploying to Render] · **GitHub:** [github.com/Mohammad-Adnan-Shakil](https://github.com/Mohammad-Adnan-Shakil)

---

## What It Does

F1 Pulse is a complete intelligence layer over the 2026 F1 season — built for analysis, prediction, and strategic simulation.

| Feature | Description |
|---|---|
| 🤖 **AI Race Prediction** | Predict where any driver finishes at any circuit |
| 🔀 **What-If Simulation** | Change grid position, see how it shifts the predicted outcome |
| 📊 **Confidence Scoring** | Know exactly how reliable each prediction is — and when models disagree |
| 🏆 **Live Standings** | Real-time driver and constructor standings from PostgreSQL |
| 📅 **Race Calendar** | Full 2026 season with completed vs upcoming status |
| 📈 **Performance Insights** | Trend detection, consistency scoring, multi-model analysis |

---

## Engineering Highlights

These are the non-trivial decisions that make this project more than a tutorial:

- **Multi-model conflict detection** — When XGBoost, Random Forest, and Linear Regression disagree, the AI Orchestrator doesn't average them out — it flags the conflict as a "high-uncertainty race" signal. Disagreement itself is data.
- **Cross-language ML execution** — Python ML engine called from Java via `ProcessBuilder` with JSON over STDIN/STDOUT. No microservice overhead, no shared memory. Clean subprocess architecture.
- **JWT + RBAC from scratch** — Token generation, validation middleware, and role-based route protection implemented without Spring Security's opinionated defaults.
- **Feature engineering pipeline** — Models receive rolling average finish, consistency score, recent trend direction, and grid-to-finish delta — not raw position integers.

---

## Tech Stack

### Backend — Java + Spring Boot
- REST API with JWT authentication + Role-Based Access Control (RBAC)
- JPA / Hibernate ORM with PostgreSQL
- `ProcessBuilder` integration for Java ↔ Python ML execution

### Frontend — React + Tailwind CSS
- Animated dashboard with live race clock
- Recharts for driver standings and race progress visualization
- Framer Motion for page transitions and card animations
- Fully responsive — mobile, tablet, desktop

### Machine Learning — Python
- **XGBoost** — race outcome prediction
- **Random Forest** — performance trend analysis
- **Linear Regression** — average finish baseline
- **Custom AI Orchestrator** — combines all three models, detects conflicts, generates human-readable insights
- Feature engineering pipeline from raw race + driver data
- Joblib for model serialization and loading

### Integration Layer
- Java calls Python via `ProcessBuilder` (subprocess)
- JSON over STDIN/STDOUT for structured ML communication
- Stateless request/response — no shared memory between services

---

## Architecture

```
React Frontend
    ↓  (REST + JWT)
Spring Boot Backend
    ↓  (ProcessBuilder)
Python ML Engine
    ↓
AI Orchestrator
    ├── XGBoost Model
    ├── Random Forest Model
    └── Linear Regression Model
    ↓
Prediction + Confidence + Insight Response
    ↓
Spring Boot → React → User
```

---

## AI Engine — How It Works

1. Frontend sends: `driverId` + `raceId` + `gridPosition`
2. Spring Boot fetches driver stats and race history from PostgreSQL
3. Feature vector is constructed: `(avg_finish, consistency, recent_form, grid)`
4. Java spawns Python subprocess, sends JSON payload via STDIN
5. Python runs XGBoost + Random Forest + Linear Regression in parallel
6. AI Orchestrator compares model outputs:
   - Models agree → **high confidence prediction**
   - Models conflict → flags uncertainty, returns `"conflicting models"` insight
7. Response includes: predicted finish, confidence %, trend, what-if impact, performance insight text
8. Spring Boot returns enriched JSON to frontend
9. React renders: position badge, confidence ring, simulation cards

---

## API Reference

### `POST /api/ai/predict`

```json
// Request
{
  "driverId": 1,
  "raceId": 10,
  "gridPosition": 5
}

// Response
{
  "predictedFinish": 2,
  "confidence": 80,
  "confidenceLabel": "HIGH",
  "avgFinish": 1.3,
  "consistency": 98,
  "trend": "IMPROVING",
  "whatIfCurrentAvg": 1.3,
  "whatIfProjectedAvg": 1.2,
  "simulationImpact": "SLIGHT_IMPROVEMENT",
  "performanceInsight": "Driver shows strong consistency with an improving trend"
}
```

### Other Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/api/drivers/standings` | Live driver standings |
| `GET` | `/api/races/calendar` | Full 2026 race calendar |
| `GET` | `/api/constructors/standings` | Constructor championship table |
| `POST` | `/api/auth/login` | Authenticate and receive JWT |
| `POST` | `/api/auth/register` | Register a new user |

---

## Local Setup

### Prerequisites
- Java 17+
- Node.js 18+
- Python 3.9+
- PostgreSQL 14+

### Backend
```bash
cd backend
# Configure application.properties with your PostgreSQL credentials
./mvnw spring-boot:run
```

### ML Engine
```bash
cd ml
pip install -r requirements.txt
# Models load automatically when Spring Boot spawns the subprocess
```

### Frontend
```bash
cd frontend
npm install
npm run dev
# Runs on localhost:5174
```

### Database
```bash
# Run the seed script to populate 2026 season data
psql -U postgres -d f1pulse -f db/seed.sql
```

---

## Project Structure

```
f1-pulse/
├── backend/          # Spring Boot — APIs, auth, DB, ML integration
├── frontend/         # React + Tailwind — dashboard, charts, prediction UI
├── ml/               # Python — XGBoost, Random Forest, orchestrator
└── db/               # PostgreSQL schema + 2026 season seed data
```

---

## Author

**Mohammad Adnan Shakil**
CSE Student · Presidency University, Bengaluru (2024–2028)
Building toward backend + full-stack roles at top-tier companies

[![GitHub](https://img.shields.io/badge/GitHub-Mohammad--Adnan--Shakil-181717?style=flat-square&logo=github)](https://github.com/Mohammad-Adnan-Shakil)
[![LinkedIn](https://img.shields.io/badge/LinkedIn-Mohammad%20Adnan%20Shakil-0A66C2?style=flat-square&logo=linkedin)](https://linkedin.com/in/Mohammad-Adnan-Shakil)
