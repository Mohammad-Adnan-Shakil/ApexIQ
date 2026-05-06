#!/usr/bin/env python3
"""
OpenF1 Telemetry Data Fetcher
Replaces FastF1 with OpenF1 API for real-time telemetry data.

OpenF1 provides:
- Live telemetry data for current seasons
- Driver telemetry: Speed, Throttle, Brake, Gear, DRS, RPM
- Lap times and comparisons
- Current season and recent races
- Real-time updates

API: https://api.openf1.org
"""

import sys
import json
import logging
import requests
import numpy as np
import re
from typing import Optional, Dict, List, Any
from urllib.parse import unquote
import warnings

# Configure logging
logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(levelname)s - %(message)s'
)
logger = logging.getLogger(__name__)

# Disable output buffering for proper streaming
sys.stdout.reconfigure(line_buffering=True)

# Suppress warnings
warnings.filterwarnings('ignore')

# OpenF1 API configuration
OPENF1_API_BASE = "https://api.openf1.org/v1"
CURRENT_SEASON = 2024  # Will be updated dynamically

# Session type mapping: shorthand -> OpenF1 full name
SESSION_TYPE_MAP = {
    'R': 'Race',
    'Q': 'Qualifying',
    'FP1': 'Practice 1',
    'FP2': 'Practice 2',
    'FP3': 'Practice 3',
    'S': 'Sprint',
    'RACE': 'Race',
    'QUALIFYING': 'Qualifying',
    'PRACTICE 1': 'Practice 1',
    'PRACTICE 2': 'Practice 2',
    'PRACTICE 3': 'Practice 3',
    'SPRINT': 'Sprint',
}


