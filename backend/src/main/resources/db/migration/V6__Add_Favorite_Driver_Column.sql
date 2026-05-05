-- Add favorite_driver column to users table
ALTER TABLE users ADD COLUMN IF NOT EXISTS favorite_driver VARCHAR(10);

-- Add index for better performance
CREATE INDEX IF NOT EXISTS idx_users_favorite_driver ON users(favorite_driver);

COMMIT;
