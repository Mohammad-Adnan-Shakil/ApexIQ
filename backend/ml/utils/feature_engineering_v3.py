"""
Complete ML Feature Engineering for F1 Predictions
Uses historical data from PostgreSQL database to build rich features
Implements all 12 features as specified in Task 2
"""

import pandas as pd
import numpy as np
from datetime import datetime
import psycopg2
from psycopg2.extras import RealDictCursor


def get_db_connection():
    """Get PostgreSQL connection using environment variables or defaults"""
    import os
    return psycopg2.connect(
        host=os.getenv('DB_HOST', 'localhost'),
        port=os.getenv('DB_PORT', '5432'),
        database=os.getenv('DB_NAME', 'deltabox'),
        user=os.getenv('DB_USER', 'postgres'),
        password=os.getenv('DB_PASSWORD', 'adnanshakil20')
    )


def fetch_driver_statistics(driver_id, race_date, conn):
    """Fetch career statistics for a driver up to a specific race date"""
    cursor = conn.cursor()
    
    # Career stats (all-time)
    cursor.execute("""
        SELECT 
            COUNT(*) as total_races,
            SUM(CASE WHEN finish_position = 1 THEN 1 ELSE 0 END) as total_wins,
            SUM(CASE WHEN finish_position <= 3 THEN 1 ELSE 0 END) as total_podiums,
            SUM(CASE WHEN grid_position = 1 THEN 1 ELSE 0 END) as total_poles,
            AVG(CASE WHEN finish_position IS NOT NULL THEN finish_position ELSE NULL END) as career_avg_finish,
            STDDEV(finish_position) as career_std_dev
        FROM historical_result hr
        JOIN historical_race r ON hr.race_id = r.id
        WHERE hr.driver_id = %s AND r.race_date < %s
          AND hr.finish_position IS NOT NULL
    """, (driver_id, race_date))
    
    career = cursor.fetchone()
    
    # Recent 5 races
    cursor.execute("""
        SELECT AVG(finish_position) as recent_5_avg
        FROM (
            SELECT hr.finish_position
            FROM historical_result hr
            JOIN historical_race r ON hr.race_id = r.id
            WHERE hr.driver_id = %s AND r.race_date < %s
              AND hr.finish_position IS NOT NULL
            ORDER BY r.race_date DESC
            LIMIT 5
        ) as t
    """, (driver_id, race_date))
    
    recent_5 = cursor.fetchone()[0]
    
    # Recent 10 races (for trend calculation)
    cursor.execute("""
        SELECT finish_position
        FROM (
            SELECT hr.finish_position
            FROM historical_result hr
            JOIN historical_race r ON hr.race_id = r.id
            WHERE hr.driver_id = %s AND r.race_date < %s
              AND hr.finish_position IS NOT NULL
            ORDER BY r.race_date DESC
            LIMIT 10
        ) as t
        ORDER BY finish_position ASC
    """, (driver_id, race_date))
    
    recent_10_positions = [row[0] for row in cursor.fetchall()]
    
    # Grid to finish delta (average difference)
    cursor.execute("""
        SELECT AVG(finish_position - grid_position) as avg_grid_to_finish
        FROM historical_result hr
        JOIN historical_race r ON hr.race_id = r.id
        WHERE hr.driver_id = %s AND r.race_date < %s
          AND hr.finish_position IS NOT NULL
          AND hr.grid_position IS NOT NULL
    """, (driver_id, race_date))
    
    grid_to_finish = cursor.fetchone()[0]
    
    cursor.close()
    
    # Calculate recent trend (slope of last 10 finishes)
    recent_trend = 0
    if len(recent_10_positions) >= 3:
        # Simple linear regression to get trend
        x = np.arange(len(recent_10_positions))
        y = np.array(recent_10_positions)
        slope = np.polyfit(x, y, 1)[0]
        recent_trend = slope  # Negative = improving, Positive = declining
    
    return {
        'total_races': career[0] or 0,
        'total_wins': career[1] or 0,
        'total_poles': career[3] or 0,
        'career_avg_finish': float(career[4] or 0),
        'consistency_score': float(career[5] or 0),  # Lower = more consistent
        'recent_5_avg': float(recent_5 or 0),
        'recent_trend': float(recent_trend),
        'grid_to_finish_delta': float(grid_to_finish or 0)
    }


