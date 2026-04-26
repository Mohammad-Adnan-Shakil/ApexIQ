# DeltaBox - AI-Powered Formula 1 Intelligence Platform

**Date**: April 23, 2026  
**Status**: ACTIVE DEVELOPMENT  
**Current Phase**: Feature Implementation + Bug Fixes

---

## 🎯 PROJECT SUMMARY

**DeltaBox** is an AI-powered Formula 1 intelligence platform that predicts race outcomes using a 3-model ML ensemble (XGBoost + Random Forest + Linear Regression). The application provides drivers, teams, races, and AI-powered features with JWT-based authentication.

### Key Stats
- **Backend**: Java 21 + Spring Boot 3.2.5 + PostgreSQL
- **Frontend**: React 19 + Tailwind CSS + Vite
- **ML Engine**: Python 3.9 + XGBoost, Random Forest, Linear Regression
- **Authentication**: JWT + Role-Based Access Control (RBAC)
- **Architecture**: React → Spring Boot → Python ML via ProcessBuilder

---

## 📁 PROJECT FOLDER STRUCTURE

```
DeltaBox/
├── README.md                                    # Main project documentation
├── IMPLEMENTATION_SUMMARY.md                    # Feature implementation details
├── CHANGES_2026_04_19.md                       # Latest changes log
├── FULL_DEPLOYMENT_TEST_REPORT_2026_04_22.md  # Deployment test results
├── TEST_RESULTS_2026_04_22.md                  # Latest test results
├── ROUND2_PERFORMANCE_OPTIMIZATIONS.md         # Performance improvements doc
├── package.json                                 # Root package config (concurrently)
│
├── backend/                                     # Java Spring Boot Backend
│   ├── pom.xml                                 # Maven configuration (Java 21, Spring Boot 3.2.5)
│   ├── mvnw / mvnw.cmd                         # Maven wrapper scripts
│   ├── HELP.md                                 # Backend documentation
│   ├── reset_db.sql                            # Database reset script
│   ├── test-results.txt                        # Test output log
│   │
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/
│   │   │   │   └── com/f1pulse/backend/        # MAIN JAVA PACKAGES
│   │   │   │       ├── BackendApplication.java  # Spring Boot entry point
│   │   │   │       ├── CorsConfig.java          # CORS configuration for frontend
│   │   │   │       ├── SecurityConfig.java      # JWT + security setup
│   │   │   │       │
│   │   │   │       ├── controller/              # REST API Endpoints
│   │   │   │       │   ├── AuthController.java       # Login, Register
│   │   │   │       │   ├── UserController.java       # User profile endpoints
│   │   │   │       │   ├── DriverController.java     # Driver stats/list
│   │   │   │       │   ├── TeamController.java       # Team stats/list
│   │   │   │       │   ├── RaceController.java       # Race schedule/results
│   │   │   │       │   ├── ConstructorController.java # Constructor standings
│   │   │   │       │   ├── HistoricalController.java # Historical F1 data
│   │   │   │       │   ├── AdminController.java      # Admin endpoints
│   │   │   │       │   ├── AdminIngestionController.java # Data ingestion
│   │   │   │       │   ├── TelemetryController.java  # Telemetry data
│   │   │   │       │   └── AIController.java         # AI orchestration
│   │   │   │       │
│   │   │   │       ├── ai/                      # AI & ML Integration Layer
│   │   │   │       │   ├── controller/
│   │   │   │       │   │   ├── PredictionController.java      # Race predictions
│   │   │   │       │   │   ├── SimulationController.java      # What-if scenarios
│   │   │   │       │   │   ├── RaceEngineerController.java    # Engineer insights
│   │   │   │       │   │   ├── DriverComparisonController.java # Driver vs Driver
│   │   │   │       │   │   └── DriverInsightsController.java  # Performance trends
│   │   │   │       │   │
│   │   │   │       │   ├── service/
│   │   │   │       │   │   ├── PredictionServiceImpl.java      # Prediction logic
│   │   │   │       │   │   ├── SimulationServiceImpl.java      # Simulation logic
│   │   │   │       │   │   ├── RaceEngineerService.java       # Race engineer insights
│   │   │   │       │   │   ├── DriverComparisonServiceImpl.java # Comparison logic
│   │   │   │       │   │   └── DriverInsightsServiceImpl.java  # Insights logic
│   │   │   │       │   │
│   │   │   │       │   └── integration/
│   │   │   │       │       └── PythonExecutor.java   # Subprocess manager for Python ML
│   │   │   │       │
│   │   │   │       ├── service/                 # Business Logic Services
│   │   │   │       │   ├── AIService.java             # AI orchestration logic
│   │   │   │       │   ├── F1Service.java             # F1 core services
│   │   │   │       │   ├── ErgastService.java         # Ergast F1 API client
│   │   │   │       │   ├── HistoricalDataIngestionService.java # Data ingestion
│   │   │   │       │   ├── DataInitializationService.java # Initial DB setup
│   │   │   │       │   ├── SyncService.java           # Data synchronization
│   │   │   │       │   │
│   │   │   │       │   ├── impl/
│   │   │   │       │   │   ├── AuthServiceImpl.java    # Authentication impl
│   │   │   │       │   │   └── UserServiceImpl.java    # User management impl
│   │   │   │       │   │
│   │   │   │       ├── entity/                  # JPA Entities (Database Models)
│   │   │   │       │   ├── User.java
│   │   │   │       │   ├── Driver.java
│   │   │   │       │   ├── Team.java
│   │   │   │       │   ├── Race.java
│   │   │   │       │   ├── Constructor.java
│   │   │   │       │   ├── HistoricalSeason.java
│   │   │   │       │   ├── HistoricalRace.java
│   │   │   │       │   ├── HistoricalDriver.java
│   │   │   │       │   └── HistoricalConstructor.java
│   │   │   │       │
│   │   │   │       ├── repository/              # JPA Repositories (DB Access)
│   │   │   │       │   ├── UserRepository.java
│   │   │   │       │   ├── DriverRepository.java
│   │   │   │       │   ├── TeamRepository.java
│   │   │   │       │   ├── RaceRepository.java
│   │   │   │       │   ├── ConstructorRepository.java
│   │   │   │       │   ├── HistoricalSeasonRepository.java
│   │   │   │       │   ├── HistoricalRaceRepository.java
│   │   │   │       │   ├── HistoricalDriverRepository.java
│   │   │   │       │   └── HistoricalConstructorRepository.java
│   │   │   │       │
│   │   │   │       ├── dto/                     # Data Transfer Objects
│   │   │   │       │   ├── PredictionRequest.java
│   │   │   │       │   ├── PredictionResponse.java
│   │   │   │       │   ├── SimulationRequest.java
│   │   │   │       │   ├── AuthRequest.java
│   │   │   │       │   └── (other DTOs)
│   │   │   │       │
│   │   │   │       ├── security/                # JWT & Authentication
│   │   │   │       │   ├── JwtService.java           # JWT token management
│   │   │   │       │   ├── CustomUserDetailsService.java # User auth provider
│   │   │   │       │   └── JwtAuthenticationFilter.java  # JWT validation filter
│   │   │   │       │
│   │   │   │       └── exception/               # Custom Exceptions
│   │   │   │           ├── APIException.java
│   │   │   │           └── (other exceptions)
│   │   │   │
│   │   │   └── resources/
│   │   │       ├── application.properties      # Spring Boot config (DB, ports)
│   │   │       ├── db/
│   │   │       │   └── migration/              # Flyway SQL migrations
│   │   │       │       ├── V1__Add_Historical_Tables.sql
│   │   │       │       ├── V2__Add_Race_Table.sql
│   │   │       │       └── (other migrations)
│   │   │       ├── static/                     # Static files
│   │   │       └── templates/                  # Thymeleaf templates (if any)
│   │   │
│   │   └── test/
│   │       └── java/                           # Unit & Integration Tests
│   │           └── com/f1pulse/backend/
│   │
│   ├── ml/                                      # Python ML Engine
│   │   ├── predict.py                          # Main ML prediction orchestrator
│   │   ├── requirements.txt                    # Python dependencies
│   │   │
│   │   ├── models/                             # Serialized ML Models (joblib)
│   │   │   ├── xgboost_model.pkl
│   │   │   ├── random_forest_model.pkl
│   │   │   └── linear_regression_model.pkl
│   │   │
│   │   ├── data/                               # Training Data
│   │   │   ├── f1_training_data.csv            # F1 race/driver data
│   │   │   └── driver_performance_data.csv     # Performance metrics
│   │   │
│   │   ├── scripts/                            # ML Training & Utility Scripts
│   │   │   ├── train_random_forest.py          # Random Forest training
│   │   │   ├── train_random_forest_v2.py       # Improved version
│   │   │   ├── trainxgboost.py                 # XGBoost training
│   │   │   ├── trainxgboost_v2.py              # Improved version
│   │   │   ├── predict_rf.py                   # RF prediction script
│   │   │   ├── predictxgb.py                   # XGBoost prediction script
│   │   │   ├── ai_orchestrator.py              # Model ensemble orchestrator
│   │   │   └── telemetry_analysis.py           # Telemetry data analysis
│   │   │
│   │   ├── utils/                              # ML Utility Functions
│   │   │   ├── feature_engineering.py          # Feature extraction
│   │   │   └── feature_engineering_v2.py       # Improved features
│   │   │
│   │   └── venv/                               # Python virtual environment
│   │
│   └── target/                                 # Maven build output
│       ├── classes/                            # Compiled .class files
│       ├── generated-sources/                  # Annotation processors
│       └── test-classes/                       # Compiled test classes
│
├── frontend/                                    # React Frontend
│   ├── package.json                            # npm dependencies (React, Tailwind, Vite)
│   ├── vite.config.js                          # Vite build config
│   ├── eslint.config.js                        # ESLint configuration
│   ├── postcss.config.js                       # PostCSS config for Tailwind
│   ├── tailwind.config.js                      # Tailwind CSS config
│   ├── index.html                              # HTML entry point
│   │
│   ├── src/
│   │   ├── main.jsx                            # React app entry point
│   │   ├── App.jsx                             # Main App component
│   │   ├── index.css                           # Global styles
│   │   │
│   │   ├── assets/                             # Images, icons, static files
│   │   │
│   │   ├── components/                         # Reusable React Components
│   │   │   ├── AuthGate.jsx                    # Auth provider wrapper
│   │   │   ├── DriverTable.jsx                 # Driver standings table
│   │   │   ├── ErrorBoundary.jsx               # Error handling component
│   │   │   ├── LiveClock.jsx                   # Live race timer
│   │   │   ├── PointChart.jsx                  # Standings chart (Recharts)
│   │   │   ├── PredicitonCard.jsx              # AI prediction display
│   │   │   ├── SkeletonLoader.jsx              # Loading skeleton
│   │   │   ├── StatCard.jsx                    # Stats display card
│   │   │   │
│   │   │   └── common/                         # Common UI components
│   │   │       ├── Header.jsx
│   │   │       ├── Footer.jsx
│   │   │       └── (other common components)
│   │   │
│   │   ├── context/                            # React Context (State Management)
│   │   │   └── AuthContext.jsx                 # JWT auth state + user
│   │   │
│   │   ├── hooks/                              # Custom React Hooks
│   │   │   ├── useFetch.js                     # Data fetching hook
│   │   │   ├── useInView.js                    # Viewport detection hook
│   │   │   └── usePageTitle.js                 # Page title hook
│   │   │
│   │   ├── layout/                             # Layout Components
│   │   │   ├── MainLayout.jsx                  # Main wrapper layout
│   │   │   └── Sidebar.jsx                     # Navigation sidebar
│   │   │
│   │   ├── pages/                              # Page Components (Routes)
│   │   │   ├── Login.jsx                       # Login page
│   │   │   ├── Register.jsx                    # Register page
│   │   │   ├── Dashboard.jsx                   # Main dashboard
│   │   │   ├── Drivers.jsx                     # Drivers standings page
│   │   │   ├── Teams.jsx                       # Teams standings page
│   │   │   ├── Races.jsx                       # Race calendar page
│   │   │   ├── Predictions.jsx                 # AI predictions page
│   │   │   ├── AIPage.jsx                      # AI features page
│   │   │   ├── TelemetryPage.jsx               # Telemetry analysis
│   │   │   ├── RaceEngineerPage.jsx            # Race engineer insights
│   │   │   └── Profile.jsx                     # User profile page
│   │   │
│   │   ├── routes/                             # Route Protection Components
│   │   │   ├── ProtectedRoute.jsx              # JWT auth protection
│   │   │   └── RequireFeatureAccess.jsx        # RBAC feature access
│   │   │
│   │   ├── services/                           # API Service Layer
│   │   │   └── api.js                          # Axios API client
│   │   │
│   │   └── utils/                              # Utility Functions
│   │       ├── axios.js                        # Axios instance config
│   │       └── formatters.js                   # Data formatting utilities
│   │
│   ├── public/                                 # Public static files
│   │
│   └── node_modules/                           # npm packages (installed)
│
├── db/                                          # Database Scripts
│   └── seed_2026_season.sql                    # 2026 F1 season seed data
│
└── serverside/                                  # Legacy/Archive (not active)
    └── bacnkend/
        └── target/
```

