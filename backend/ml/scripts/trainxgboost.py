import pandas as pd
import numpy as np
import pickle
import os

from sklearn.model_selection import train_test_split
from sklearn.metrics import mean_absolute_error, mean_squared_error
from sklearn.preprocessing import LabelEncoder

from xgboost import XGBRegressor

# Paths
BASE_DIR = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
DATA_PATH = os.path.join(BASE_DIR, "data/f1_training_data.csv")
MODEL_DIR = os.path.join(BASE_DIR, "models")

# Load dataset
df = pd.read_csv(DATA_PATH)

# Encode categorical features
le_constructor = LabelEncoder()
le_track = LabelEncoder()

df['constructor_id'] = le_constructor.fit_transform(df['constructor_id'])
df['track_id'] = le_track.fit_transform(df['track_id'])

# Features & Target
X = df[[
    'qualifying_position',
    'constructor_id',
    'track_id',
    'season_year',
    'recent_avg_position_last_5',
    'recent_std_last_5',
    'grid_position',
    'is_home_race'
]]

y = df['finishing_position']

# Split
X_train, X_test, y_train, y_test = train_test_split(
    X, y, test_size=0.2, random_state=42
)

# Model
model = XGBRegressor(
    n_estimators=300,
    learning_rate=0.05,
    max_depth=6,
    subsample=0.8,
    colsample_bytree=0.8,
    random_state=42
)

model.fit(X_train, y_train)

# Predictions
y_pred = model.predict(X_test)

# Metrics
mae = mean_absolute_error(y_test, y_pred)
rmse = np.sqrt(mean_squared_error(y_test, y_pred))

print("MAE:", mae)
print("RMSE:", rmse)

# Feature importance
print("\nFeature Importance:")
for name, score in zip(X.columns, model.feature_importances_):
    print(f"{name}: {score:.4f}")

# Save models
os.makedirs(MODEL_DIR, exist_ok=True)

with open(os.path.join(MODEL_DIR, "xgb_model.pkl"), "wb") as f:
    pickle.dump(model, f, protocol=4)
with open(os.path.join(MODEL_DIR, "le_constructor.pkl"), "wb") as f:
    pickle.dump(le_constructor, f, protocol=4)
with open(os.path.join(MODEL_DIR, "le_track.pkl"), "wb") as f:
    pickle.dump(le_track, f, protocol=4)

print("\nModel saved successfully.")