def fetch_circuit_statistics(driver_id, circuit_name, race_date, conn):
    """Fetch driver's historical performance at a specific circuit"""
    cursor = conn.cursor()
    
    cursor.execute("""
        SELECT 
            COUNT(*) as races_at_circuit,
            AVG(finish_position) as circuit_avg_finish,
            SUM(CASE WHEN finish_position = 1 THEN 1 ELSE 0 END) as wins_at_circuit,
            SUM(CASE WHEN grid_position = 1 THEN 1 ELSE 0 END) as poles_at_circuit
        FROM historical_result hr
        JOIN historical_race r ON hr.race_id = r.id
        WHERE hr.driver_id = %s AND r.circuit_name = %s AND r.race_date < %s
          AND hr.finish_position IS NOT NULL
    """, (driver_id, circuit_name, race_date))
    
    result = cursor.fetchone()
    cursor.close()
    
    return {
        'circuit_races': result[0] or 0,
        'circuit_avg_finish': float(result[1] or 0),
        'circuit_wins': result[2] or 0,
        'circuit_poles': result[3] or 0
    }


def fetch_constructor_circuit_stats(constructor_id, circuit_name, race_date, conn):
    """Fetch constructor's historical performance at a specific circuit"""
    cursor = conn.cursor()
    
    cursor.execute("""
        SELECT 
            COUNT(*) as races_at_circuit,
            AVG(finish_position) as circuit_avg_finish,
            SUM(CASE WHEN finish_position = 1 THEN 1 ELSE 0 END) as wins_at_circuit
        FROM historical_result hr
        JOIN historical_race r ON hr.race_id = r.id
        WHERE hr.constructor_id = %s AND r.circuit_name = %s AND r.race_date < %s
          AND hr.finish_position IS NOT NULL
    """, (constructor_id, circuit_name, race_date))
    
    result = cursor.fetchone()
    cursor.close()
    
    return {
        'team_circuit_races': result[0] or 0,
        'team_circuit_avg_finish': float(result[1] or 0),
        'team_circuit_wins': result[2] or 0
    }


def fetch_season_statistics(driver_id, season_year, race_date, conn):
    """Fetch driver's current season performance including championship position"""
    cursor = conn.cursor()
    
    cursor.execute("""
        SELECT 
            COUNT(*) as season_races,
            AVG(finish_position) as season_avg_finish,
            SUM(CASE WHEN finish_position = 1 THEN 1 ELSE 0 END) as season_wins
        FROM historical_result hr
        JOIN historical_race r ON hr.race_id = r.id
        WHERE hr.driver_id = %s AND r.season_year = %s AND r.race_date < %s
          AND hr.finish_position IS NOT NULL
    """, (driver_id, season_year, race_date))
    
    result = cursor.fetchone()
    
    # Get championship position from standings table
    cursor.execute("""
        SELECT position
        FROM historical_driver_standings
        WHERE driver_id = %s AND year = %s
    """, (driver_id, season_year))
    
    champ_result = cursor.fetchone()
    championship_position = champ_result[0] if champ_result else 0
    
    cursor.close()
    
    return {
        'season_races': result[0] or 0,
        'season_avg_finish': float(result[1] or 0),
        'season_wins': result[2] or 0,
        'championship_position': championship_position
    }


def build_feature_vector(result_row, driver_stats, circuit_stats, constructor_stats, season_stats):
    """
    Build complete 12-feature vector for ML model
    
    Features:
    1. career_avg_finish
    2. career_wins
    3. career_poles
    4. recent_form_5 (avg finish last 5 races)
    5. circuit_affinity (avg finish at this specific circuit)
    6. team_performance (constructor avg finish at circuit)
    7. experience (total career race starts)
    8. championship_position (current season standing)
    9. grid_position
    10. consistency_score (std dev of finish positions)
    11. recent_trend (slope of last 10 finishes)
    12. grid_to_finish_delta (avg difference between grid and finish)
    """
    
    features = {
        'career_avg_finish': driver_stats.get('career_avg_finish', 20),
        'career_wins': driver_stats.get('total_wins', 0),
        'career_poles': driver_stats.get('total_poles', 0),
        'recent_form_5': driver_stats.get('recent_5_avg', 20),
        'circuit_affinity': circuit_stats.get('circuit_avg_finish', 20),
        'team_performance': constructor_stats.get('team_circuit_avg_finish', 20),
        'experience': driver_stats.get('total_races', 0),
        'championship_position': season_stats.get('championship_position', 0),
        'grid_position': int(result_row['grid_position'] or 20),
        'consistency_score': driver_stats.get('consistency_score', 10),
        'recent_trend': driver_stats.get('recent_trend', 0),
        'grid_to_finish_delta': driver_stats.get('grid_to_finish_delta', 0)
    }
    
    return features


