#!/usr/bin/env python3
import requests

# Get 2024 Canadian GP race
meetings = requests.get('https://api.openf1.org/v1/meetings?year=2024').json()
canadian = [m for m in meetings if 'Canada' in m['meeting_name'] or 'Canadian' in m['meeting_name']][0]
meeting_key = canadian['meeting_key']
print(f"Meeting: {canadian['meeting_name']} (key={meeting_key})")

# Get sessions
sessions = requests.get(f'https://api.openf1.org/v1/sessions?meeting_key={meeting_key}').json()
race_session = [s for s in sessions if s['session_type'] == 'Race'][0]
session_key = race_session['session_key']
print(f"Session: {race_session['session_name']} (key={session_key})")

# Get drivers
drivers = requests.get(f'https://api.openf1.org/v1/drivers?session_key={session_key}').json()
ver = [d for d in drivers if d['name_acronym'] == 'VER'][0]
driver_number = ver['driver_number']
print(f"Driver: {ver['broadcast_name']} (number={driver_number})")

# Get lap data
laps = requests.get(f'https://api.openf1.org/v1/laps?session_key={session_key}&driver_number={driver_number}').json()
print(f"\nLap data response type: {type(laps)}")
if isinstance(laps, list) and laps:
    print(f"Number of laps: {len(laps)}")
    lap = laps[0]
    print(f"\nSample lap keys: {list(lap.keys())}")
    print(f"Sample lap data: {lap}")
elif isinstance(laps, dict):
    print(f"Response keys: {list(laps.keys())}")
    print(f"Full response: {laps}")
