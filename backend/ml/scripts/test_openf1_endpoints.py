#!/usr/bin/env python3
"""
Test various OpenF1 telemetry endpoints and parameters
"""
import requests
import json

BASE = "https://api.openf1.org/v1"

# Get a recent qualifying session with telemetry
meetings = requests.get(f"{BASE}/meetings?year=2026").json()
recent = meetings[-1] if meetings else None

if not recent:
    print("No meetings found for 2026")
    exit(1)

print(f"Testing with: {recent['meeting_name']} (key={recent['meeting_key']})")

sessions = requests.get(f"{BASE}/sessions?meeting_key={recent['meeting_key']}").json()
print(f"\nAvailable sessions: {[s['session_name'] for s in sessions]}")

# Try each session
for session in sessions:
    session_key = session['session_key']
    session_name = session['session_name']
    
    # Try different endpoint parameter formats
    test_urls = [
        f"{BASE}/telemetry?session_key={session_key}&driver_number=1",
        f"{BASE}/telemetry?meeting_key={recent['meeting_key']}&session_key={session_key}&driver_number=1",
        f"{BASE}/car_data?session_key={session_key}&driver_number=1",
        f"{BASE}/position?session_key={session_key}&driver_number=1",
    ]
    
    print(f"\n{session_name}:")
    for url in test_urls:
        try:
            r = requests.get(url, timeout=5)
            endpoint = url.split('/')[-1].split('?')[0]
            data = r.json() if r.status_code == 200 else {}
            count = len(data) if isinstance(data, list) else (1 if data else 0)
            print(f"  {endpoint}: {r.status_code} ({count} records)")
            if r.status_code == 200 and data and isinstance(data, list) and len(data) > 0:
                print(f"    Sample keys: {list(data[0].keys())}")
        except Exception as e:
            print(f"  Error: {str(e)}")