---

## 🔑 KEY FILE DESCRIPTIONS

### Backend - Java Spring Boot

| File | Purpose |
|------|---------|
| `pom.xml` | Maven config: Java 21, Spring Boot 3.2.5, JWT, JPA, PostgreSQL |
| `application.properties` | Database URL, server port, JWT secret, Spring settings |
| `AuthController.java` | `/auth/login`, `/auth/register` endpoints |
| `PredictionController.java` | `/ai/predict` - Race outcome predictions |
| `DriverController.java` | `/drivers` - Driver standings & stats |
| `RaceController.java` | `/races` - Race schedule with status filtering |
| `PythonExecutor.java` | Java ↔ Python subprocess manager |
| `JwtService.java` | JWT token generation & validation |
| `Flyway migrations` | Database schema versioning |

### Backend - Python ML

| File | Purpose |
|------|---------|
| `predict.py` | Main orchestrator: calls XGBoost, Random Forest, Linear Regression |
| `ai_orchestrator.py` | Ensemble logic: detects model disagreement/conflicts |
| `requirements.txt` | Python deps: xgboost, scikit-learn, pandas, numpy |
| `train_random_forest.py` | Trains RF model on historical F1 data |
| `trainxgboost.py` | Trains XGBoost model |
| `feature_engineering.py` | Creates ML features from raw data |

