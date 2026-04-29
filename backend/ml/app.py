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
        std_last5 = input_data["std_last_5"]
        
        impact = simulate_impact(rf_pred, avg_last5)
        insight = generate_insight(rf_pred, xgb_pred, avg_last5, std_last5)
        
        response = {
            "driver_id": input_data["driver_id"],
            "rf_prediction": rf_pred,
            "xgb_prediction": xgb_pred,
            "confidence": xgb_result["confidence"],
            "confidence_label": xgb_result["confidence_label"],
            "simulation_impact": impact,
            "final_insight": insight,
            "top_features": xgb_result.get("top_features", [])
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

if __name__ == "__main__":
    import uvicorn
    port = int(os.getenv("PORT", 8000))
    uvicorn.run(app, host="0.0.0.0", port=port)
