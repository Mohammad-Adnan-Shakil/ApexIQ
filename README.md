# DeltaBox — AI-Powered Formula 1 Intelligence Platform

![Java](https://img.shields.io/badge/Java-21-ED8B00?style=flat-square&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-6DB33F?style=flat-square&logo=springboot&logoColor=white)
![React](https://img.shields.io/badge/React-18-61DAFB?style=flat-square&logo=react&logoColor=black)
![Python](https://img.shields.io/badge/Python-3.11-3776AB?style=flat-square&logo=python&logoColor=white)
![FastAPI](https://img.shields.io/badge/FastAPI-ML%20Service-009688?style=flat-square&logo=fastapi&logoColor=white)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-14-4169E1?style=flat-square&logo=postgresql&logoColor=white)
![XGBoost](https://img.shields.io/badge/XGBoost-Ensemble-FF6600?style=flat-square)
![JWT](https://img.shields.io/badge/JWT-Auth-000000?style=flat-square&logo=jsonwebtokens)
![Railway](https://img.shields.io/badge/Deployed-Railway-0B0D0E?style=flat-square&logo=railway)
![Vercel](https://img.shields.io/badge/Frontend-Vercel-000000?style=flat-square&logo=vercel)

> DeltaBox is a production-deployed F1 intelligence platform that predicts race outcomes using a 3-model ML ensemble (XGBoost + Random Forest + Linear Regression). The ML layer runs as a standalone FastAPI microservice, called from a Spring Boot backend via HTTP — with conflict detection that flags high-uncertainty races instead of hiding them.

🔗 **Live Demo:** [delta-box.vercel.app](https://delta-box.vercel.app) · **GitHub:** [github.com/Mohammad-Adnan-Shakil](https://github.com/Mohammad-Adnan-Shakil)

---

## What It Does

DeltaBox is a complete intelligence layer over the 2026 F1 season — built for analysis, prediction, and strategic simulation.

| Feature | Description |
|---|---|
| 🤖 **AI Race Prediction** | Predict where any driver finishes at any circuit using XGBoost + Random Forest ensemble |
| 🔀 **What-If Simulation** | Change grid position, see how it shifts the predicted outcome in real time |
| 📊 **Confidence Scoring** | Know exactly how reliable each prediction is — and when models disagree |
| 📡 **Live Telemetry** | Compare lap telemetry between two drivers — speed, throttle, brake, gear, delta |
| 🏆 **Live Standings** | Driver and constructor standings from PostgreSQL, synced with 2026 season data |
| 📅 **Race Calendar** | Full 2026 season with completed vs upcoming race status |
| 📈 **Performance Insights** | Trend detection, consistency scoring, and multi-model analysis per driver |

---

## Engineering Highlights

Non-trivial decisions that separate this from a tutorial project:

- **ML microservice architecture** — Python ML engine deployed as a standalone FastAPI service on Railway. Spring Boot calls it over HTTP, keeping services independently scalable and deployable.
- **Multi-model conflict detection** — When XGBoost and Random Forest disagree beyond a threshold, the AI Orchestrator doesn't average them — it flags the conflict as a high-uncertainty signal. Disagreement itself is data.
- **Live telemetry via fastf1** — Real lap-by-lap telemetry data (speed, throttle, brake, gear, time delta) fetched and processed via the fastf1 library, exposed through the FastAPI service.
- **JWT + RBAC from scratch** — Token generation, validation middleware, and role-based route protection implemented without relying on Spring Security's opinionated defaults.
- **Feature engineering pipeline** — Models receive rolling average finish, consistency score, recent trend direction, and grid-to-finish delta — not raw position integers.

---

## Architecture

```
React Frontend (Vercel)
        ↓  REST + JWT
Spring Boot Backend (Railway)
        ↓  HTTP POST
FastAPI ML Microservice (Railway)
        ↓
   AI Orchestrator
   ├── XGBoost Model (.pkl)
   ├── Random Forest Model (.pkl)
   └── Label Encoders (driver, constructor, track)
        ↓
Prediction + Confidence + Insight + Telemetry Response
        ↓
Spring Boot → React → User
```

---

## Tech Stack

### Backend — Java + Spring Boot
- REST API with JWT authentication and Role-Based Access Control (RBAC)
- JPA / Hibernate ORM with PostgreSQL
- `RestTemplate` HTTP client for Java ↔ Python ML service communication
- Deployed on Railway with environment-based configuration

### Frontend — React + Tailwind CSS
- Animated dashboard with live race clock
- Recharts for driver standings and performance visualization
- Framer Motion for page transitions and card animations
- Fully responsive — mobile, tablet, desktop
- Deployed on Vercel

### Machine Learning — Python + FastAPI
- **XGBoost** — race outcome prediction
- **Random Forest** — performance trend analysis
- **Custom AI Orchestrator** — combines models, detects conflicts, generates human-readable insights
- **fastf1** — live lap telemetry data (speed, throttle, brake, gear, delta)
- FastAPI microservice with `/predict` and `/telemetry` endpoints
- Models serialized with pickle (protocol=4), loaded at startup
- Deployed as a separate Railway service

### Infrastructure
- Spring Boot → FastAPI communication over HTTPS (Railway internal routing)
- Environment variables for all secrets (`JWT_SECRET`, `ML_SERVICE_URL`, `SPRING_DATASOURCE_*`)
- PostgreSQL with persistent volume on Railway
- Auto-deploy on push via GitHub integration

---

## AI Engine — How It Works

1. Frontend sends: `driverId` + `raceId` + `gridPosition`
2. Spring Boot fetches driver stats and race history from PostgreSQL
3. Feature vector is constructed: `(avg_finish, consistency, recent_form, grid, track, constructor)`
4. Spring Boot POSTs JSON payload to FastAPI `/predict` endpoint
5. FastAPI runs XGBoost + Random Forest in parallel via AI Orchestrator
6. Orchestrator compares model outputs:
   - Models agree → **high confidence prediction**
   - Models conflict → flags uncertainty, returns `"conflicting models"` insight
7. Response: predicted finish, confidence %, trend, what-if impact, top features, performance insight
8. Spring Boot returns enriched JSON to frontend
9. React renders: position badge, confidence ring, simulation cards, insight text

---

## API Reference

### `POST /api/ai/intelligence`

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
  "rfPrediction": 2,
  "xgbPrediction": 3,
  "simulationImpact": "SLIGHT_IMPROVEMENT",
  "finalInsight": "Driver shows strong consistency with an improving trend",
  "topFeatures": ["grid_position", "avg_last_5", "track_id"]
}
```

### Other Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/api/drivers/standings` | Live driver standings |
| `GET` | `/api/races/calendar` | Full 2026 race calendar |
| `GET` | `/api/constructors/standings` | Constructor championship table |
| `GET` | `/api/telemetry/compare` | Lap telemetry comparison between two drivers |
| `POST` | `/api/auth/login` | Authenticate and receive JWT |
| `POST` | `/api/auth/register` | Register a new user |

---

## Local Setup

### Prerequisites
- Java 21+
- Node.js 18+
- Python 3.11+
- PostgreSQL 14+

### Backend
```bash
cd backend
# Set environment variables or edit application.properties
./mvnw spring-boot:run
```

### ML Service
```bash
cd backend/ml
pip install -r requirements.txt
uvicorn app:app --host 0.0.0.0 --port 8000
# Models load automatically at startup
```

### Frontend
```bash
cd frontend
npm install
npm run dev
# Runs on localhost:5173
```

### Environment Variables
```env
# Backend (Railway)
SPRING_DATASOURCE_URL=jdbc:postgresql://...
SPRING_DATASOURCE_USERNAME=...
SPRING_DATASOURCE_PASSWORD=...
JWT_SECRET=...
ML_SERVICE_URL=https://your-ml-service.up.railway.app

# ML Service (Railway)
PORT=8000
```

---

## Project Structure

```
deltabox/
├── backend/
│   ├── src/                  # Spring Boot — APIs, auth, DB, ML integration
│   └── ml/
│       ├── app.py            # FastAPI entrypoint
│       ├── scripts/          # ai_orchestrator.py, telemetry_analysis.py
│       ├── models/           # .pkl model files
│       ├── requirements.txt
│       └── railway.toml
├── frontend/                 # React + Tailwind — dashboard, charts, prediction UI
└── db/                       # PostgreSQL schema + 2026 season seed data
```

---

## Author

**Mohammad Adnan Shakil**  
CSE Student · Presidency University, Bengaluru (2024–2028)  
Building toward backend + full-stack roles at top-tier companies

[![GitHub](https://img.shields.io/badge/GitHub-Mohammad--Adnan--Shakil-181717?style=flat-square&logo=github)](https://github.com/Mohammad-Adnan-Shakil)
[![LinkedIn](https://img.shields.io/badge/LinkedIn-Mohammad%20Adnan%20Shakil-0A66C2?style=flat-square&logo=linkedin)](https://linkedin.com/in/Mohammad-Adnan-Shakil)