### Frontend - React

| File | Purpose |
|------|---------|
| `App.jsx` | Main app routing & layout |
| `AuthContext.jsx` | JWT token + user state management |
| `ProtectedRoute.jsx` | Route guard for authenticated pages |
| `Dashboard.jsx` | Main landing page (drivers, races, standings) |
| `Predictions.jsx` | AI race prediction interface |
| `api.js` | Axios client for backend API calls |
| `index.css` | Global Tailwind CSS |

---

## 🚀 CURRENT PROJECT STATE

### ✅ COMPLETED FEATURES

1. **Authentication System**
   - JWT token-based auth
   - Login/Register endpoints
   - RBAC (role-based access control)
   - Protected routes on frontend

2. **Historical F1 Data (1950-2026)**
   - Ergast API integration
   - Database schema for historical seasons, races, drivers, teams
   - Flyway migrations for schema versioning
   - Public endpoints for historical stats

3. **2026 Season Data**
   - Driver standings
   - Team standings
   - Race calendar with status (Completed, Upcoming, All)
   - Real-time race clock

4. **ML Prediction System**
   - 3-model ensemble: XGBoost, Random Forest, Linear Regression
   - Python subprocess integration via `ProcessBuilder`
   - Confidence scoring
   - Conflict detection (when models disagree)

