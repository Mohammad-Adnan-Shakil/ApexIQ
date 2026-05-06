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
import warnings
from urllib.parse import unquote
import logging

# Configure logging for this script
logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(levelname)s - %(message)s'
)
logger = logging.getLogger(__name__)

# Disable all warnings
warnings.filterwarnings('ignore')

# Disable output buffering for proper streaming when called from Java
sys.stdout.reconfigure(line_buffering=True)

# Suppress FastF1 logging to stdout
logging.getLogger('fastf1').setLevel(logging.WARNING)

# Enable FastF1 cache to speed up subsequent runs
import os
cache_dir = os.path.join(os.path.dirname(__file__), 'cache')
os.makedirs(cache_dir, exist_ok=True)
fastf1.Cache.enable_cache(cache_dir)


def map_session_type(session_type_input: str) -> str:
    """
    Map various session type formats to FastF1-compatible format.
    
    FastF1 accepts:
    - "Qualifying", "Q" -> "Qualifying"
    - "Race", "R" -> "Race"
    - "Free Practice 1", "FP1", "Practice 1", "P1" -> "FP1"
    - "Free Practice 2", "FP2", "Practice 2", "P2" -> "FP2"
    - "Free Practice 3", "FP3", "Practice 3", "P3" -> "FP3"
    - "Sprint", "S" -> "Sprint"
    """
    mapping = {
        # Qualifying variants
        "qualifying": "Qualifying",
        "q": "Qualifying",
        # Race variants
        "race": "Race",
        "r": "Race",
        # Free Practice 1 variants
        "free practice 1": "FP1",
        "fp1": "FP1",
        "practice 1": "FP1",
        "p1": "FP1",
        # Free Practice 2 variants
        "free practice 2": "FP2",
        "fp2": "FP2",
        "practice 2": "FP2",
        "p2": "FP2",
        # Free Practice 3 variants
        "free practice 3": "FP3",
        "fp3": "FP3",
        "practice 3": "FP3",
        "p3": "FP3",
        # Sprint variants
        "sprint": "Sprint",
        "s": "Sprint",
    }
    
    normalized = session_type_input.lower().strip()
    mapped = mapping.get(normalized, session_type_input)
    
    logger.info(f"🔄 Session type mapping: '{session_type_input}' -> '{mapped}'")
    return mapped


def get_session_with_fallback(year: int, grand_prix: str, session_type: str, fallback_year: Optional[int] = None):
    """
    Attempt to load a FastF1 session, with fallback to another year if needed.
    
    Returns: Tuple (session, actual_year_used, fallback_applied)
    """
    try:
        logger.info(f"  📤 Calling fastf1.get_session({year}, '{grand_prix}', '{session_type}')")
        session = fastf1.get_session(year, grand_prix, session_type)
        logger.info(f"  ✅ Session object created")
        
        logger.info(f"  📥 Loading session data (may take 20-30 seconds for first load)...")
        session.load()
        logger.info(f"  ✅ Session loaded successfully from year {year}")
        
        return session, year, False
        
    except Exception as e:
        error_msg = str(e).lower()
        
        # Check if error is due to unsupported year or session
        if any(keyword in error_msg for keyword in ['not found', 'invalid event', 'no data', 'unavailable', 'does not exist']):
            logger.warning(f"  ⚠️  Year {year} not supported by FastF1: {str(e)}")
            
            if fallback_year and fallback_year != year:
                logger.info(f"  🔄 Attempting fallback to year {fallback_year}...")
                try:
                    logger.info(f"  📤 Calling fastf1.get_session({fallback_year}, '{grand_prix}', '{session_type}')")
                    session = fastf1.get_session(fallback_year, grand_prix, session_type)
                    logger.info(f"  ✅ Session object created (fallback)")
                    
                    logger.info(f"  📥 Loading session data (fallback)...")
                    session.load()
                    logger.info(f"  ✅ Session loaded successfully from fallback year {fallback_year}")
                    
                    return session, fallback_year, True
                except Exception as fallback_e:
                    logger.error(f"  ❌ Fallback also failed: {str(fallback_e)}")
                    return None, None, False
            else:
                logger.error(f"  ❌ No fallback available, returning None")
                return None, None, False
        else:
            # Other error (network, cache, etc.)
            logger.error(f"  ❌ Unexpected error loading session: {str(e)}")
            return None, None, False


