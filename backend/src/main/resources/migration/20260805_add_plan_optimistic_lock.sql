-- Add optimistic locking and enforce the one-ACTIVE-Plan invariant for existing databases.
ALTER TABLE plans ADD COLUMN IF NOT EXISTS version INT NOT NULL DEFAULT 0;

CREATE UNIQUE INDEX IF NOT EXISTS uq_plans_one_active_per_user
    ON plans(user_id) WHERE status = 'ACTIVE';
