-- Add google_sub column to users table for Google OAuth2 authentication
ALTER TABLE users ADD COLUMN IF NOT EXISTS google_sub VARCHAR(255);

-- Create index on google_sub for faster lookups
CREATE INDEX IF NOT EXISTS idx_users_google_sub ON users(google_sub);

COMMIT;
