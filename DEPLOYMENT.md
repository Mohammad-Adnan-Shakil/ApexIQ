# DeltaBox Microservices Deployment Guide

This guide covers deploying the DeltaBox F1 platform as microservices on Render.

## Architecture Overview

```
┌─────────────────┐     ┌──────────────────┐     ┌─────────────────┐
│   React Frontend │────▶│  Spring Boot API │────▶│  Flask ML Service│
│   (Static Site)  │     │   (REST API)      │     │  (Python/ML)    │
└─────────────────┘     └──────────────────┘     └─────────────────┘
                               │
                               ▼
                        ┌──────────────┐
                        │  PostgreSQL  │
                        └──────────────┘
```

## Services

1. **deltabox-frontend** - React static site
2. **deltabox-backend** - Spring Boot REST API
3. **deltabox-ml-service** - Flask ML prediction service
4. **deltabox-db** - PostgreSQL database

## Deployment Options

### Option A: Render Blueprint (Recommended)

1. Push all code to GitHub
2. In Render Dashboard, click "Blueprint" and connect your repo
3. Render will read `render.yaml` and create all services automatically

### Option B: Manual Setup

#### 1. Deploy ML Service (Flask)

**Settings:**
- **Name:** deltabox-ml-service
- **Runtime:** Python 3
- **Build Command:** `cd ml-service && pip install -r requirements.txt`
- **Start Command:** `cd ml-service && gunicorn app:app --bind 0.0.0.0:$PORT --workers 2`
- **Health Check Path:** `/health`

**Environment Variables:**
```
PORT=5000
PYTHON_VERSION=3.11.0
```

**Note:** Model files (`models/*.pkl`) must be in the `ml-service/models/` directory or downloaded during build.

#### 2. Deploy Spring Boot Backend

**Settings:**
- **Name:** deltabox-backend
- **Runtime:** Java
- **Build Command:** `cd backend && ./mvnw clean package -DskipTests`
- **Start Command:** `java -jar backend/target/backend-0.0.1-SNAPSHOT.jar`
- **Health Check Path:** `/actuator/health`

**Environment Variables:**
```
DATABASE_URL=postgresql://...
DB_USERNAME=...
DB_PASSWORD=...
ML_SERVICE_URL=https://deltabox-ml-service.onrender.com
JWT_SECRET_KEY=your-secret-key
GROQ_API_KEY=your-groq-key
GOOGLE_CLIENT_ID=your-google-client-id
GOOGLE_CLIENT_SECRET=your-google-client-secret
SPRING_PROFILES_ACTIVE=production
```

**Important:** The `ML_SERVICE_URL` must point to the deployed Flask ML service URL.

#### 3. Deploy Frontend (Static Site)

**Settings:**
- **Name:** deltabox-frontend
- **Type:** Static Site
- **Build Command:** `cd frontend && npm install && npm run build`
- **Publish Directory:** `frontend/dist`

**Environment Variables:**
```
VITE_API_URL=https://deltabox-backend.onrender.com
```

#### 4. Create PostgreSQL Database

- **Name:** deltabox-db
- **Type:** PostgreSQL
- **Plan:** Starter (or higher for production)

## Local Development Setup

### Prerequisites

- Java 21
- Node.js 18+
- Python 3.11
- PostgreSQL 15

### 1. Start the ML Service

```bash
cd ml-service
python -m venv venv
source venv/bin/activate  # On Windows: venv\Scripts\activate
pip install -r requirements.txt

# Ensure models exist in ml-service/models/
python app.py
```

The ML service will run on `http://localhost:5000`

### 2. Start the Spring Boot Backend

```bash
cd backend
./mvnw spring-boot:run
```

The backend will run on `http://localhost:8080` and connect to the ML service at `http://localhost:5000` (configured in `application.properties`).

### 3. Start the Frontend

```bash
cd frontend
npm install
npm run dev
```

The frontend will run on `http://localhost:5173`

## API Endpoints

### ML Service Endpoints

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/health` | GET | Health check for Render |
| `/predict` | POST | Race outcome prediction |
| `/compare` | POST | Driver comparison |
| `/insights` | POST | Performance insights |
| `/simulate` | POST | What-if simulation |
| `/telemetry` | GET | Telemetry analysis |

### Spring Boot Backend Endpoints

| Endpoint | Description |
|----------|-------------|
| `/api/predictions/*` | Prediction APIs (proxied to ML service) |
| `/api/ai/*` | AI/ML features |
| `/api/f1/*` | F1 data APIs |
| `/swagger-ui.html` | API documentation |

## Troubleshooting

### ML Service Issues

1. **Models not loading:** Ensure `models/*.pkl` files exist in `ml-service/models/`
2. **Memory issues:** Increase Render plan or reduce model size
3. **Cold start:** First request may be slow due to model loading

### Backend Issues

1. **ML Service connection:** Check `ML_SERVICE_URL` environment variable
2. **Database connection:** Verify `DATABASE_URL` format: `jdbc:postgresql://host:port/db`
3. **CORS errors:** Ensure ML service has CORS enabled (Flask-CORS is configured)

### Frontend Issues

1. **API not reachable:** Check `VITE_API_URL` points to correct backend URL
2. **Build failures:** Ensure all dependencies are in `package.json`

## Migration from ProcessBuilder

The system has been migrated from subprocess-based ML calls to HTTP-based microservices:

- ❌ **Old:** `PythonExecutor` used `ProcessBuilder` to run Python scripts
- ✅ **New:** `MLClientService` uses `RestTemplate` to call Flask HTTP endpoints

Benefits:
- ML service can be deployed independently
- Better error handling and monitoring
- Easier scaling of ML workers
- Language-agnostic API (could use Node.js, Go, etc. for ML in future)
