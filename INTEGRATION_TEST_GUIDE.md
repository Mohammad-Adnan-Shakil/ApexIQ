# DeltaBox Integration Test Walk-Through Guide

This guide provides step-by-step instructions for manually testing the DeltaBox application end-to-end.

## Prerequisites

1. **Backend Running**: Start the Spring Boot backend
   ```bash
   cd backend
   ./mvnw.cmd spring-boot:run
   ```
   Backend should run on `http://localhost:8080`

2. **Frontend Running**: Start the React frontend
   ```bash
   cd frontend
   npm run dev
   ```
   Frontend should run on `http://localhost:5173`

3. **Database**: PostgreSQL should be running with the DeltaBox database

4. **Environment Variables**: Ensure the following are set:
   - `SPRING_DATASOURCE_URL`
   - `SPRING_DATASOURCE_USERNAME`
   - `SPRING_DATASOURCE_PASSWORD`
   - `JWT_SECRET`
   - `DEEPSEEK_API_KEY`
   - `VITE_GOOGLE_CLIENT_ID` (for frontend)

---

## Test 1: User Registration and Login

### 1.1 Register New User
1. Navigate to `http://localhost:5173/register`
2. Fill in the registration form:
   - Username: `testuser`
   - Email: `test@example.com`
   - Password: `Test1234!`
3. Click "Register"
4. **Expected**: Redirect to login page with success message

### 1.2 Login with Credentials
1. Navigate to `http://localhost:5173/login`
2. Enter credentials:
   - Email: `test@example.com`
   - Password: `Test1234!`
3. Click "Login"
4. **Expected**: Redirect to Dashboard page, user authenticated

### 1.3 Google OAuth Login (Optional)
1. On the login page, click "Sign in with Google"
2. Complete Google OAuth flow
3. **Expected**: Redirect to Dashboard page, user authenticated

---

## Test 2: Dashboard Navigation

### 2.1 View Dashboard
1. After login, verify you're on the Dashboard
2. **Expected**: See dashboard with stats cards, recent races, driver standings

### 2.2 Navigate to Other Pages
1. Click "Drivers" in sidebar
2. **Expected**: Drivers page loads with driver table
3. Click "Teams" in sidebar
4. **Expected**: Teams page loads with constructor standings
5. Click "Races" in sidebar
6. **Expected**: Races page loads with race calendar

---

## Test 3: AI Driver Intelligence

### 3.1 View AI Page
1. Navigate to AI page from sidebar
2. **Expected**: AI Driver Intelligence page loads

### 3.2 Get Driver Prediction
1. Enter Driver Code: `VER`
2. Enter Grid Position: `1`
3. Enter Driver Form: `0.85`
4. Enter Team Performance: `0.90`
5. Enter Track Affinity: `0.75`
6. Click "Analyze"
7. **Expected**: Prediction result displayed with predicted position and confidence

### 3.3 View Model Metrics
1. Scroll to "Model Performance" section
2. **Expected**: See model metrics (top-1, top-3, MAE) for XGBoost, Random Forest, Linear Regression

---

## Test 4: Race Engineer

### 4.1 View Race Engineer Page
1. Navigate to Race Engineer page from sidebar
2. **Expected**: Race Engineer page loads with chat interface

### 4.2 Get Strategic Advice
1. Fill in race context:
   - Lap: `37`
   - Total Laps: `57`
   - Position: `3`
   - Gap to Leader: `+12.4s`
   - Tyre Compound: `SOFT`
   - Tyre Age: `18`
   - Fuel Load: `31.4`
   - Weather: `Dry`
   - Last Lap Time: `1:22.847`
   - Driver Message: `Tires are degrading`
2. Click "Send"
3. **Expected**: Engineer response appears in chat with strategic advice

---

## Test 5: Telemetry Comparison

### 5.1 View Telemetry Page
1. Navigate to Telemetry page from sidebar
2. **Expected**: Telemetry Comparison page loads with form

### 5.2 Compare Driver Telemetry
1. Enter parameters:
   - Year: `2024`
   - Grand Prix: `Monaco`
   - Session: `Q` (Qualifying)
   - Driver 1: `VER`
   - Driver 2: `LEC`
2. Click "Analyze"
3. **Expected**: 
   - Loading indicator appears (first load may take 20-30 seconds)
   - Summary bar shows driver lap times and max gap
   - 5 charts display: Speed, Throttle, Brake, Gear, Time Delta

---

## Test 6: Historical Data Ingestion (Admin)

### 6.1 Trigger Historical Ingestion
1. Use Postman or curl to trigger ingestion:
   ```bash
   curl -X POST http://localhost:8080/api/admin/ingest/historical \
     -H "Authorization: Bearer <your-jwt-token>" \
     -H "Content-Type: application/json" \
     -d '{"fromYear": 2023, "toYear": 2024}'
   ```
2. **Expected**: Returns job ID and status "started"

### 6.2 Check Ingestion Status
1. Use Postman or curl to check status:
   ```bash
   curl http://localhost:8080/api/admin/ingest/status \
     -H "Authorization: Bearer <your-jwt-token>"
   ```
2. **Expected**: Returns ingestion status with counts for seasons, races, drivers, constructors, results, qualifying, standings

---

## Test 7: Protected Routes

### 7.1 Access Protected Route Without Auth
1. Open browser in incognito/private mode
2. Navigate directly to `http://localhost:5173/dashboard`
3. **Expected**: Redirected to login page

### 7.2 Access Protected Route With Auth
1. Login with valid credentials
2. Navigate to any protected page (Dashboard, AI, Race Engineer, Telemetry)
3. **Expected**: Page loads successfully

---

## Test 8: Logout

### 8.1 Logout User
1. Click user profile in top-right corner
2. Click "Logout"
3. **Expected**: Redirected to login page, JWT token cleared from localStorage

---

## Common Issues and Troubleshooting

### Backend Not Starting
- Check PostgreSQL is running
- Verify database connection string in `application.properties`
- Check for port conflicts (8080)

### Frontend Not Starting
- Check Node.js version (should be 18+)
- Run `npm install` if dependencies missing
- Check for port conflicts (5173)

### API Calls Failing
- Verify backend is running
- Check CORS configuration
- Verify JWT token is valid and included in headers

### Telemetry Analysis Failing
- First load takes 20-30 seconds (FastF1 downloads session data)
- Check Python environment has FastF1 installed
- Verify telemetry_analysis.py script exists in `backend/ml/scripts/`

### Google OAuth Not Working
- Verify `VITE_GOOGLE_CLIENT_ID` is set in frontend `.env`
- Check Google Cloud Console OAuth configuration
- Verify redirect URI matches frontend URL

---

## Test Results Checklist

- [ ] User registration works
- [ ] User login works
- [ ] Google OAuth login works (if configured)
- [ ] Dashboard loads and displays data
- [ ] Navigation between pages works
- [ ] AI prediction returns results
- [ ] Model metrics display correctly
- [ ] Race engineer provides strategic advice
- [ ] Telemetry comparison displays 5 charts
- [ ] Historical ingestion starts and reports status
- [ ] Protected routes redirect unauthenticated users
- [ ] Logout clears authentication state

---

## Notes

- Some features require historical data to be ingested first
- Telemetry comparison requires FastF1 to download session data on first load
- Race Engineer requires DeepSeek API key to be configured
- Google OAuth requires Google Cloud Console setup