def build_training_dataset(conn, min_samples_per_driver=5):
    """
    Build complete training dataset from historical_results
    Returns DataFrame ready for model training
    
    Only includes results where:
    - finish_position is not null
    - status = 'Finished'
    - Driver has at least min_samples_per_driver races (for reliable stats)
    """
    
    cursor = conn.cursor()
    
    # First, identify drivers with enough races
    cursor.execute("""
        SELECT driver_id, COUNT(*) as race_count
        FROM historical_result hr
        JOIN historical_race r ON hr.race_id = r.id
        WHERE hr.finish_position IS NOT NULL
        AND hr.status = 'Finished'
        GROUP BY driver_id
        HAVING COUNT(*) >= %s
    """, (min_samples_per_driver,))
    
    eligible_drivers = {row[0]: row[1] for row in cursor.fetchall()}
    print(f"📊 Found {len(eligible_drivers)} drivers with >= {min_samples_per_driver} races")
    
    # Fetch all completed race results for eligible drivers
    cursor.execute("""
        SELECT 
            hr.id,
            hr.driver_id,
            hr.constructor_id,
            hr.grid_position,
            hr.finish_position,
            r.season_year,
            r.circuit_name,
            r.race_date
        FROM historical_result hr
        JOIN historical_race r ON hr.race_id = r.id
        WHERE hr.finish_position IS NOT NULL
        AND hr.status = 'Finished'
        AND r.race_date < NOW()
        AND hr.driver_id = ANY(%s)
        ORDER BY r.race_date
    """, (list(eligible_drivers.keys()),))
    
    rows = cursor.fetchall()
    cursor.close()
    
    features_list = []
    targets = []
    
    print(f"📊 Building training dataset from {len(rows)} completed race results...")
    
    for i, row in enumerate(rows):
        if i % 500 == 0:
            print(f"  Processing {i}/{len(rows)} ({i/len(rows)*100:.1f}%)...")
        
        result_id, driver_id, constructor_id, grid_pos, finish_pos, season_year, circuit_name, race_date = row
        
        try:
            # Get driver stats up to this race
            driver_stats = fetch_driver_statistics(driver_id, race_date, conn)
            circuit_stats = fetch_circuit_statistics(driver_id, circuit_name, race_date, conn)
            constructor_stats = fetch_constructor_circuit_stats(constructor_id, circuit_name, race_date, conn) if constructor_id else {}
            season_stats = fetch_season_statistics(driver_id, season_year, race_date, conn)
            
            # Build features
            features = build_feature_vector(
                {'grid_position': grid_pos},
                driver_stats,
                circuit_stats,
                constructor_stats,
                season_stats
            )
            
            features_list.append(features)
            targets.append(finish_pos)
            
        except Exception as e:
            print(f"  ⚠️  Error processing race {result_id}: {e}")
            continue
    
    print(f"✅ Built {len(features_list)} training samples")
    
    df = pd.DataFrame(features_list)
    df['target_finish_position'] = targets
    
    return df


def get_feature_names():
    """Returns list of feature names in order"""
    return [
        'career_avg_finish',
        'career_wins',
        'career_poles',
        'recent_form_5',
        'circuit_affinity',
        'team_performance',
        'experience',
        'championship_position',
        'grid_position',
        'consistency_score',
        'recent_trend',
        'grid_to_finish_delta'
    ]


if __name__ == "__main__":
    # Test the feature engineering
    print("🔧 Testing feature engineering pipeline...")
    
    conn = get_db_connection()
    try:
        df = build_training_dataset(conn)
        print(f"\n📊 Dataset shape: {df.shape}")
        print(f"\n📋 Feature names: {get_feature_names()}")
        print(f"\n📈 Sample features:\n{df[get_feature_names()].head()}")
        print(f"\n🎯 Sample targets:\n{df['target_finish_position'].head()}")
    finally:
        conn.close()
