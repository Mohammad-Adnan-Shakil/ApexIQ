import sys
import json
import joblib
import pandas as pd
import os

BASE_DIR = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
MODEL_DIR = os.path.join(BASE_DIR, "models")


def encode_safe(le, val):
    try:
        return le.transform([val])[0]
    except:
        return 0


def predict(input_data: dict, model=None, le_constructor=None, le_driver=None, le_track=None):
    """Predict using Random Forest model"""
    # Load models if not provided
    if model is None:
        model = joblib.load(os.path.join(MODEL_DIR, "rf_model.pkl"))
    if le_driver is None:
        le_driver = joblib.load(os.path.join(MODEL_DIR, "le_driver.pkl"))
    
    driver_encoded = encode_safe(le_driver, input_data["driver_id"])
    
    features = pd.DataFrame([{
        "driver_id": driver_encoded,
        "avg_last_5": input_data["avg_last_5"],
        "std_last_5": input_data["std_last_5"],
        "avg_last_10": input_data["avg_last_10"],
        "std_last_10": input_data["std_last_10"],
        "last_race_position": input_data["last_race_position"]
    }])
    
    prediction = model.predict(features)[0]
    
    output = {
        "predicted_next_position": round(float(prediction), 2)
    }
    
    return output


# Legacy subprocess support (for backward compatibility)
if __name__ == "__main__":
    try:
        input_json = json.loads(sys.stdin.read())
    except Exception:
        print(json.dumps({"error": "Invalid input"}))
        sys.exit(1)
    
    result = predict(input_json)
    print(json.dumps(result))