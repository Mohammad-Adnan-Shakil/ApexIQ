#!/usr/bin/env python3
"""Test fuzzy race matching with multiple race names"""
import sys
sys.path.insert(0, '/projects/DeltaBox/backend/ml/scripts')

from telemetry_openf1 import analyze

test_cases = [
    {"year": 2024, "race": "Monaco", "session": "R", "driver1": "VER", "driver2": "LEC"},
    {"year": 2024, "race": "Canada", "session": "R", "driver1": "VER", "driver2": "LEC"},
    {"year": 2024, "race": "Bahrain", "session": "R", "driver1": "HAM", "driver2": "RUS"},
]

print("\n" + "="*70)
print("FUZZY RACE MATCHING TEST")
print("="*70)

for i, test in enumerate(test_cases, 1):
    print("\nTest {}: {} {} {} {} vs {}".format(
        i, test['year'], test['race'], test['session'], 
        test['driver1'], test['driver2']))
    
    result = analyze(test['year'], test['race'], test['session'], 
                     test['driver1'], test['driver2'])
    
    if 'error' in result:
        print("  STATUS: FAILED")
        print("  Error: {}".format(result['error']))
    else:
        points = len(result.get('distance', []))
        print("  STATUS: SUCCESS")
        print("  Points: {}".format(points))
        print("  Race: {}".format(result.get('race')))
        print("  Session: {}".format(result.get('session')))

print("\n" + "="*70)
