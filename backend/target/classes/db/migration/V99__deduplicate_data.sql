-- Remove duplicate drivers, keeping only the lowest ID for each code
DELETE FROM driver WHERE id NOT IN (
  SELECT MIN(id) FROM driver GROUP BY code, season
);

-- Remove duplicate teams, keeping only the lowest ID for each name  
DELETE FROM team WHERE id NOT IN (
  SELECT MIN(id) FROM team GROUP BY name
);

-- Remove duplicate races, keeping only the lowest ID for each round/season combination
DELETE FROM race WHERE id NOT IN (
  SELECT MIN(id) FROM race GROUP BY season, round, driver_id
);

COMMIT;