def get_session(year: int, grand_prix: str, session_type: str):
    """Fetch and load an F1 session from FastF1, with automatic fallback for future years."""
    try:
        # Map session type to FastF1 format
        mapped_session = map_session_type(session_type)
        
        # Decode URL-encoded values as safety measure
        grand_prix_decoded = unquote(grand_prix)
        
        logger.info(f"🚀 FastF1 Session Loading:")
        logger.info(f"  Requested: year={year}, grand_prix='{grand_prix_decoded}', session='{mapped_session}'")
        
        # Determine fallback year for future years (FastF1 may not have 2026+ data yet)
        fallback_year = None
        if year > 2024:  # If year is in the future, fallback to latest available
            fallback_year = 2024
            logger.info(f"  📋 Year {year} is in the future, will fallback to {fallback_year} if needed")
        
        # Attempt to load with fallback
        session, actual_year, fallback_applied = get_session_with_fallback(
            year, grand_prix_decoded, mapped_session, fallback_year
        )
        
        if session is None:
            logger.error(f"❌ Failed to load session: {year} {grand_prix_decoded} {mapped_session}")
            return None
        
        if fallback_applied:
            logger.warning(f"⚠️  Loaded from fallback year {actual_year} instead of requested {year}")
        
        return session
        
    except Exception as e:
        logger.error(f"❌ Unexpected error in get_session: {str(e)}", exc_info=True)
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


def analyze(year: int, grand_prix: str, session_type: str, driver1: str, driver2: str) -> Dict[str, Any]:
    """
    Analyze telemetry for two drivers from a specific F1 session.
    
    Args:
        year: F1 season year
        grand_prix: Grand prix name (e.g., "Monaco")
        session_type: Session type (e.g., "Q", "R")
        driver1: First driver code (e.g., "VER")
        driver2: Second driver code (e.g., "LEC")
    
    Returns:
        Dictionary with telemetry data for both drivers
    """
    # Normalize and decode all inputs
    grand_prix_decoded = unquote(grand_prix) if grand_prix else grand_prix
    session_type_decoded = unquote(session_type) if session_type else session_type
    driver1_decoded = unquote(driver1).upper() if driver1 else driver1
    driver2_decoded = unquote(driver2).upper() if driver2 else driver2
    
    # 🚀 LOG ANALYZE FUNCTION ENTRY
    logger.info(f"🚀 TELEMETRY ANALYZE: Received request")
    logger.info(f"  📋 Input (raw): year={year}, grand_prix='{grand_prix}', session_type='{session_type}', driver1='{driver1}', driver2='{driver2}'")
    logger.info(f"  ✅ Input (decoded): year={year}, grand_prix='{grand_prix_decoded}', session_type='{session_type_decoded}', driver1='{driver1_decoded}', driver2='{driver2_decoded}'")
    
    # Fetch session
    logger.info(f"🔗 Attempting to load FastF1 session...")
    session = get_session(year, grand_prix_decoded, session_type_decoded)
    
    if session is None:
        # Provide user-friendly error message
        if year > 2024:
            error_msg = f"Telemetry for year {year} is not yet available. FastF1 currently supports up to 2024. Please select a year from 2018-2024."
        else:
            error_msg = f"Could not load telemetry: {grand_prix_decoded} {session_type_decoded} in {year}. The session may not exist in FastF1 database."
        logger.error(f"❌ {error_msg}")
        return {"error": error_msg}
    
    logger.info(f"📊 Session loaded successfully - extracting telemetry for drivers {driver1_decoded} and {driver2_decoded}")
    
    # Extract telemetry for both drivers
    tel1_data = get_driver_telemetry(session, driver1_decoded)
    tel2_data = get_driver_telemetry(session, driver2_decoded)
    
    if tel1_data is None or tel2_data is None:
        missing = driver1_decoded if tel1_data is None else driver2_decoded
        error_msg = f"Could not extract telemetry for driver {missing}"
        logger.error(f"❌ {error_msg}")
        return {"error": error_msg}
    
    logger.info(f"✅ Telemetry extracted - driver1: {tel1_data['lap_time']}, driver2: {tel2_data['lap_time']}")
    
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
    
    logger.info(f"📊 Downsampled telemetry to {len(downsampled['distance'])} points")
    
    # Build output JSON
    output = {
        "driver1": driver1_decoded,
        "driver2": driver2_decoded,
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
    
    logger.info(f"✅ TELEMETRY ANALYSIS COMPLETE: Returning {len(output['distance'])} data points for {driver1_decoded} vs {driver2_decoded}")
    
    return output


def main():
    """Main execution function for CLI usage."""
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
    
    result = analyze(year, grand_prix, session_type, driver1_code, driver2_code)
    
    if "error" in result:
        print(json.dumps(result))
        sys.exit(1)
    
    print(json.dumps(result))
    sys.exit(0)


if __name__ == "__main__":
    main()
