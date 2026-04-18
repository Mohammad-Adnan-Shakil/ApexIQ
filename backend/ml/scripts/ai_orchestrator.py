import sys
import json
import subprocess
import os

# Paths
BASE_DIR = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
SCRIPT_DIR = os.path.join(BASE_DIR, "scripts")

RF_SCRIPT = os.path.join(SCRIPT_DIR, "predict_rf.py")
XGB_SCRIPT = os.path.join(SCRIPT_DIR, "predictxgb.py")  # adjust name if needed


def run_script(script_path, input_json):
    try:
        process = subprocess.Popen(
            ["python", script_path],
            stdin=subprocess.PIPE,
            stdout=subprocess.PIPE,
            stderr=subprocess.PIPE,
            text=True
        )

        stdout, stderr = process.communicate(json.dumps(input_json))

        if stderr:
            return {"error": stderr.strip()}

        return json.loads(stdout)

    except Exception as e:
        return {"error": str(e)}


def simulate_impact(predicted, avg_last5):
    if predicted < avg_last5:
        return "positive"
    elif predicted > avg_last5:
        return "negative"
    return "neutral"


def generate_insight(rf_pred, xgb_pred, avg_last5, std_last5):
    if abs(rf_pred - xgb_pred) > 5:
        return "Model predictions are conflicting — race outcome is highly uncertain"
    elif rf_pred < avg_last5 and std_last5 < 2:
        return "Driver is improving with strong consistency"
    elif std_last5 > 4:
        return "Driver performance is unstable and unpredictable"
    else:
        return "Driver performance is moderate with no clear trend"


def main():
    try:
        if len(sys.argv) > 1:
            input_json = json.loads(sys.argv[1])
        else:
            input_json = json.loads(sys.stdin.read())
    except:
        print(json.dumps({"error": "Invalid input"}))
        sys.exit(1)

    # 🔥 Call RF (Phase 2)
    rf_result = run_script(RF_SCRIPT, input_json)

    # 🔥 Call XGB (Phase 1)
    xgb_result = run_script(XGB_SCRIPT, input_json)

    if "error" in rf_result or "error" in xgb_result:
        print(json.dumps({
            "error": "Model execution failed",
            "rf_error": rf_result.get("error"),
            "xgb_error": xgb_result.get("error")
        }))
        sys.exit(1)

    rf_pred = rf_result.get("predicted_next_position")
    xgb_pred = xgb_result.get("predicted_position")

    avg_last5 = input_json.get("avg_last_5")
    std_last5 = input_json.get("std_last_5")

    # 🔥 Simulation logic
    impact = simulate_impact(rf_pred, avg_last5)

    # 🔥 Final insight
    insight = generate_insight(rf_pred, xgb_pred, avg_last5, std_last5)

    output = {
        "driver_id": input_json.get("driver_id"),
        "rf_prediction": rf_pred,
        "xgb_prediction": xgb_pred,
        "simulation_impact": impact,
        "final_insight": insight
    }

    print(json.dumps(output))


if __name__ == "__main__":
    main()