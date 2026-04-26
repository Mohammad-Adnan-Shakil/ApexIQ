#!/usr/bin/env python3
"""
FastF1 Telemetry Analysis Script
Extracts and compares lap telemetry for two drivers from a specific F1 session.

Usage:
    python3 telemetry_analysis.py <year> <grand_prix> <session_type> <driver1_code> <driver2_code>

Example:
    python3 telemetry_analysis.py 2024 Monaco Q VER LEC
"""

import sys
import json
import numpy as np
import fastf1
from typing import Optional, Dict, List, Any

# Disable output buffering for proper streaming when called from Java
sys.stdout.reconfigure(line_buffering=True)

# Suppress FastF1 logging to stdout
import logging
logging.getLogger('fastf1').setLevel(logging.WARNING)

# Enable FastF1 cache to speed up subsequent runs
import os
cache_dir = os.path.join(os.path.dirname(__file__), 'cache')
os.makedirs(cache_dir, exist_ok=True)
fastf1.Cache.enable_cache(cache_dir)


def get_session(year: int, grand_prix: str, session_type: str):
    """Fetch and load an F1 session from FastF1."""
    try:
        session = fastf1.get_session(year, grand_prix, session_type)
        session.load()
        return session
    except Exception as e:
        return None


def get_driver_telemetry(session, driver_code: str) -> Optional[Dict[str, Any]]:
    """Extract telemetry for a specific driver's fastest lap."""
    try:
        # Get the fastest lap for the driver
        laps = session.laps.pick_driver(driver_code)
        if laps.empty:
            return None
        
        fastest_lap = laps.pick_fastest()
        telemetry = fastest_lap.get_telemetry()
        
        # Format lap time as M:SS.mmm
        lap_time_seconds = fastest_lap['LapTime'].total_seconds()
        minutes = int(lap_time_seconds // 60)
        seconds = lap_time_seconds % 60
        lap_time_str = f"{minutes}:{seconds:06.3f}"
        
        return {
            'driver': driver_code,
            'telemetry': telemetry,
            'lap_time': lap_time_str
        }
    except Exception as e:
        return None


def interpolate_to_distance(tel, max_distance: Optional[float] = None) -> Dict[str, np.ndarray]:
    """
    Interpolate telemetry to uniform distance intervals.
    
    Returns dict with aligned arrays for Speed, Throttle, Brake, Gear, Distance
    """
    distance = tel['Distance'].values
    
    if max_distance is None:
        max_distance = distance[-1]
    
    # Create uniform distance array (1m intervals)
    distance_uniform = np.arange(0, max_distance, 1.0)
    
    # Interpolate each telemetry variable
    speed = np.interp(distance_uniform, distance, tel['Speed'].values)
    throttle = np.interp(distance_uniform, distance, tel['Throttle'].values)
    brake = np.interp(distance_uniform, distance, tel['Brake'].values)
    
    # For gear, use nearest neighbor (not interpolated)
    gear_indices = np.searchsorted(distance, distance_uniform)
    gear_indices = np.clip(gear_indices, 0, len(tel) - 1)
    gear = tel['nGear'].values[gear_indices]
    
    return {
        'distance': distance_uniform,
        'speed': speed,
        'throttle': throttle,
        'brake': brake,
        'gear': gear
    }


def calculate_cumulative_delta(time1_values: List[float], time2_values: List[float]) -> List[float]:
    """
    Calculate cumulative time delta between two drivers at each distance point.
    Delta = driver1_time - driver2_time (positive means driver1 is slower)
    
    Assumes both arrays are aligned to the same distance points.
    """
    # Approximate time from speed: time_delta ≈ distance / avg_speed
    # For simplicity, we calculate cumulative time differences
    
    delta = []
    cumulative_delta = 0.0
    
    for i in range(len(time1_values)):
        if i == 0:
            delta.append(0.0)
        else:
            # Simple approximation: use speed ratio to estimate time difference
            # Assume 1m distance per point
            if time2_values[i] > 0:
                time_for_1m_driver1 = 1.0 / (time1_values[i] / 3.6) if time1_values[i] > 0 else 0
                time_for_1m_driver2 = 1.0 / (time2_values[i] / 3.6) if time2_values[i] > 0 else 0
                point_delta = (time_for_1m_driver1 - time_for_1m_driver2) * 1000  # Convert to milliseconds
            else:
                point_delta = 0.0
            
            cumulative_delta += point_delta
            delta.append(cumulative_delta / 1000)  # Convert back to seconds
    
    return delta


def downsample_to_max_points(data_dict: Dict[str, List[float]], max_points: int = 500) -> Dict[str, List]:
    """Downsample data to maximum number of points using uniform spacing."""
    length = len(data_dict['distance'])
    
    if length <= max_points:
        return {k: v.tolist() if isinstance(v, np.ndarray) else v for k, v in data_dict.items()}
    
    # Calculate step size
    step = length // max_points
    if step < 1:
        step = 1
    
    # Downsample using uniform index slicing
    indices = np.arange(0, length, step)
    if indices[-1] != length - 1:
        indices = np.append(indices, length - 1)
    
    downsampled = {}
    for key, values in data_dict.items():
        if isinstance(values, np.ndarray):
            downsampled[key] = values[indices].tolist()
        else:
            downsampled[key] = [values[i] for i in indices]
    
    return downsampled


def main():
    """Main execution function."""
    # Validate arguments
    if len(sys.argv) != 6:
        error_output = {
            "error": "Invalid arguments. Usage: telemetry_analysis.py <year> <grand_prix> <session_type> <driver1_code> <driver2_code>"
        }
        print(json.dumps(error_output))
        sys.exit(1)
    
    try:
        year = int(sys.argv[1])
        grand_prix = sys.argv[2]
        session_type = sys.argv[3]
        driver1_code = sys.argv[4].upper()
        driver2_code = sys.argv[5].upper()
    except (ValueError, IndexError) as e:
        error_output = {"error": f"Failed to parse arguments: {str(e)}"}
        print(json.dumps(error_output))
        sys.exit(1)
    
    # Fetch session
    session = get_session(year, grand_prix, session_type)
    if session is None:
        error_output = {"error": f"Failed to load session: {year} {grand_prix} {session_type}"}
        print(json.dumps(error_output))
        sys.exit(1)
    
    # Extract telemetry for both drivers
    tel1_data = get_driver_telemetry(session, driver1_code)
    tel2_data = get_driver_telemetry(session, driver2_code)
    
    if tel1_data is None or tel2_data is None:
        missing = driver1_code if tel1_data is None else driver2_code
        error_output = {"error": f"Could not extract telemetry for driver {missing}"}
        print(json.dumps(error_output))
        sys.exit(1)
    
    # Align telemetry on distance
    max_distance = min(
        tel1_data['telemetry']['Distance'].max(),
        tel2_data['telemetry']['Distance'].max()
    )
    
    inter1 = interpolate_to_distance(tel1_data['telemetry'], max_distance)
    inter2 = interpolate_to_distance(tel2_data['telemetry'], max_distance)
    
    # Calculate delta (cumulative time difference)
    delta = calculate_cumulative_delta(inter1['speed'], inter2['speed'])
    
    # Prepare result data
    result_data = {
        'distance': inter1['distance'],
        'driver1_speed': inter1['speed'],
        'driver2_speed': inter2['speed'],
        'driver1_throttle': inter1['throttle'],
        'driver2_throttle': inter2['throttle'],
        'driver1_brake': inter1['brake'],
        'driver2_brake': inter2['brake'],
        'driver1_gear': inter1['gear'],
        'driver2_gear': inter2['gear'],
        'delta': delta
    }
    
    # Downsample to max 500 points
    downsampled = downsample_to_max_points(result_data, max_points=500)
    
    # Build output JSON
    output = {
        "driver1": driver1_code,
        "driver2": driver2_code,
        "distance": downsampled['distance'],
        "driver1_speed": downsampled['driver1_speed'],
        "driver2_speed": downsampled['driver2_speed'],
        "driver1_throttle": downsampled['driver1_throttle'],
        "driver2_throttle": downsampled['driver2_throttle'],
        "driver1_brake": downsampled['driver1_brake'],
        "driver2_brake": downsampled['driver2_brake'],
        "driver1_gear": downsampled['driver1_gear'],
        "driver2_gear": downsampled['driver2_gear'],
        "delta": downsampled['delta'],
        "driver1_lap_time": tel1_data['lap_time'],
        "driver2_lap_time": tel2_data['lap_time']
    }
    
    # Output JSON only (zero other print statements)
    print(json.dumps(output))
    sys.exit(0)


if __name__ == "__main__":
    main()
