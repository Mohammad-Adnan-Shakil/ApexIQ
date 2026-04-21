import sys
import json
import subprocess
import os

BASE_DIR = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
SCRIPT_DIR = os.path.join(BASE_DIR, "scripts")

RF_SCRIPT = os.path.join(SCRIPT_DIR, "predict_rf.py")
XGB_SCRIPT = os.path.join(SCRIPT_DIR, "predictxgb.py")


def run_script(script_path, input_json):
    try:
        process = subprocess.Popen(
            [sys.executable, script_path],
            stdin=subprocess.PIPE,
            stdout=subprocess.PIPE,
            stderr=subprocess.PIPE,
            text=True,
        )

        stdout, stderr = process.communicate(json.dumps(input_json))

        if process.returncode != 0:
            return {"error": stderr.strip() or "Model script failed"}

        if not stdout:
            return {"error": "Empty response from script"}

        return json.loads(stdout)

    except Exception as e:
        return {"error": str(e)}


def simulate_impact(predicted, avg_last5):
    if predicted < avg_last5:
        return "positive"
    if predicted > avg_last5:
        return "negative"
    return "neutral"


def generate_insight(rf_pred, xgb_pred, avg_last5, std_last5):
    if abs(rf_pred - xgb_pred) > 5:
        return "Model predictions are conflicting; race outcome is highly uncertain"
    if rf_pred < avg_last5 and std_last5 < 2:
        return "Driver is improving with strong consistency"
    if std_last5 > 4:
        return "Driver performance is unstable and unpredictable"
    return "Driver performance is moderate with no clear trend"


def main():
    try:
        raw_input = sys.stdin.read().strip()

        if not raw_input:
            print(json.dumps({"error": "No input provided"}))
            sys.exit(1)

        input_json = json.loads(raw_input)

    except Exception as e:
        print(json.dumps({"error": f"Invalid input: {str(e)}"}))
        sys.exit(1)

    rf_result = run_script(RF_SCRIPT, input_json)
    xgb_result = run_script(XGB_SCRIPT, input_json)

    if "error" in rf_result or "error" in xgb_result:
        print(json.dumps({
            "error": "Model execution failed",
            "rf_error": rf_result.get("error"),
            "xgb_error": xgb_result.get("error"),
        }))
        sys.exit(1)

    rf_pred = rf_result["predicted_next_position"]
    xgb_pred = xgb_result["predicted_position"]

    avg_last5 = input_json["avg_last_5"]
    std_last5 = input_json["std_last_5"]

    impact = simulate_impact(rf_pred, avg_last5)
    insight = generate_insight(rf_pred, xgb_pred, avg_last5, std_last5)

    response = {
        "driver_id": input_json["driver_id"],
        "rf_prediction": rf_pred,
        "xgb_prediction": xgb_pred,
        "confidence": xgb_result["confidence"],
        "confidence_label": xgb_result["confidence_label"],
        "simulation_impact": impact,
        "final_insight": insight,
        "top_features": xgb_result.get("top_features", [])
    }

    print(json.dumps(response))


if __name__ == "__main__":
    main()
