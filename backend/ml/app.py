import os
import json
import pickle
import math
from fastapi import FastAPI, HTTPException
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel
from typing import List, Dict, Any, Optional, Union
import sys

# Add scripts directory to path for imports
SCRIPT_DIR = os.path.join(os.path.dirname(__file__), "scripts")
sys.path.insert(0, SCRIPT_DIR)

BASE_DIR = os.path.dirname(os.path.abspath(__file__))
MODELS_DIR = os.path.join(BASE_DIR, "models")

app = FastAPI(title="DeltaBox ML Service", version="1.0.0")

# CORS middleware
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# Global model cache
models = {}

def load_models():
    """Load all ML models on startup"""
    global models
    try:
        models["rf"] = pickle.load(open(os.path.join(MODELS_DIR, "rf_model.pkl"), "rb"))
        models["xgb"] = pickle.load(open(os.path.join(MODELS_DIR, "xgb_model.pkl"), "rb"))
        models["le_constructor"] = pickle.load(open(os.path.join(MODELS_DIR, "le_constructor.pkl"), "rb"))
        models["le_driver"] = pickle.load(open(os.path.join(MODELS_DIR, "le_driver.pkl"), "rb"))
        models["le_track"] = pickle.load(open(os.path.join(MODELS_DIR, "le_track.pkl"), "rb"))
        print("✅ All models loaded successfully")
    except Exception as e:
        print(f"❌ Error loading models: {e}")
        raise

# Load models on startup
@app.on_event("startup")
def startup_event():
    load_models()

class PredictionRequest(BaseModel):
    driver_id: Union[str, int] = 0
    avg_last_5: Optional[float] = 0.0
    std_last_5: Optional[float] = 0.0
    avg_last_10: Optional[float] = 0.0
    std_last_10: Optional[float] = 0.0
    last_race_position: Optional[float] = 0.0
    qualifying_position: Optional[int] = 0
    constructor_id: Optional[str] = "unknown"
    track_id: Optional[str] = "unknown"
    season_year: Optional[int] = 2026
    recent_avg_position_last_5: Optional[float] = 0.0
    recent_std_last_5: Optional[float] = 0.0
    grid_position: Optional[int] = 0
    is_home_race: Optional[int] = 0
    career_avg_finish: Optional[float] = 0.0
    season_avg_finish: Optional[float] = 0.0
    recent_avg_finish: Optional[float] = 0.0

class FeatureImportance(BaseModel):
    feature: str
    importance: float
    explanation: str

class PredictionResponse(BaseModel):
    driver_id: int
    rf_prediction: float
    xgb_prediction: float
    confidence: float
    confidence_label: str
    simulation_impact: str
    final_insight: str
    top_features: List[FeatureImportance]
    predicted_range: Optional[str] = None
    probability_distribution: Optional[List[Dict[str, float]]] = None
    trend: Optional[str] = None
    uncertainty_factors: Optional[List[str]] = None
    performance_breakdown: Optional[Dict[str, float]] = None

def simulate_impact(predicted: float, avg_last5: float) -> str:
    if predicted < avg_last5:
        return "positive"
    if predicted > avg_last5:
        return "negative"
    return "neutral"

def generate_insight(rf_pred: float, xgb_pred: float, avg_last5: float, std_last5: float) -> str:
    if abs(rf_pred - xgb_pred) > 5:
        return "Model predictions are conflicting; race outcome is highly uncertain"
    if rf_pred < avg_last5 and std_last5 < 2:
        return "Driver is improving with strong consistency"
    if std_last5 > 4:
        return "Driver performance is unstable and unpredictable"
    return "Driver performance is moderate with no clear trend"

def calculate_trend(recent_avg_finish: float, season_avg_finish: float) -> str:
    """Calculate trend based on recent vs season performance"""
    if recent_avg_finish < season_avg_finish - 1:
        return "IMPROVING"
    elif recent_avg_finish > season_avg_finish + 1:
        return "DECLINING"
    else:
        return "STABLE"

