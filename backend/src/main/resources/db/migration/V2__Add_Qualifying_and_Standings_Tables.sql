-- ✅ Additional Historical Tables for Complete F1 Data
-- Adds qualifying results and standings tables

-- ========================================
-- HISTORICAL QUALIFYING RESULTS
-- ========================================
CREATE TABLE IF NOT EXISTS historical_qualifying (
    id BIGSERIAL PRIMARY KEY,
    race_id BIGINT NOT NULL,
    driver_id BIGINT NOT NULL,
    constructor_id BIGINT,
    position INTEGER, -- Qualifying position (1-20)
    q1 VARCHAR(20), -- Q1 time
    q2 VARCHAR(20), -- Q2 time
    q3 VARCHAR(20), -- Q3 time
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (race_id) REFERENCES historical_race(id),
    FOREIGN KEY (driver_id) REFERENCES historical_driver(id),
    FOREIGN KEY (constructor_id) REFERENCES historical_constructor(id),
    UNIQUE(race_id, driver_id)
);

CREATE INDEX IF NOT EXISTS idx_historical_qualifying_race ON historical_qualifying(race_id);
CREATE INDEX IF NOT EXISTS idx_historical_qualifying_driver ON historical_qualifying(driver_id);

-- ========================================
-- HISTORICAL DRIVER STANDINGS
-- ========================================
CREATE TABLE IF NOT EXISTS historical_driver_standings (
    id BIGSERIAL PRIMARY KEY,
    year INTEGER NOT NULL,
    driver_id BIGINT NOT NULL,
    position INTEGER, -- Championship position
    points DECIMAL(8,2),
    wins INTEGER DEFAULT 0,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (driver_id) REFERENCES historical_driver(id),
    UNIQUE(year, driver_id)
);

CREATE INDEX IF NOT EXISTS idx_historical_driver_standings_year ON historical_driver_standings(year);
CREATE INDEX IF NOT EXISTS idx_historical_driver_standings_driver ON historical_driver_standings(driver_id);

-- ========================================
-- HISTORICAL CONSTRUCTOR STANDINGS
-- ========================================
CREATE TABLE IF NOT EXISTS historical_constructor_standings (
    id BIGSERIAL PRIMARY KEY,
    year INTEGER NOT NULL,
    constructor_id BIGINT NOT NULL,
    position INTEGER, -- Championship position
    points DECIMAL(8,2),
    wins INTEGER DEFAULT 0,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (constructor_id) REFERENCES historical_constructor(id),
    UNIQUE(year, constructor_id)
);

CREATE INDEX IF NOT EXISTS idx_historical_constructor_standings_year ON historical_constructor_standings(year);
CREATE INDEX IF NOT EXISTS idx_historical_constructor_standings_constructor ON historical_constructor_standings(constructor_id);

COMMIT;
