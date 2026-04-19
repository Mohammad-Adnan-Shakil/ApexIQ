# F1 PULSE — AI-Powered Formula 1 Intelligence Platform

A full-stack AI platform for Formula 1 race analysis, driver performance 
prediction, and strategic simulation. Built with Spring Boot, React, and 
a multi-model Python ML engine.

🔗 Live Demo: [coming soon] · GitHub: github.com/Mohammad-Adnan-Shakil

---

## What It Does

F1 Pulse gives you a complete intelligence layer over the 2026 F1 season:

- **AI Race Prediction** — predict where any driver finishes at any circuit
- **What-If Simulation** — change grid position and see how it impacts outcome
- **Confidence Scoring** — know how reliable each prediction is
- **Live Standings** — real-time driver and constructor standings from PostgreSQL
- **Race Calendar** — full 2026 season with completed vs upcoming status
- **Performance Insights** — trend detection, consistency scoring, model analysis

---

## Tech Stack

### Backend — Java + Spring Boot
- REST API with JWT authentication + Role-Based Access Control (RBAC)
- JPA / Hibernate ORM
- PostgreSQL database
- ProcessBuilder integration for Java ↔ Python ML execution

### Frontend — React + Tailwind CSS
- Animated dashboard with live race clock
- Recharts for driver standings and race progress visualization
- Framer Motion for page transitions and card animations
- Fully responsive — mobile, tablet, desktop

### Machine Learning — Python
- **XGBoost** — race outcome prediction
- **Random Forest** — performance trend analysis  
- **Linear Regression** — average finish baseline
- Custom AI Orchestrator: combines all three models, detects conflicts,
  generates human-readable insights
- Feature engineering pipeline from raw race + driver data
- Joblib for model serialization and loading

### Integration Layer
- Java calls Python via ProcessBuilder (subprocess)
- JSON over STDIN/STDOUT for structured ML communication
- Stateless request/response — no shared memory between services

---

## Architecture
React Frontend
↓ (REST + JWT)
Spring Boot Backend
↓ (ProcessBuilder)
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

---

## AI Engine — How It Works

1. Frontend sends: driverId + raceId + gridPosition
2. Spring Boot fetches driver stats and race history from PostgreSQL
3. Feature vector is constructed (avg finish, consistency, recent form, grid)
4. Java spawns Python subprocess, sends JSON payload via STDIN
5. Python runs XGBoost + Random Forest + Linear Regression in parallel
6. AI Orchestrator compares model outputs:
   - If models agree → high confidence prediction
   - If models conflict → flags uncertainty, returns "conflicting models" insight
7. Response includes: predicted finish, confidence %, trend, what-if impact,
   performance insight text
8. Spring Boot returns enriched JSON to frontend
9. React renders: position badge, confidence ring, simulation cards

---

## Key Engineering Decisions

**Multi-model architecture over single model**  
Using three models lets the orchestrator detect when models disagree — 
which itself is a signal (high uncertainty race) rather than hiding it.

**Cross-language execution via ProcessBuilder**  
Keeps the ML layer in Python (where the ecosystem is strongest) while 
the backend stays in Java (performance, type safety, Spring ecosystem).
No microservice overhead — simple subprocess with JSON I/O.

**JWT + RBAC from scratch**  
Implemented token generation, validation middleware, and role-based 
route protection without using Spring Security's opinionated defaults.

**Feature engineering from raw data**  
Rather than feeding raw finish positions, the pipeline computes: 
rolling average finish, consistency score, recent trend direction, 
grid-to-finish delta — giving models meaningful signal.

---

## API Reference

### POST /api/ai/predict
```json
Request:
{
  "driverId": 1,
  "raceId": 10,
  "gridPosition": 5
}

Response:
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

### GET /api/drivers/standings
### GET /api/races/calendar  
### GET /api/constructors/standings
### POST /api/auth/login
### POST /api/auth/register

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
# Models are loaded automatically by Spring Boot via subprocess
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
f1-pulse/
├── backend/          # Spring Boot — APIs, auth, DB, ML integration
├── frontend/         # React + Tailwind — dashboard, charts, prediction UI
├── ml/               # Python — XGBoost, Random Forest, orchestrator
└── db/               # PostgreSQL schema + 2026 season seed data

---

## Screenshots

| Dashboard | AI Prediction | Driver Standings |
|-----------|--------------|-----------------|
| [add screenshot] | [add screenshot] | [add screenshot] |

---

## Author

**Mohammad Adnan Shakil**  
CSE Student · Presidency University, Bengaluru (2024–2028)  
Building toward backend + full-stack roles at top-tier companies

GitHub · [github.com/Mohammad-Adnan-Shakil](https://github.com/Mohammad-Adnan-Shakil)  
LinkedIn · [linkedin.com/in/Mohammad-Adnan-Shakil](https://linkedin.com/in/Mohammad-Adnan-Shakil)