def compute_weighted_finish(career_avg: float, season_avg: float, recent_avg: float) -> float:
    """Compute weighted average finish using multi-timescale model"""
    weights = {
        "career": 0.25,
        "season": 0.45,
        "recent": 0.30
    }
    
    return (
        weights["career"] * career_avg +
        weights["season"] * season_avg +
        weights["recent"] * recent_avg
    )

def calculate_prediction_range(avg_finish: float, confidence: float, trend: str, simulation_impact: str) -> str:
    """Calculate prediction range based on confidence level, trend, and simulation impact"""
    # Base range from confidence (strict ranges)
    if confidence < 15:
        range_min, range_max = 5, 10
    elif confidence < 30:
        range_min, range_max = 3, 6
    elif confidence < 60:
        range_min, range_max = 2, 4
    else:
        range_min, range_max = 1, 2
    
    # Adjust using trend
    if trend == "DECLINING":
        range_min += 1
        range_max += 2
    
    # Adjust using simulation (negative impact means projectedAvg > avgFinish)
    if simulation_impact == "negative":
        range_min += 1
        range_max += 1
    
    # Clamp to valid range
    range_min = max(1, min(20, range_min))
    range_max = max(1, min(20, range_max))
    
    # Ensure min <= max
    if range_min > range_max:
        range_min, range_max = range_max, range_min
    
    return f"P{range_min}–P{range_max}"

def calculate_uncertainty_factors(confidence: float, trend: str, std_last5: float, simulation_impact: str) -> List[str]:
    """Calculate factors contributing to low confidence"""
    factors = []
    
    if confidence < 30:
        factors.append("Low confidence due to limited data or inconsistent performance")
    
    if trend == "DECLINING":
        factors.append("Declining recent performance trend")
    
    if std_last5 > 3:
        factors.append("High performance variance (unstable results)")
    
    if simulation_impact == "negative":
        factors.append("Projected performance drop in simulation")
    
    if confidence < 15:
        factors.append("Outcome variance is very high")
    
    return factors if factors else ["Prediction based on stable performance data"]

def calculate_probability_distribution(avg_finish: float, confidence: float) -> List[Dict[str, float]]:
    """Calculate probability distribution for finish positions using gaussian distribution"""
    distribution = []
    variance = (100 - confidence) / 100 * 5
    
    for pos in range(1, 21):
        prob = math.exp(-math.pow(pos - avg_finish, 2) / (2 * variance))
        distribution.append({
            "position": pos,
            "probability": prob
        })
    
    # Normalize to sum to 1.0
    total = sum(d["probability"] for d in distribution)
    for d in distribution:
        d["probability"] = d["probability"] / total
    
    return distribution

