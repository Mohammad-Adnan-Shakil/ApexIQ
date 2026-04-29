import os
import json
import pickle
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
    probability_distribution: Optional[Dict[str, float]] = None
    trend: Optional[str] = None

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

def calculate_trend(avg_last5: float, avg_last10: float) -> str:
    """Calculate trend based on recent performance"""
    if avg_last5 < avg_last10 - 1:
        return "IMPROVING"
    elif avg_last5 > avg_last10 + 1:
        return "DECLINING"
    else:
        return "STABLE"

def calculate_prediction_range(predicted: float, confidence: float) -> str:
    """Calculate prediction range based on confidence level"""
    if confidence < 15:
        # Very low confidence - wide range
        min_pos = max(1, int(predicted - 5))
        max_pos = min(20, int(predicted + 5))
        return f"P{min_pos}-P{max_pos}"
    elif confidence < 30:
        # Low confidence - moderate range
        min_pos = max(1, int(predicted - 3))
        max_pos = min(20, int(predicted + 3))
        return f"P{min_pos}-P{max_pos}"
    elif confidence < 50:
        # Moderate confidence - narrow range
        min_pos = max(1, int(predicted - 2))
        max_pos = min(20, int(predicted + 2))
        return f"P{min_pos}-P{max_pos}"
    else:
        # High confidence - single position
        return f"P{int(predicted)}"

def calculate_probability_distribution(predicted: float, confidence: float) -> Dict[str, float]:
    """Calculate probability distribution for finish positions"""
    base_prob = confidence / 100
    
    # Distribute probability based on confidence
    if confidence < 30:
        return {
            "p1": max(0.01, base_prob * 0.2),
            "p2": max(0.02, base_prob * 0.3),
            "p3": max(0.03, base_prob * 0.4),
            "p4plus": max(0.05, 1.0 - (base_prob * 0.9))
        }
    elif confidence < 50:
        return {
            "p1": max(0.05, base_prob * 0.3),
            "p2": max(0.08, base_prob * 0.35),
            "p3": max(0.1, base_prob * 0.35),
            "p4plus": max(0.1, 1.0 - (base_prob))
        }
    else:
        return {
            "p1": max(0.1, base_prob * 0.4),
            "p2": max(0.15, base_prob * 0.35),
            "p3": max(0.2, base_prob * 0.25),
            "p4plus": max(0.15, 1.0 - (base_prob))
        }

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
        
        # Calculate trend
        trend = calculate_trend(avg_last5, avg_last10)
        
        # Get confidence and clamp to minimum 5%
        confidence = xgb_result["confidence"]
        confidence = max(0.05, confidence)  # Clamp to minimum 5%
        
        # Apply trend-aware confidence adjustment
        if trend == "DECLINING":
            confidence *= 0.7  # Reduce confidence for declining trend
            confidence = max(0.05, confidence)  # Still clamp to minimum 5%
        
        # Update confidence label based on adjusted confidence
        if confidence > 0.75:
            confidence_label = "high"
        elif confidence > 0.5:
            confidence_label = "medium"
        else:
            confidence_label = "low"
        
        # Calculate average prediction
        avg_prediction = (rf_pred + xgb_pred) / 2
        
        # Calculate prediction range based on confidence
        predicted_range = calculate_prediction_range(avg_prediction, confidence * 100)
        
        # Calculate probability distribution
        probability_distribution = calculate_probability_distribution(avg_prediction, confidence * 100)
        
        impact = simulate_impact(rf_pred, avg_last5)
        insight = generate_insight(rf_pred, xgb_pred, avg_last5, std_last5)
        
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
            "trend": trend
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