5. **Core Pages**
   - Dashboard (overview)
   - Drivers page (standings + stats)
   - Teams page (constructor standings)
   - Races page (calendar with filtering)
   - Predictions page (race outcome predictions)
   - Race Engineer page (insights & simulations)
   - Telemetry page (data analysis)
   - Profile page (user management)

6. **Frontend UI**
   - Recharts visualizations (driver standings, race progress)
   - Framer Motion animations
   - Responsive design (mobile, tablet, desktop)
   - Tailwind CSS styling
   - Live race clock

### ⚠️ IN PROGRESS / KNOWN ISSUES

1. **Java package naming inconsistency**
   - Some files reference `com.deltabox` but actual package is `com.f1pulse.backend`
   - May need alignment in future cleanup

2. **Python ML model loading**
   - Joblib models need verification if they're up-to-date
   - May require retraining

3. **CORS configuration**
   - Recent fix added localhost:5173+ support
   - Needs testing across different ports

### 🔮 NEXT STEPS (Recommended)

1. **Performance Optimization** - Implement caching for driver/race stats
2. **Data Sync** - Add real-time F1 API sync for live race data
3. **Telemetry Integration** - Full telemetry data from F1 API
4. **Advanced Analytics** - Driver comparison, trend analysis
5. **Deployment** - Render.com deployment (mentioned in README)

