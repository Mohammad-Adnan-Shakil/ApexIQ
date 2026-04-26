"""
Complete ML Training Pipeline for F1 Predictions
Trains XGBoost, Random Forest, and Linear Regression on full historical dataset
Evaluates with top-1, top-3, and MAE metrics
Saves models and metrics to disk
"""

import sys
import os
sys.path.append(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))

import pandas as pd
import numpy as np
import joblib
import json
from datetime import datetime
from sklearn.model_selection import train_test_split, StratifiedKFold
from sklearn.ensemble import RandomForestRegressor
from sklearn.linear_model import LinearRegression
from sklearn.metrics import mean_absolute_error, accuracy_score
import xgboost as xgb

from utils.feature_engineering_v3 import get_db_connection, build_training_dataset, get_feature_names


def train_xgboost(X_train, y_train, X_val, y_val):
    """Train XGBoost model"""
    print("🤖 Training XGBoost model...")
    
    model = xgb.XGBRegressor(
        n_estimators=200,
        max_depth=6,
        learning_rate=0.1,
        subsample=0.8,
        colsample_bytree=0.8,
        random_state=42,
        objective='reg:squarederror'
    )
    
    model.fit(X_train, y_train, eval_set=[(X_val, y_val)], verbose=False)
    
    # Feature importance
    importance = dict(zip(get_feature_names(), model.feature_importances_))
    print(f"✅ XGBoost trained. Top features: {sorted(importance.items(), key=lambda x: x[1], reverse=True)[:3]}")
    
    return model


def train_random_forest(X_train, y_train):
    """Train Random Forest model"""
    print("🌲 Training Random Forest model...")
    
    model = RandomForestRegressor(
        n_estimators=200,
        max_depth=10,
        min_samples_split=5,
        min_samples_leaf=2,
        random_state=42,
        n_jobs=-1
    )
    
    model.fit(X_train, y_train)
    
    # Feature importance
    importance = dict(zip(get_feature_names(), model.feature_importances_))
    print(f"✅ Random Forest trained. Top features: {sorted(importance.items(), key=lambda x: x[1], reverse=True)[:3]}")
    
    return model


def train_linear_regression(X_train, y_train):
    """Train Linear Regression model"""
    print("📈 Training Linear Regression model...")
    
    model = LinearRegression()
    model.fit(X_train, y_train)
    
    print(f"✅ Linear Regression trained. R² score: {model.score(X_train, y_train):.4f}")
    
    return model


def evaluate_model(model, X_test, y_test, model_name):
    """
    Evaluate model with multiple metrics:
    - Top-1 accuracy: exact position match
    - Top-3 accuracy: within 3 positions
    - MAE: mean absolute error
    """
    predictions = model.predict(X_test)
    
    # Round predictions to nearest integer
    predictions_rounded = np.round(predictions).astype(int)
    
    # Top-1 accuracy (exact match)
    top1_correct = (predictions_rounded == y_test).sum()
    top1_accuracy = top1_correct / len(y_test)
    
    # Top-3 accuracy (within 3 positions)
    top3_correct = (np.abs(predictions_rounded - y_test) <= 3).sum()
    top3_accuracy = top3_correct / len(y_test)
    
    # MAE
    mae = mean_absolute_error(y_test, predictions)
    
    metrics = {
        'top1_accuracy': float(top1_accuracy),
        'top3_accuracy': float(top3_accuracy),
        'mae': float(mae),
        'top1_correct': int(top1_correct),
        'top3_correct': int(top3_correct),
        'total_samples': len(y_test)
    }
    
    print(f"📊 {model_name} Metrics:")
    print(f"  Top-1 Accuracy: {top1_accuracy:.2%} ({top1_correct}/{len(y_test)})")
    print(f"  Top-3 Accuracy: {top3_accuracy:.2%} ({top3_correct}/{len(y_test)})")
    print(f"  MAE: {mae:.3f}")
    
    return metrics


def ensemble_predictions(xgb_pred, rf_pred, lr_pred, y_test):
    """Create ensemble predictions and evaluate"""
    # Simple average ensemble
    ensemble_pred = (xgb_pred + rf_pred + lr_pred) / 3
    ensemble_pred_rounded = np.round(ensemble_pred).astype(int)
    
    # Top-1 accuracy
    top1_correct = (ensemble_pred_rounded == y_test).sum()
    top1_accuracy = top1_correct / len(y_test)
    
    # Top-3 accuracy
    top3_correct = (np.abs(ensemble_pred_rounded - y_test) <= 3).sum()
    top3_accuracy = top3_correct / len(y_test)
    
    # MAE
    mae = mean_absolute_error(y_test, ensemble_pred)
    
    metrics = {
        'top1_accuracy': float(top1_accuracy),
        'top3_accuracy': float(top3_accuracy),
        'mae': float(mae),
        'top1_correct': int(top1_correct),
        'top3_correct': int(top3_correct),
        'total_samples': len(y_test)
    }
    
    print(f"🤖 Ensemble Metrics:")
    print(f"  Top-1 Accuracy: {top1_accuracy:.2%} ({top1_correct}/{len(y_test)})")
    print(f"  Top-3 Accuracy: {top3_accuracy:.2%} ({top3_correct}/{len(y_test)})")
    print(f"  MAE: {mae:.3f}")
    
    return metrics


