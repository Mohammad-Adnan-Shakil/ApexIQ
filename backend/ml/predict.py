import sys
import json

def predict(data):
    grid = data.get("gridPosition", 0)
    form = data.get("driverForm", 0)
    team = data.get("teamPerformance", 0)
    track = data.get("trackAffinity", 0)

    score = (0.4 * grid) + (0.3 * form) + (0.2 * team) + (0.1 * track)

    predicted_position = max(1, 20 - score)

    return {
        "predictedPosition": round(predicted_position, 2),
        "confidence": "MEDIUM"
    }

if __name__ == "__main__":
    try:
        # 🔥 READ FROM STDIN (CRITICAL)
        input_json = sys.stdin.read()

        if not input_json:
            raise Exception("No input received")

        data = json.loads(input_json)

        result = predict(data)

        print(json.dumps(result))
        sys.stdout.flush()   # 🔥 VERY IMPORTANT

    except Exception as e:
        print(json.dumps({
            "error": str(e)
        }))
        sys.stdout.flush()