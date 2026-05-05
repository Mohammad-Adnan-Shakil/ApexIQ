-- Repair favorite_driver column for production deployment
-- This migration safely adds the column if missing and repairs any Flyway issues

-- Add column safely (idempotent operation)
ALTER TABLE users ADD COLUMN IF NOT EXISTS favorite_driver VARCHAR(20);

-- Create index for performance (idempotent)
CREATE INDEX IF NOT EXISTS idx_users_favorite_driver ON users(favorite_driver);

-- Update existing records to set NULL values (safe operation)
UPDATE users SET favorite_driver = NULL WHERE favorite_driver IS NULL;