def get_current_season() -> int:
    """Get the current F1 season from OpenF1 API."""
    try:
        logger.info("📋 Fetching current season from OpenF1...")
        response = requests.get(f"{OPENF1_API_BASE}/drivers", timeout=5)
        response.raise_for_status()
        data = response.json()
        
        if data:
            # Get year from first driver's data
            current_year = max([d.get('session_key', 0) // 1000000 for d in data if 'session_key' in d])
            if current_year > 0:
                logger.info(f"✅ Current season detected: {current_year}")
                return current_year
        
        return 2024  # Default fallback
    except Exception as e:
        logger.warning(f"⚠️  Could not fetch current season: {str(e)}, using 2024")
        return 2024


def get_races(year: int) -> List[Dict[str, Any]]:
    """Fetch all races for a given season (via meetings)."""
    try:
        logger.info(f"🔍 Fetching meetings for {year}...")
        return get_meetings(year)
    except Exception as e:
        logger.error(f"❌ Failed to fetch races for {year}: {str(e)}")
        return []


def get_meetings(year: int) -> List[Dict[str, Any]]:
    """Get all meetings (race events) for a given year from OpenF1."""
    try:
        logger.info(f"🔍 Fetching meetings for {year}...")
        response = requests.get(f"{OPENF1_API_BASE}/meetings", params={"year": year}, timeout=10)
        response.raise_for_status()
        meetings = response.json()
        
        logger.info(f"✅ Found {len(meetings)} meetings for {year}")
        return meetings
    except Exception as e:
        logger.error(f"❌ Failed to fetch meetings for {year}: {str(e)}")
        return []


def get_sessions(meeting_key: int) -> List[Dict[str, Any]]:
    """Get all sessions for a specific meeting."""
    try:
        response = requests.get(f"{OPENF1_API_BASE}/sessions", params={"meeting_key": meeting_key}, timeout=10)
        response.raise_for_status()
        sessions = response.json()
        
        return sessions
    except Exception as e:
        logger.error(f"❌ Failed to fetch sessions for meeting {meeting_key}: {str(e)}")
        return []


def normalize_race_name(name: str) -> str:
    """
    Normalize race name for fuzzy matching.
    
    Normalization rules:
    - Lowercase
    - Remove "formula 1"
    - Remove "grand prix" 
    - Remove punctuation
    - Trim spaces
    """
    if not name:
        return ""
    
    # Lowercase
    normalized = name.lower()
    
    # Remove "formula 1"
    normalized = re.sub(r'\bformula\s+1\b', '', normalized)
    
    # Remove "grand prix"
    normalized = re.sub(r'\bgrand\s+prix\b', '', normalized)
    
    # Remove punctuation
    normalized = re.sub(r'[.,!?;:\'"()-]', '', normalized)
    
    # Trim and collapse multiple spaces
    normalized = re.sub(r'\s+', ' ', normalized).strip()
    
    return normalized


def find_race(year: int, grand_prix: str) -> Optional[Dict[str, Any]]:
    """Find a specific race/meeting by year and grand prix name with fuzzy matching."""
    try:
        logger.info(f"🏁 User requested race: {grand_prix} {year}")
        meetings = get_meetings(year)
        
        if not meetings:
            logger.error(f"❌ No meetings found for {year}")
            return None
        
        logger.info(f"📋 Available meetings: {[m.get('meeting_name') for m in meetings]}")
        
        # Normalize user input
        query_normalized = normalize_race_name(grand_prix)
        logger.info(f"🔍 Normalized query: '{grand_prix}' → '{query_normalized}'")
        
        # Try exact match first
        for meeting in meetings:
            meeting_name = meeting.get('meeting_name', '')
            if normalize_race_name(meeting_name) == query_normalized:
                logger.info(f"✅ Exact match: {meeting_name}")
                return meeting
        
        # Try contains match
        for meeting in meetings:
            meeting_name = meeting.get('meeting_name', '')
            location = meeting.get('location', '')
            country = meeting.get('country_name', '')
            
            meeting_name_norm = normalize_race_name(meeting_name)
            location_norm = normalize_race_name(location)
            country_norm = normalize_race_name(country)
            
            if (query_normalized in meeting_name_norm or
                query_normalized in location_norm or
                query_normalized in country_norm or
                meeting_name_norm.startswith(query_normalized) or
                location_norm.startswith(query_normalized)):
                logger.info(f"✅ Contains match: {meeting_name}")
                return meeting
        
        # Try keyword matching (any meaningful word matches)
        query_keywords = set(query_normalized.split())
        for meeting in meetings:
            meeting_name = meeting.get('meeting_name', '')
            location = meeting.get('location', '')
            
            meeting_text_norm = normalize_race_name(f"{meeting_name} {location}")
            meeting_keywords = set(meeting_text_norm.split())
            
            # If any keyword matches
            if query_keywords & meeting_keywords:
                logger.info(f"✅ Keyword match: {meeting_name}")
                return meeting
        
        logger.error(f"❌ Race not found: {grand_prix} {year}")
        logger.info(f"   Try: {', '.join([m.get('meeting_name') for m in meetings[:5]])}")
        return None
    except Exception as e:
        logger.error(f"❌ Error finding race: {str(e)}")
        return None


def find_session(meeting_key: int, session_type: str) -> Optional[Dict[str, Any]]:
    """Find a specific session by meeting key and session type."""
    try:
        sessions = get_sessions(meeting_key)
        
        if not sessions:
            logger.warning(f"⚠️  No sessions found for meeting {meeting_key}")
            return None
        
        # Normalize requested session type
        session_type_upper = session_type.strip().upper()
        requested_session = SESSION_TYPE_MAP.get(session_type_upper, session_type_upper)
        
        logger.info(f"  🔍 Looking for session type: '{requested_session}' (input: '{session_type}')")
        
        for session in sessions:
            session_name = session.get('session_name', '').strip()
            
            # Debug: Log all available sessions
            if session == sessions[0]:
                logger.info(f"  📋 Available sessions: {[s.get('session_name') for s in sessions]}")
            
            # Exact match on session name
            if session_name == requested_session:
                logger.info(f"  ⏱ Found session: '{session_name}' (key: {session.get('session_key')})")
                return session
        
        logger.warning(f"⚠️  Session '{requested_session}' not found. Available: {[s.get('session_name') for s in sessions]}")
        return None
    except Exception as e:
        logger.error(f"❌ Error finding session: {str(e)}")
        return None


def get_drivers_for_session(session_key: int) -> List[Dict[str, Any]]:
    """Get all drivers that participated in a session."""
    try:
        response = requests.get(f"{OPENF1_API_BASE}/drivers", params={"session_key": session_key}, timeout=10)
        response.raise_for_status()
        drivers = response.json()
        
        logger.info(f"📊 Found {len(drivers)} drivers for session {session_key}")
        return drivers
    except Exception as e:
        logger.error(f"❌ Failed to fetch drivers: {str(e)}")
        return []


def get_driver_telemetry(session_key: int, driver_number: int) -> Optional[List[Dict[str, Any]]]:
    """
    Fetch telemetry-like data for a specific driver in a session.
    
    OpenF1 does NOT have a dedicated telemetry endpoint.
    Instead, we use:
    1. Laps data (lap times, sectors, speeds)
    2. Drivers data for session participation
    
    Returns list of laps with timing and speed data for each driver.
    """
    try:
        logger.info(f"  📥 Fetching lap data for driver {driver_number} in session {session_key}...")
        
        response = requests.get(
            f"{OPENF1_API_BASE}/laps",
            params={
                "session_key": session_key,
                "driver_number": driver_number
            },
            timeout=15
        )
        response.raise_for_status()
        laps = response.json()
        
        # Check if response is error dict ({"detail": "No results found."})
        if isinstance(laps, dict) and 'detail' in laps:
            logger.warning(f"  ⚠️  No laps available for driver {driver_number}: {laps.get('detail')}")
            return None
        
        if not laps or not isinstance(laps, list):
            logger.warning(f"  ⚠️  No lap data available for driver {driver_number}")
            return None
        
        logger.info(f"  ✅ Retrieved {len(laps)} laps for driver {driver_number}")
        return laps
    except Exception as e:
        logger.error(f"  ❌ Failed to fetch lap data: {str(e)}")
        return None


def get_lap_times(session_key: int, driver_number: int) -> Optional[List[Dict[str, Any]]]:
    """Fetch lap times for a driver."""
    try:
        response = requests.get(
            f"{OPENF1_API_BASE}/laps",
            params={
                "session_key": session_key,
                "driver_number": driver_number
            },
            timeout=10
        )
        response.raise_for_status()
        laps = response.json()
        
        if laps:
            logger.info(f"  ✅ Retrieved {len(laps)} laps for driver {driver_number}")
            return laps
        
        return None
    except Exception as e:
        logger.warning(f"  ⚠️  Could not fetch laps: {str(e)}")
        return None


def process_lap_data(lap_data: List[Dict[str, Any]]) -> Dict[str, np.ndarray]:
    """
    Process raw OpenF1 lap data into telemetry-like arrays.
    
    OpenF1 provides lap-aggregated data with:
    - Speed measurements at specific points: i1_speed, i2_speed, st_speed
    - Sector times: duration_sector_1/2/3
    - Segment times: segments_sector_1/2/3
    - Lap timing: lap_duration, lap_number
    
    We reconstruct synthetic telemetry by:
    1. Creating a distance-based progression through each lap
    2. Interpolating speeds from measurement points
    3. Inferring throttle/brake from sector times
    4. Normalizing gear selection (F1 uses ~7 gears)
    
    Returns: {
        'speed': ndarray,
        'throttle': ndarray,
        'brake': ndarray,
        'gear': ndarray,
        'distance': ndarray,
        'lap_numbers': list,
    }
    """
    if not lap_data:
        return None
    
    try:
        # Aggregate lap data into a synthetic telemetry timeline
        all_distances = []
        all_speeds = []
        all_throttles = []
        all_brakes = []
        all_gears = []
        lap_numbers = []
        
        cumulative_distance = 0.0
        lap_circuit_distance = 4361  # Montreal circuit approx 4.361km (typical F1 lap)
        
        for lap in lap_data:
            lap_num = lap.get('lap_number', 0)
            
            # Speed measurements at key points (km/h)
            i1_speed = lap.get('i1_speed') or 0
            i2_speed = lap.get('i2_speed') or 0
            st_speed = lap.get('st_speed') or 0  # Start/finish line speed
            
            # Ensure speeds are numeric
            try:
                i1_speed = float(i1_speed) if i1_speed else 0
                i2_speed = float(i2_speed) if i2_speed else 0
                st_speed = float(st_speed) if st_speed else 0
            except (ValueError, TypeError):
                i1_speed = i2_speed = st_speed = 0
            
            # Sector times (seconds)
            dur_s1 = lap.get('duration_sector_1') or 0
            dur_s2 = lap.get('duration_sector_2') or 0
            dur_s3 = lap.get('duration_sector_3') or 0
            
            # Segment data - can be list or integer
            segs_s1_data = lap.get('segments_sector_1', [])
            segs_s2_data = lap.get('segments_sector_2', [])
            segs_s3_data = lap.get('segments_sector_3', [])
            
            # Count segments properly
            segs_s1 = len(segs_s1_data) if isinstance(segs_s1_data, list) else (segs_s1_data if isinstance(segs_s1_data, int) else 6)
            segs_s2 = len(segs_s2_data) if isinstance(segs_s2_data, list) else (segs_s2_data if isinstance(segs_s2_data, int) else 6)
            segs_s3 = len(segs_s3_data) if isinstance(segs_s3_data, list) else (segs_s3_data if isinstance(segs_s3_data, int) else 7)
            
            segs_s1 = max(1, segs_s1)
            segs_s2 = max(1, segs_s2)
            segs_s3 = max(1, segs_s3)
            
            total_segments = segs_s1 + segs_s2 + segs_s3
            
            # Calculate distance per segment
            dist_per_segment = lap_circuit_distance / max(total_segments, 1)
            
            # Create data points for each sector in this lap
            for sector_num, (duration, segment_count_sector) in enumerate([
                (dur_s1, segs_s1),
                (dur_s2, segs_s2),
                (dur_s3, segs_s3)
            ]):
                # Get speed for this sector
                if sector_num == 0:
                    # Sector 1: transition from st_speed (start/finish) through i1 (1st intersection)
                    speed_start = st_speed if st_speed > 0 else 220
                    speed_end = i1_speed if i1_speed > 0 else 240
                elif sector_num == 1:
                    # Sector 2: from i1 to i2
                    speed_start = i1_speed if i1_speed > 0 else 240
                    speed_end = i2_speed if i2_speed > 0 else 260
                else:
                    # Sector 3: from i2 back to st (finish line)
                    speed_start = i2_speed if i2_speed > 0 else 260
                    speed_end = st_speed if st_speed > 0 else 220
                
                # Interpolate speeds through sector
                sector_speeds = np.linspace(speed_start, speed_end, max(segment_count_sector, 1))
                
                # Create points for each segment
                for seg_idx in range(max(segment_count_sector, 1)):
                    all_distances.append(cumulative_distance)
                    speed_val = sector_speeds[seg_idx]
                    all_speeds.append(speed_val)
                    
                    # Estimate throttle based on speed (higher speed = more throttle)
                    throttle = min(100, (speed_val / 320) * 100)  # Max F1 speed ~330km/h
                    all_throttles.append(throttle)
                    
                    # Brake is inversely related to throttle, with more brake in later sectors
                    base_brake = max(0, 100 - throttle)
                    if sector_num == 2:  # More braking in sector 3 (approaching corners)
                        brake = base_brake * 0.8
                    elif sector_num == 1:
                        brake = base_brake * 0.5
                    else:
                        brake = base_brake * 0.3
                    all_brakes.append(brake)
                    
                    # Gear selection (F1 7-speed transmission, ~45 km/h per gear at optimal)
                    gear = max(1, min(7, int(speed_val / 45)))
                    all_gears.append(gear)
                    
                    lap_numbers.append(lap_num)
                    cumulative_distance += dist_per_segment
        
        if not all_distances:
            logger.warning(f"⚠️  No valid distance data extracted from {len(lap_data)} laps")
            return None
        
        logger.info(f"  ✅ Processed {len(lap_data)} laps into {len(all_distances)} telemetry points")
        
        return {
            'speed': np.array(all_speeds, dtype=float),
            'throttle': np.array(all_throttles, dtype=float),
            'brake': np.array(all_brakes, dtype=float),
            'gear': np.array(all_gears, dtype=int),
            'distance': np.array(all_distances, dtype=float),
            'lap_numbers': lap_numbers,
        }
    except Exception as e:
        logger.error(f"❌ Error processing lap data: {str(e)}", exc_info=True)
        return None
        
        logger.info(f"  ✅ Processed {len(lap_data)} laps into {len(all_distances)} telemetry points")
        
        return {
            'speed': np.array(all_speeds, dtype=float),
            'throttle': np.array(all_throttles, dtype=float),
            'brake': np.array(all_brakes, dtype=float),
            'gear': np.array(all_gears, dtype=int),
            'distance': np.array(all_distances, dtype=float),
            'lap_numbers': lap_numbers,
        }
    except Exception as e:
        logger.error(f"❌ Error processing lap data: {str(e)}")
        return None


def align_telemetry_data(tel1: Dict[str, np.ndarray], tel2: Dict[str, np.ndarray]) -> tuple:
    """
    Align two telemetry datasets by normalizing to lap-based segments.
    
    Since we're working with synthetic telemetry from lap data,
    we align both drivers' data to the same number of points over the same distance.
    """
    try:
        # Find common distance range
        max_dist_1 = tel1['distance'][-1] if len(tel1['distance']) > 0 else 0
        max_dist_2 = tel2['distance'][-1] if len(tel2['distance']) > 0 else 0
        
        max_distance = min(max_dist_1, max_dist_2)
        min_distance = max(tel1['distance'][0] if len(tel1['distance']) > 0 else 0,
                          tel2['distance'][0] if len(tel2['distance']) > 0 else 0)
        
        if max_distance <= min_distance:
            logger.warning(f"⚠️  Cannot align: no overlapping distance range")
            return None, None
        
        # Create uniform distance array (use same resolution as original data)
        common_points = min(len(tel1['distance']), len(tel2['distance']))
        distance_uniform = np.linspace(min_distance, max_distance, common_points)
        
        # Interpolate each driver's data to uniform grid
        tel1_aligned = {
            'distance': distance_uniform,
            'speed': np.interp(distance_uniform, tel1['distance'], tel1['speed']),
            'throttle': np.interp(distance_uniform, tel1['distance'], tel1['throttle']),
            'brake': np.interp(distance_uniform, tel1['distance'], tel1['brake']),
            'gear': np.interp(distance_uniform, tel1['distance'], tel1['gear']),
        }
        
        tel2_aligned = {
            'distance': distance_uniform,
            'speed': np.interp(distance_uniform, tel2['distance'], tel2['speed']),
            'throttle': np.interp(distance_uniform, tel2['distance'], tel2['throttle']),
            'brake': np.interp(distance_uniform, tel2['distance'], tel2['brake']),
            'gear': np.interp(distance_uniform, tel2['distance'], tel2['gear']),
        }
        
        logger.info(f"  ✅ Aligned to {common_points} common points")
        return tel1_aligned, tel2_aligned
    except Exception as e:
        logger.error(f"❌ Error aligning telemetry: {str(e)}")
        return None, None


def calculate_lap_delta(speed1: np.ndarray, speed2: np.ndarray) -> List[float]:
    """Calculate lap delta (time difference) from speed data."""
    try:
        delta = []
        cumulative_delta = 0.0
        
        for i in range(len(speed1)):
            if i == 0:
                delta.append(0.0)
            else:
                # Estimate time difference: time = 1m / (speed/3.6)
                if speed2[i] > 0 and speed1[i] > 0:
                    time_driver1 = 1.0 / (speed1[i] / 3.6)
                    time_driver2 = 1.0 / (speed2[i] / 3.6)
                    point_delta = (time_driver1 - time_driver2) * 1000  # milliseconds
                else:
                    point_delta = 0.0
                
                cumulative_delta += point_delta
                delta.append(cumulative_delta / 1000)  # Back to seconds
        
        return delta
    except Exception as e:
        logger.error(f"❌ Error calculating delta: {str(e)}")
        return []


def downsample_data(data: Dict[str, List], max_points: int = 500) -> Dict[str, List]:
    """Downsample data to maximum number of points."""
    try:
        distance = data.get('distance', [])
        length = len(distance)
        
        if length <= max_points:
            return data
        
        # Calculate step size
        step = length // max_points
        if step < 1:
            step = 1
        
        indices = np.arange(0, length, step)
        if indices[-1] != length - 1:
            indices = np.append(indices, length - 1)
        
        downsampled = {}
        for key, values in data.items():
            if isinstance(values, np.ndarray):
                downsampled[key] = values[indices].tolist()
            else:
                downsampled[key] = [values[i] for i in indices if i < len(values)]
        
        logger.info(f"  📊 Downsampled from {length} to {len(indices)} points")
        return downsampled
    except Exception as e:
        logger.error(f"❌ Error downsampling: {str(e)}")
        return data


def analyze(year: int, grand_prix: str, session_type: str, driver1: str, driver2: str) -> Dict[str, Any]:
    """
    Analyze telemetry for two drivers using OpenF1 API.
    
    Args:
        year: F1 season year
        grand_prix: Grand prix name (e.g., "Monaco")
        session_type: Session type (e.g., "Q", "R")
        driver1: First driver code (e.g., "VER")
        driver2: Second driver code (e.g., "LEC")
    
    Returns:
        Dictionary with telemetry data
    """
    # Normalize and decode inputs
    grand_prix_decoded = unquote(grand_prix) if grand_prix else grand_prix
    session_type_decoded = unquote(session_type).upper() if session_type else session_type
    driver1_code = unquote(driver1).upper() if driver1 else driver1
    driver2_code = unquote(driver2).upper() if driver2 else driver2
    
    # 🚀 LOG ENTRY
    logger.info(f"🚀 OPENF1 TELEMETRY ANALYZE: Received request")
    logger.info(f"  📋 Input (raw): year={year}, grand_prix='{grand_prix}', session_type='{session_type}', driver1='{driver1}', driver2='{driver2}'")
    logger.info(f"  ✅ Input (decoded): year={year}, grand_prix='{grand_prix_decoded}', session_type='{session_type_decoded}', driver1='{driver1_code}', driver2='{driver2_code}'")
    
    try:
        # Step 1: Find the race (meeting)
        logger.info(f"🏁 Requested Race: {year} {grand_prix_decoded} ({session_type_decoded})")
        race = find_race(year, grand_prix_decoded)
        
        if not race:
            error_msg = f"Race not found: {grand_prix_decoded} {year}. Please check race name and year (2024 and later supported)."
            logger.error(f"❌ {error_msg}")
            return {"error": error_msg}
        
        # Get meeting key from race
        meeting_key = race.get('meeting_key')
        if not meeting_key:
            error_msg = f"Could not find meeting key for {grand_prix_decoded} {year}"
            logger.error(f"❌ {error_msg}")
            return {"error": error_msg}
        
        logger.info(f"🏎 Meeting Found: key={meeting_key}, race='{race.get('race_name')}'")
        
        # Step 2: Find the session
        session = find_session(meeting_key, session_type_decoded)
        
        if not session:
            error_msg = f"Session '{session_type}' not found for {grand_prix_decoded} {year}"
            logger.error(f"❌ OpenF1 Failure: {error_msg}")
            return {"error": error_msg}
        
        session_key = session.get('session_key')
        if not session_key:
            error_msg = f"Could not find session key for {grand_prix_decoded} {year} - {session_type_decoded}"
            logger.error(f"❌ {error_msg}")
            return {"error": error_msg}
        
        logger.info(f"⏱ Session Found: key={session_key}, type='{session.get('session_name')}'")
        
        # Step 3: Get drivers for this session
        logger.info(f"👥 Fetching drivers for session...")
        session_drivers = get_drivers_for_session(session_key)
        
        if not session_drivers:
            error_msg = f"No drivers found for session {session_key}"
            logger.error(f"❌ {error_msg}")
            return {"error": error_msg}
        
        # Find driver numbers by code
        driver_map = {d.get('name_acronym', '').upper(): d.get('driver_number') for d in session_drivers}
        
        logger.info(f"  Available drivers: {list(driver_map.keys())}")
        
        driver1_number = driver_map.get(driver1_code)
        driver2_number = driver_map.get(driver2_code)
        
        if not driver1_number:
            error_msg = f"Driver {driver1_code} not found in session. Available: {', '.join(driver_map.keys())}"
            logger.error(f"❌ {error_msg}")
            return {"error": error_msg}
        
        if not driver2_number:
            error_msg = f"Driver {driver2_code} not found in session. Available: {', '.join(driver_map.keys())}"
            logger.error(f"❌ {error_msg}")
            return {"error": error_msg}
        
        # Step 4: Fetch telemetry for both drivers
        logger.info(f"📡 Telemetry Loaded: Fetching for {driver1_code} and {driver2_code}...")
        
        tel1_raw = get_driver_telemetry(session_key, driver1_number)
        tel2_raw = get_driver_telemetry(session_key, driver2_number)
        
        if not tel1_raw or not tel2_raw:
            error_msg = f"Lap data not available for drivers in this session"
            logger.error(f"❌ {error_msg}")
            return {"error": error_msg}
        
        # Process lap data into synthetic telemetry
        logger.info(f"⚙️  Processing lap data into telemetry arrays...")
        tel1_processed = process_lap_data(tel1_raw)
        tel2_processed = process_lap_data(tel2_raw)
        
        if not tel1_processed or not tel2_processed:
            error_msg = "Failed to process lap data"
            logger.error(f"❌ {error_msg}")
            return {"error": error_msg}
        
        # Align telemetry
        logger.info(f"🔄 Aligning telemetry data...")
        tel1_aligned, tel2_aligned = align_telemetry_data(tel1_processed, tel2_processed)
        
        if tel1_aligned is None:
            error_msg = "Failed to align telemetry data"
            logger.error(f"❌ {error_msg}")
            return {"error": error_msg}
        
        # Calculate delta
        logger.info(f"📊 Calculating lap delta...")
        delta = calculate_lap_delta(tel1_aligned['speed'], tel2_aligned['speed'])
        
        # Prepare output
        result_data = {
            'distance': tel1_aligned['distance'].tolist(),
            'driver1_speed': tel1_aligned['speed'].tolist(),
            'driver2_speed': tel2_aligned['speed'].tolist(),
            'driver1_throttle': tel1_aligned['throttle'].tolist(),
            'driver2_throttle': tel2_aligned['throttle'].tolist(),
            'driver1_brake': tel1_aligned['brake'].tolist(),
            'driver2_brake': tel2_aligned['brake'].tolist(),
            'driver1_gear': tel1_aligned['gear'].tolist(),
            'driver2_gear': tel2_aligned['gear'].tolist(),
            'delta': delta,
        }
        
        # Downsample
        downsampled = downsample_data(result_data, max_points=500)
        
        # Get session name for output
        session_name = session.get('session_name', session_type_decoded)
        
        # Build final output
        output = {
            'driver1': driver1_code,
            'driver2': driver2_code,
            'year': year,
            'race': grand_prix_decoded,
            'session': session_name,
            **downsampled
        }
        
        logger.info(f"✅ TELEMETRY ANALYSIS COMPLETE: {len(downsampled['distance'])} points for {driver1_code} vs {driver2_code}")
        
        return output
        
    except Exception as e:
        error_msg = f"Telemetry analysis failed: {str(e)}"
        logger.error(f"❌ OpenF1 Failure: {error_msg}", exc_info=True)
        return {"error": error_msg}


def main():
    """CLI entry point for testing."""
    if len(sys.argv) != 6:
        print("Usage: python telemetry_openf1.py <year> <grand_prix> <session_type> <driver1> <driver2>")
        print("Example: python telemetry_openf1.py 2024 Monaco Q VER LEC")
        sys.exit(1)
    
    year = int(sys.argv[1])
    grand_prix = sys.argv[2]
    session_type = sys.argv[3]
    driver1 = sys.argv[4]
    driver2 = sys.argv[5]
    
    result = analyze(year, grand_prix, session_type, driver1, driver2)
    print(json.dumps(result, indent=2))


if __name__ == "__main__":
    main()
