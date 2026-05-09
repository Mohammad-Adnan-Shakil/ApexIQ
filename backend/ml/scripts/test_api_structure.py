#!/usr/bin/env python3
import requests
import json

# Check laps endpoint structure
laps = requests.get('https://api.openf1.org/v1/laps?limit=1').json()
print("Laps response type:", type(laps))
print("Laps keys:" if isinstance(laps, dict) else "Laps length:", list(laps.keys()) if isinstance(laps, dict) else len(laps))
if isinstance(laps, dict) and laps:
    print("Sample lap structure:", json.dumps(list(laps.values())[0] if laps.values() else {}, indent=2))

# Check drivers endpoint
drivers = requests.get('https://api.openf1.org/v1/drivers').json()
print("\n\nDrivers response type:", type(drivers))
print("Drivers first entry:", drivers[0] if isinstance(drivers, list) and drivers else "N/A")

# Check meetings endpoint
meetings = requests.get('https://api.openf1.org/v1/meetings?year=2024').json()
print("\n\nMeetings 2024:", len(meetings))
print("First meeting:", meetings[0] if meetings else "N/A")

# Check sessions endpoint
sessions = requests.get('https://api.openf1.org/v1/sessions').json()
print("\n\nTotal sessions:", len(sessions) if isinstance(sessions, list) else "dict" if isinstance(sessions, dict) else "other")
print("First session:", sessions[0] if isinstance(sessions, list) and sessions else "N/A")