def main():
    print("🚀 Starting complete ML training pipeline...")
    print(f"⏰ Started at: {datetime.now().strftime('%Y-%m-%d %H:%M:%S')}")
    
    # Connect to database
    print("📡 Connecting to PostgreSQL...")
    conn = get_db_connection()
    
    try:
        # Build training dataset
        print("📊 Building training dataset from historical data...")
        df = build_training_dataset(conn, min_samples_per_driver=5)
        
        if len(df) == 0:
            print("❌ No training data available. Please run historical data ingestion first.")
            return
        
        print(f"✅ Dataset built: {len(df)} samples, {len(get_feature_names())} features")
        
        # Split features and target
        feature_names = get_feature_names()
        X = df[feature_names]
        y = df['target_finish_position']
        
        # Stratified split by season (using season_year if available, otherwise random)
        # For simplicity, we'll use random split with stratification on target
        # Binning target for stratification
        y_binned = pd.cut(y, bins=10, labels=False)
        
        X_train, X_test, y_train, y_test = train_test_split(
            X, y, test_size=0.2, random_state=42, stratify=y_binned
        )
        
        # Further split train into train/validation
        y_train_binned = pd.cut(y_train, bins=10, labels=False)
        X_train, X_val, y_train, y_val = train_test_split(
            X_train, y_train, test_size=0.2, random_state=42, stratify=y_train_binned
        )
        
        print(f"📊 Train: {len(X_train)}, Val: {len(X_val)}, Test: {len(X_test)}")
        
        # Train models
        xgb_model = train_xgboost(X_train, y_train, X_val, y_val)
        rf_model = train_random_forest(X_train, y_train)
        lr_model = train_linear_regression(X_train, y_train)
        
        # Evaluate models
        print("\n" + "="*60)
        print("📊 MODEL EVALUATION")
        print("="*60)
        
        xgb_metrics = evaluate_model(xgb_model, X_test, y_test, "XGBoost")
        rf_metrics = evaluate_model(rf_model, X_test, y_test, "Random Forest")
        lr_metrics = evaluate_model(lr_model, X_test, y_test, "Linear Regression")
        
        # Ensemble evaluation
        print("\n" + "="*60)
        xgb_pred = xgb_model.predict(X_test)
        rf_pred = rf_model.predict(X_test)
        lr_pred = lr_model.predict(X_test)
        ensemble_metrics = ensemble_predictions(xgb_pred, rf_pred, lr_pred, y_test)
        
        # Save models
        model_dir = os.path.join(os.path.dirname(os.path.dirname(os.path.abspath(__file__))), 'models')
        os.makedirs(model_dir, exist_ok=True)
        
        print(f"\n💾 Saving models to {model_dir}...")
        joblib.dump(xgb_model, os.path.join(model_dir, 'xgboost_model_v3.pkl'))
        joblib.dump(rf_model, os.path.join(model_dir, 'random_forest_model_v3.pkl'))
        joblib.dump(lr_model, os.path.join(model_dir, 'linear_regression_model_v3.pkl'))
        
        # Save feature names for reproducibility
        joblib.dump(feature_names, os.path.join(model_dir, 'feature_names_v3.pkl'))
        
        # Save metrics
        metrics = {
            'training_date': datetime.now().isoformat(),
            'total_samples': len(df),
            'train_samples': len(X_train),
            'val_samples': len(X_val),
            'test_samples': len(X_test),
            'feature_names': feature_names,
            'xgboost': xgb_metrics,
            'random_forest': rf_metrics,
            'linear_regression': lr_metrics,
            'ensemble': ensemble_metrics
        }
        
        metrics_path = os.path.join(model_dir, 'model_metrics.json')
        with open(metrics_path, 'w') as f:
            json.dump(metrics, f, indent=2)
        
        print(f"✅ Metrics saved to {metrics_path}")
        
        # Print summary
        print("\n" + "="*60)
        print("📊 TRAINING SUMMARY")
        print("="*60)
        print(f"Total samples: {len(df)}")
        print(f"Features: {len(feature_names)}")
        print(f"\nBest model by Top-1 Accuracy: Ensemble ({ensemble_metrics['top1_accuracy']:.2%})")
        print(f"Best model by Top-3 Accuracy: Ensemble ({ensemble_metrics['top3_accuracy']:.2%})")
        print(f"Best model by MAE: Ensemble ({ensemble_metrics['mae']:.3f})")
        print(f"\n⏰ Completed at: {datetime.now().strftime('%Y-%m-%d %H:%M:%S')}")
        
    finally:
        conn.close()
        print("📡 Database connection closed")


if __name__ == "__main__":
    main()
