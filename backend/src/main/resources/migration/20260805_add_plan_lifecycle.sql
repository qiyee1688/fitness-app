ALTER TABLE plans
    ADD COLUMN IF NOT EXISTS status_changed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;

CREATE INDEX IF NOT EXISTS idx_plans_lifecycle_queue
    ON plans(user_id, status, start_date, status_changed_at);
