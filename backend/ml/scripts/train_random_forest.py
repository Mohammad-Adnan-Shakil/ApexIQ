import pandas as pd
import numpy as np
import pickle
import os

from sklearn.model_selection import train_test_split
from sklearn.ensemble import RandomForestRegressor
from sklearn.preprocessing import LabelEncoder
from sklearn.metrics import mean_absolute_error

# Paths
BASE_DIR = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
DATA_PATH = os.path.join(BASE_DIR, "data/driver_performance_data.csv")
MODEL_DIR = os.path.join(BASE_DIR, "models")

# Load data
df = pd.read_csv(DATA_PATH)

print("Shape:", df.shape)
print(df.head())

# Encode driver_id (important)
le_driver = LabelEncoder()
df["driver_id"] = le_driver.fit_transform(df["driver_id"])

# Features
X = df[
    [
        "driver_id",
        "avg_last_5",
        "std_last_5",
        "avg_last_10",
        "std_last_10",
        "last_race_position"
    ]
]

# Target
y = df["target_next_race_position"]

# Split
X_train, X_test, y_train, y_test = train_test_split(
    X, y, test_size=0.2, random_state=42
)

# Model
model = RandomForestRegressor(
    n_estimators=200,
    max_depth=10,
    random_state=42
)

model.fit(X_train, y_train)

# Evaluate
y_pred = model.predict(X_test)
mae = mean_absolute_error(y_test, y_pred)

print("\nMAE:", mae)

# Feature importance
print("\nFeature Importance:")
for name, score in zip(X.columns, model.feature_importances_):
    print(f"{name}: {score:.4f}")

# Save
os.makedirs(MODEL_DIR, exist_ok=True)

with open(os.path.join(MODEL_DIR, "rf_model.pkl"), "wb") as f:
    pickle.dump(model, f, protocol=4)
with open(os.path.join(MODEL_DIR, "le_driver.pkl"), "wb") as f:
    pickle.dump(le_driver, f, protocol=4)

print("\nRandom Forest model saved successfully.")