def run_prediction(input_data: Dict[str, Any]) -> Dict[str, Any]:
    """Run ML prediction using loaded models"""
    try:
        # Import prediction functions from scripts
        from predict_rf import predict as rf_predict
        from predictxgb import predict as xgb_predict
        
        # Run RF prediction
        rf_result = rf_predict(input_data, models["rf"], models["le_constructor"], models["le_driver"], models["le_track"])
        
        # Run XGBoost prediction
        xgb_result = xgb_predict(input_data, models["xgb"], models["le_constructor"], models["le_driver"], models["le_track"])
        
        rf_pred = rf_result["predicted_next_position"]
        xgb_pred = xgb_result["predicted_position"]
        
        avg_last5 = input_data["avg_last_5"]
        avg_last10 = input_data["avg_last_10"]
        std_last5 = input_data["std_last_5"]
        
        # Get multi-timescale performance data
        career_avg = input_data.get("career_avg_finish", avg_last5)
        season_avg = input_data.get("season_avg_finish", avg_last5)
        recent_avg = input_data.get("recent_avg_finish", avg_last5)
        
        # Fallback to legacy fields if new fields not provided
        if career_avg == 0:
            career_avg = avg_last5
        if season_avg == 0:
            season_avg = avg_last5
        if recent_avg == 0:
            recent_avg = avg_last5
        
        # Calculate trend using recent vs season performance
        trend = calculate_trend(recent_avg, season_avg)
        
        # Get confidence and clamp to minimum 5%
        confidence = xgb_result["confidence"]
        confidence = max(0.05, confidence)  # Clamp to minimum 5%
        
        # Apply trend-aware confidence adjustment
        if trend == "DECLINING":
            confidence *= 0.7  # Reduce confidence for declining trend
            confidence = max(0.05, confidence)  # Still clamp to minimum 5%
        
        # Adjust confidence based on performance divergence
        divergence = abs(season_avg - recent_avg)
        if divergence > 2.0:
            confidence *= 0.8  # Reduce confidence when season and recent diverge significantly
            confidence = max(0.05, confidence)
        
        # Update confidence label based on adjusted confidence
        if confidence > 0.75:
            confidence_label = "high"
        elif confidence > 0.5:
            confidence_label = "medium"
        else:
            confidence_label = "low"
        
        # Calculate average prediction
        avg_prediction = (rf_pred + xgb_pred) / 2
        
        # Use weighted multi-timescale model for base finish position
        avg_finish = compute_weighted_finish(career_avg, season_avg, recent_avg)
        
        # Calculate simulation impact
        impact = simulate_impact(rf_pred, avg_finish)
        
        # Calculate prediction range based on confidence, trend, and simulation impact
        predicted_range = calculate_prediction_range(avg_finish, confidence * 100, trend, impact)
        
        # Validation guard: prevent invalid prediction states
        if confidence * 100 < 30 and predicted_range.startswith("P1"):
            raise ValueError("Invalid state: High outcome with low confidence")
        
        # Calculate uncertainty factors
        uncertainty_factors = calculate_uncertainty_factors(confidence * 100, trend, std_last5, impact)
        
        # Calculate probability distribution
        probability_distribution = calculate_probability_distribution(avg_finish, confidence * 100)
        
        # Generate insight with recent vs season comparison
        if recent_avg > season_avg + 1:
            insight = "Recent performance is declining compared to season average"
        elif recent_avg < season_avg - 1:
            insight = "Recent performance is improving compared to season average"
        else:
            insight = generate_insight(rf_pred, xgb_pred, avg_finish, std_last5)
        
        # Build performance breakdown
        performance_breakdown = {
            "career": career_avg,
            "season": season_avg,
            "recent": recent_avg,
            "weighted": avg_finish
        }
        
        response = {
            "driver_id": input_data["driver_id"],
            "rf_prediction": rf_pred,
            "xgb_prediction": xgb_pred,
            "confidence": confidence,
            "confidence_label": confidence_label,
            "simulation_impact": impact,
            "final_insight": insight,
            "top_features": xgb_result.get("top_features", []),
            "predicted_range": predicted_range,
            "probability_distribution": probability_distribution,
            "trend": trend,
            "uncertainty_factors": uncertainty_factors,
            "performance_breakdown": performance_breakdown
        }
        
        return response
        
    except Exception as e:
        return {"error": str(e)}

@app.post("/predict", response_model=PredictionResponse)
async def predict(request: PredictionRequest):
    """Run ML prediction for race outcome"""
    try:
        # Convert Pydantic model to dict
        input_data = request.dict()
        
        # Convert driver_id to string for model compatibility
        input_data["driver_id"] = str(input_data.get("driver_id", "0"))
        
        # Run prediction
        result = run_prediction(input_data)
        
        if "error" in result:
            raise HTTPException(status_code=500, detail=result["error"])
        
        return result
        
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Prediction failed: {str(e)}")

@app.get("/health")
async def health():
    """Health check endpoint"""
    return {"status": "healthy", "models_loaded": len(models) > 0}

@app.get("/telemetry")
async def telemetry(year: int, grand_prix: str, session_type: str, driver1: str, driver2: str):
    """Analyze telemetry for two drivers from a specific F1 session"""
    try:
        from telemetry_analysis import analyze
        result = analyze(year, grand_prix, session_type, driver1, driver2)
        
        if "error" in result:
            raise HTTPException(status_code=500, detail=result["error"])
        
        return result
        
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Telemetry analysis failed: {str(e)}")

if __name__ == "__main__":
    import uvicorn
    port = int(os.getenv("PORT", 8000))
    uvicorn.run(app, host="0.0.0.0", port=port)