---

## 🛠️ BUILD & RUN

### Backend (Java)
```bash
cd backend
mvn clean package
java -jar target/backend-0.0.1-SNAPSHOT.jar
```

### Frontend (React)
```bash
cd frontend
npm install
npm run dev          # Vite dev server on :5173
npm run build        # Production build
```

### ML Engine (Python)
```bash
cd backend/ml
python -m venv venv
source venv/bin/activate  # or venv\Scripts\activate on Windows
pip install -r requirements.txt
python predict.py         # Test prediction
```

---

## 📊 TECHNOLOGY STACK

| Layer | Technology | Version |
|-------|-----------|---------|
| **Frontend** | React | 19.2 |
| **Build** | Vite | Latest |
| **Styling** | Tailwind CSS | 3.x |
| **Charts** | Recharts | 3.8 |
| **Animations** | Framer Motion | 12.38 |
| **Routing** | React Router | 7.14 |
| **HTTP Client** | Axios | 1.14 |
| **Backend** | Spring Boot | 3.2.5 |
| **Java** | Java SE | 21 |
| **ORM** | Hibernate/JPA | 6.x |
| **Database** | PostgreSQL | 14+ |
| **Auth** | JWT (jjwt) | 0.11.5 |
| **ML** | XGBoost, scikit-learn | Latest |
| **Integration** | Python subprocess | 3.9+ |

---

## 📝 RECENT CHANGES (2026-04-19 to 04-22)

- ✅ AI prediction system with mock endpoints
- ✅ CORS configuration fixed for frontend
- ✅ Python executor updated (relative paths)
- ✅ Driver-team associations from F1 API
- ✅ Race filtering (All, Completed, Upcoming)
- ✅ Full deployment testing completed
- ✅ Performance optimizations applied

---

## 📞 PROJECT CONTACTS / NOTES

- **Creator**: Mohammad-Adnan-Shakil (GitHub)
- **Live Demo**: Deploying to Render
- **Repository**: github.com/Mohammad-Adnan-Shakil
- **Project Name**: DeltaBox (formerly F1 Pulse)
- **Folder Location**: `c:\projects\DeltaBox\`
- **Test Results**: See `FULL_DEPLOYMENT_TEST_REPORT_2026_04_22.md`

---

**Last Updated**: April 23, 2026  
**Status**: Active Development → Ready for Testing Phase  
**Folder Renamed**: f1-pulse → DeltaBox ✅
